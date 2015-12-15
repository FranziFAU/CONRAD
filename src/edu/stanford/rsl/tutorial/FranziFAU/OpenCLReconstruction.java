package edu.stanford.rsl.tutorial.FranziFAU;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLImageFormat.ChannelOrder;
import com.jogamp.opencl.CLImageFormat.ChannelType;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import edu.stanford.rsl.conrad.opencl.OpenCLUtil;

public class OpenCLReconstruction {
	
	private Grid2D image;
	private CLProgram program = null;
	private CLContext context = null;
	private CLDevice device = null;
	private CLKernel kernel = null;
	
	public OpenCLReconstruction(Grid2D phantom){
		image = new Grid2D(phantom);
	}
	
	public Grid2D adding(){
		long start = System.nanoTime();
		
		for(int i = 0; i < 1000; i ++){
			NumericPointwiseOperators.addedBy(image, image);
		}	
		long end = System.nanoTime();
		
		System.out.println("Time difference " + ((end - start)/1e6) + " ms");
		
		Grid2D result = new Grid2D(image);
		return result;
	}
	
	
	public Grid2D add(OpenCLGrid2D image1, OpenCLGrid2D image2){
		
		//create context
		if(context == null){
			context = OpenCLUtil.getStaticContext();
		}
		//select device
		if(device == null){
			device = context.getMaxFlopsDevice();
		}
		//define local and global sizes
		int width = Math.min(image1.getWidth(), image2.getWidth());
		int height = Math.min(image1.getHeight(), image2.getHeight());
		
		int imageSize = width*height;
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 8);
		int globalWorkSizeW = OpenCLUtil.roundUp(localWorkSize,width); // rounded up to the nearest multiple of localWorkSize
		int globalWorkSizeH = OpenCLUtil.roundUp(localWorkSize,height);
		
		//load sources, create and build programm
		if(program == null){
		try {
			program = context.createProgram(this.getClass().getResourceAsStream("exercise4.cl"))
					.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		}

		
		//create output image
		CLBuffer<FloatBuffer> output = context.createFloatBuffer(imageSize, Mem.WRITE_ONLY);
		if(kernel == null){
			kernel = program.createCLKernel("addImages");
		}
		// createCommandQueue
		CLCommandQueue queue = device.createCommandQueue();
		image1.getDelegate().prepareForDeviceOperation();
		image2.getDelegate().prepareForDeviceOperation();
		// put memory on the graphics card
		
		kernel.putArg(image1.getDelegate().getCLBuffer()).putArg(image2.getDelegate().getCLBuffer()).putArg(output).putArg(width).putArg(height);
		kernel.rewind();
				
		queue.put2DRangeKernel(kernel, 0,0,globalWorkSizeW, globalWorkSizeH,localWorkSize,localWorkSize).putBarrier()
			//put memory from graphic card to host
			.putReadBuffer(output, true)
			.finish();
		
		
		Grid2D result = new Grid2D(image1);
		output.getBuffer().rewind();
		
		for (int i = 0; i < result.getSize()[1]; ++i) {
			for(int j = 0; j < result.getSize()[0]; j++){
				result.setAtIndex(j,i,output.getBuffer().get());
			}
			
		}

		output.release();	
		queue.release();

		
		return result;
	}
	
	public Grid2D openCLBackprojection(OpenCLGrid2D sinogramm, int worksize, float detectorSpacing, int numberProjections,double scanAngle){
		//create context	
		CLContext context = OpenCLUtil.getStaticContext();
	
		//select device
		CLDevice device = context.getMaxFlopsDevice();
		
		//define local and global sizes
		int width = sinogramm.getWidth();
		int height = sinogramm.getHeight();
		
		int imageSize = width*height;
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), worksize);
		int globalWorkSizeW = OpenCLUtil.roundUp(localWorkSize,width); // rounded up to the nearest multiple of localWorkSize
		int globalWorkSizeH = OpenCLUtil.roundUp(localWorkSize,height);
		
		//load sources, create and build programm
		
		try {
		CLProgram program = context.createProgram(this.getClass().getResourceAsStream("exercise4.cl"))
					.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		// create image from input grid
		CLImageFormat format = new CLImageFormat(ChannelOrder.INTENSITY, ChannelType.FLOAT);		

		
		return image;
		
	}
	

}
