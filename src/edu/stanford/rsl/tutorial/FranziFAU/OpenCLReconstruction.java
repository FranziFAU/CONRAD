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
	
	protected OpenCLGrid2D image;
	
	public OpenCLReconstruction(Grid2D phantom){
		image = new OpenCLGrid2D(phantom);
	}
	
	public Grid2D adding(){
		
		for(int i = 0; i < 1000000; i ++){
			NumericPointwiseOperators.addBy(image, image);
		}	
		
		Grid2D result = new Grid2D(image);
		return result;
	}
	
	
	public OpenCLGrid2D add(OpenCLGrid2D image1, OpenCLGrid2D image2){
		//create context
		CLContext context = OpenCLUtil.createContext();
		//select device
		CLDevice device = context.getMaxFlopsDevice();
		//define local and global sizes
		int width = Math.min(image1.getWidth(), image2.getWidth());
		int height = Math.min(image1.getHeight(), image2.getHeight());
		
		int imageSize = width*height;
		int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 8);
		int globalWorkSize = OpenCLUtil.roundUp(localWorkSize,imageSize); // rounded up to the nearest multiple of localWorkSize
		
		
		//load sources, create and build programm
		CLProgram program = null;
		try {
			program = context.createProgram(this.getClass().getResourceAsStream("exercise4.cl"))
					.build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		//write images into buffer
		CLImageFormat format = new CLImageFormat(ChannelOrder.INTENSITY, ChannelType.FLOAT);
		//image1
		CLBuffer<FloatBuffer> imageBuffer1 = context.createFloatBuffer(imageSize, Mem.READ_ONLY);
		
		for (int i=0;i<image1.getSize()[1];++i){
			for (int j=0;j<image1.getSize()[0];++j)
				imageBuffer1.getBuffer().put(image1.getAtIndex(j, i));
		}
		imageBuffer1.getBuffer().rewind();

		//image2
		CLBuffer<FloatBuffer> imageBuffer2 = context.createFloatBuffer(imageSize, Mem.READ_ONLY);
		
		for (int i=0;i<image2.getSize()[1];++i){
			for (int j=0;j<image2.getSize()[0];++j)
				imageBuffer2.getBuffer().put(image1.getAtIndex(j, i));
		}
		imageBuffer2.getBuffer().rewind();
		
		
		//create output image
		CLBuffer<FloatBuffer> output = context.createFloatBuffer(imageSize, Mem.WRITE_ONLY);
		
		CLKernel kernel = program.createCLKernel("addImages");
		kernel.putArg(imageBuffer1).putArg(imageBuffer2).putArg(output).putArg(imageSize);
		
		// createCommandQueue
		CLCommandQueue queue = device.createCommandQueue();
		queue.put1DRangeKernel(kernel, 0, globalWorkSize,localWorkSize).putBarrier()
			.putReadBuffer(output, true)
			.finish();
		
		
		OpenCLGrid2D result = new OpenCLGrid2D(image1);
		output.getBuffer().rewind();
		
		for (int i = 0; i < result.getBuffer().length; ++i) {
			result.getBuffer()[i] = output.getBuffer().get();
		}
		
		
		kernel.release();
		program.release();
		output.release();
		imageBuffer1.release();
		imageBuffer2.release();
		
		return result;
	}
	
	

}
