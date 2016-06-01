package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericGrid;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;




public class GaussianBlob extends Grid2D {

	protected double[] meanValue;	
	protected double [] standardDeviation;	
	
	public GaussianBlob(int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue, double [] standardDeviation){
		
		// create Image with the desired size
		super(imageWidth, imageHeight);
		this.setSpacing(imageSpacing[0], imageSpacing[1]);
		this.setOrigin((-((imageWidth)*imageSpacing[0])/2),(-((imageHeight)*imageSpacing[1])/2));
		this.meanValue = meanValue;		
		this.standardDeviation = standardDeviation;
		// call Initialization function
		initializeGauss(this, meanValue, standardDeviation);
	}
	
	public double [] getMeanValue(){
		return this.meanValue;
	}
	
	public double [] getStandardDeviation(){
		return this.standardDeviation;
	}
	
	// design Gaussian Blob with the given Parameters
	protected void initializeGauss(GaussianBlob gauss, double [] mean, double [] deviation){
	
		//Set Pixel Values of the Gaussian
		for(int width = 0; width < gauss.getWidth(); width ++){
			for(int height = 0; height < gauss.getHeight(); height ++){

				double [] worldCoordinates = gauss.indexToPhysical(width, height);				
				float value = gaussian2D(worldCoordinates[0], worldCoordinates[1],mean ,deviation);	
				gauss.setAtIndex(width, height, value);
			}
		}	
	}
	
	//Compute Value of the 2D Gaussian for the given position
	private float gaussian2D (double x, double y, double [] mean, double [] deviation){		
	
		float circle = 0.0f;
		
		double left = Math.pow(x-mean[0],2) + Math.pow(y-mean[1], 2);
		double right = Math.pow(deviation[0], 2);
		if(left <= right){
			circle = 1.0f;
		}
		
		return circle;

	}
	
	//computes the mean of an array
	public static float computeMean(float [] array){
		float mean = 0;		
		
		for(int i = 0; i < array.length; i++){
			mean += array[i];
		}
		
		mean /= array.length;
		
		return mean;
		
	}
	
	public static float computeVariance(float [] array, float mean){
		float variance = 0.f;
		
		for(int i = 0; i < array.length; i++){			
			variance += (array[i]-mean)*(array[i]-mean);			
		}
		
		variance /= array.length;
		
		return variance;
	}
	

	
	public static void main(String[] args) throws IOException  {		
		
		new ImageJ();
		
		//Loading Trajcectory
		
		String file = "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Trajectory2.tif";
		Grid2D trajectoryImage = ImageUtil.wrapImagePlus(IJ.openImage(file)).getSubGrid(0);
//		trajectoryImage.show("Trajectory");
		
		//Parameters for all methods
	
		int imageWidth = 300;
		int imageHeight = 300;
		double[] imageSpacing = {1.0d,1.0d};
		
		//Projection
		int numberProjections = 2*180;
		double detectorSpacing = 1.0d;
		int numberPixel = 500;
		double timeFactor = 18.0d/numberProjections; // time associated with one projection in seconds		
		
		double frequency = 1.0d;	// in 1/second	
		//
		//GaussianBlob pulsating
		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {30.0d,30.0d};
		
		//MovingGaussian

		double [] changedMeanValue = {0.0d,0.0d};		
		double [] changedStandardDeviation = {15.0d,15.0d};			
				
//		//create GaussianBlob
		GaussianBlob object = new GaussianBlob(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);
//		object.show("GaussianBlob");	

		GaussianBlob changedObject = new GaussianBlob(imageWidth, imageHeight, imageSpacing, changedMeanValue, changedStandardDeviation);
//		changedObject.show("changedGaussianBlob");
		
		MovingGaussian movingObject = new MovingGaussian(imageWidth,imageHeight,
				imageSpacing, meanValue, standardDeviation, frequency, changedMeanValue, changedStandardDeviation);
							
		//create sinogramm of gaussianBlob
		ParallelProjection sinogramm = new ParallelProjection(numberProjections, detectorSpacing, numberPixel, timeFactor);
		Grid2D sino1 = sinogramm.createSinogrammMoving(movingObject);
//		sino1.show("Sinogramm");
		
		//backproject sinogramm
		ParallelBackprojection image = new ParallelBackprojection(object);
		Grid2D reconstruct =  image.filteredBackprojection(sino1, detectorSpacing, numberProjections);
//		image.show("Backprojected image");				
	
		//create sinogramm of backprojected image
		Grid2D sino2 = sinogramm.createSinogramm(reconstruct);
//		sino2.show("Sinogramm2");
		
		
		//saving the images	

		String title = "Pulsating";
		
		ImagePlus imageSino1 = ImageUtil.wrapGrid(sino1, title);		
		IJ.save(imageSino1, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/1.7_Sino1.tif");
		
		
		ImagePlus imageBack = ImageUtil.wrapGrid(reconstruct, title);		
		IJ.save(imageBack, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/1.7_Back.tif");
		
		
		ImagePlus imageSino2 = ImageUtil.wrapGrid(sino2, title);		
		IJ.save(imageSino2, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/1.7_Sino2.tif");
		
		
		//Gaussian Blob offset		
		
		double sigmaC = (standardDeviation[0] + changedStandardDeviation[0])/2;
//		double ln = sigmaC / standardDeviation[0];	
			
	

		double shift = standardDeviation[0] - sigmaC;
		System.out.println("shift: " + shift);

		System.out.println("sigmaC: " + sigmaC);
		
		double [] meanValueOff = {0.0d, -shift};
		double [] changedMeanValueOff = {0.0d,shift};
		double [] standardDeviationOff = {sigmaC, sigmaC};
		double [] changedStandardDeviationOff = {sigmaC, sigmaC};

		GaussianBlob objectOff = new GaussianBlob(imageWidth, imageHeight, imageSpacing, meanValueOff, standardDeviationOff);
//		objectOff.show("Off");
		
		GaussianBlob changedObjectOff = new GaussianBlob(imageWidth, imageHeight, imageSpacing, changedMeanValueOff, changedStandardDeviationOff);
//		changedObjectOff.show("Off2");
		
		MovingGaussian movingObjectOff = new MovingGaussian(imageWidth,imageHeight,
				imageSpacing, meanValueOff, standardDeviationOff, frequency, changedMeanValueOff, changedStandardDeviationOff);		

		//create sinogramm of gaussianBlob
		ParallelProjection sinogrammOff = new ParallelProjection(numberProjections, detectorSpacing, numberPixel, timeFactor);
		Grid2D sino1Off = sinogrammOff.createSinogrammMoving(movingObjectOff);
//		sino1Off.show("SinogrammOff");
		
		//backproject sinogramm
		ParallelBackprojection imageOff = new ParallelBackprojection(objectOff);
		Grid2D reconstructOff =  imageOff.filteredBackprojection(sino1Off, detectorSpacing, numberProjections);
//		imageOff.show("Backprojected imageOff");				
	
		//create sinogramm of backprojected image
		Grid2D sino2Off = sinogrammOff.createSinogramm(reconstructOff);
//		sino2Off.show("Sinogramm2Off");	
		
		String titleOff = "Offset";
		
		ImagePlus imageSino1Off = ImageUtil.wrapGrid(sino1Off, titleOff);		
		IJ.save(imageSino1Off, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/2.7_sino1.tif");
		
		
		ImagePlus imageBackOff = ImageUtil.wrapGrid(reconstructOff, titleOff);		
		IJ.save(imageBackOff, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/2.7_back.tif");
		
		
		ImagePlus imageSino2Off = ImageUtil.wrapGrid(sino2Off, title);		
		IJ.save(imageSino2Off, "/home/cip/medtech2011/ef58ozyd/Projektarbeit/Simulation4/18 Perioden/2.7_sino2.tif");
		
		//Design trajectory
		
//		Grid2D singlePoint = new Grid2D(imageWidth, imageHeight);
//		singlePoint.setSpacing(imageSpacing);
//		singlePoint.setOrigin((-((imageWidth)*imageSpacing[0])/2),(-((imageHeight)*imageSpacing[1])/2));		
//		double [] idx = {0.0d,sigmaC};
//		double [] tmp = singlePoint.physicalToIndex(idx[0], idx[1]);
//		int [] pixel = {(int)Math.floor(tmp[0]),(int)Math.floor(tmp[1])};
//		singlePoint.setValue(1.0f, pixel);		
//		singlePoint.show();	
//		
//		ParallelProjection sinogrammT = new ParallelProjection(numberProjections, detectorSpacing, numberPixel, timeFactor);
//		Grid2D trajectory = sinogrammT.createSinogramm(singlePoint);
//		trajectory.show("Result");		
//		String titleTrajectory = "Trajectory";	
//		
//		ImagePlus trajectoryImg = ImageUtil.wrapGrid(trajectory, titleTrajectory);
//		IJ.save(trajectoryImg,"/home/cip/medtech2011/ef58ozyd/Projektarbeit/Trajectory2.tif");
		
		
		float [] pulsating = new float[numberProjections];
		float [] offset = new float[numberProjections];
		
		
		for(int y = 0; y < trajectoryImage.getHeight();y++){
			pulsating[y] = 0.f;
			for(int x = 1; x < trajectoryImage.getWidth(); x++){			
				if(trajectoryImage.getAtIndex(x, y) > trajectoryImage.getAtIndex(x-1, y)){
					
					pulsating[y] = sino2.getAtIndex(x, y);
				
				}
				
				if(trajectoryImage.getAtIndex(x, y) > trajectoryImage.getAtIndex(x-1, y)){				
					
					offset[y] = sino2Off.getAtIndex(x, y);
					
				}
			}
		}
		



	
		String filename = "Pulsating.txt";

		BufferedWriter outputWriter = null;
		outputWriter = new BufferedWriter(new FileWriter(filename));

		for(int j = 0; j < numberProjections; j++){
			String b = Double.toString(pulsating[j]);
			outputWriter.write(b);
			outputWriter.write(' ');
			
		}
		outputWriter.close();
		

		
		String filenameOff = "Offset.txt";
		
		BufferedWriter outputWriterOff = null;
		outputWriterOff = new BufferedWriter(new FileWriter(filenameOff));
		
		
		for(int j = 0; j < numberProjections; j++){
			String Off = Double.toString(offset[j]);
			outputWriterOff.write(Off);
			outputWriterOff.write(' ');
			
			
		}
		outputWriterOff.close();
		
		System.out.print("Ende");
		
	}
}
