package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid2DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericGrid;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;



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
		
		double exponent = -0.5*(Math.pow((x - mean[0])/deviation[0], 2) 
				+ Math.pow((y - mean[1])/deviation[1], 2));
		double gaussianValue = Math.exp(exponent);
		gaussianValue /= Math.sqrt(2*Math.PI)*deviation[0]*deviation[1];
		
		return (float)gaussianValue;
	}
	
	public static void gradProjectionGauss(NumericGrid gridRes, final NumericGrid grid, int value, boolean offsetleft) {
		subtractOffset(gridRes,grid,grid,0,value,offsetleft);
		gridRes.notifyAfterWrite();
	}
	
	private static void subtractOffset(NumericGrid gridResult, final NumericGrid gridA, final NumericGrid gridB, int xOffset, int yOffset,boolean offsetleft) {

		if(gridA.getSize()[0] != gridB.getSize()[0] || gridA.getSize()[1] != gridB.getSize()[1] ){
			System.err.println("Grids have different sizes so they can not be subtracted.");
		}
		
		for (int x = xOffset; x < gridA.getSize()[0]+xOffset; ++x){
			for (int y = yOffset; y < gridA.getSize()[1]+yOffset; ++y){
				
					
					int xIdx = (x >= gridA.getSize()[0] || x < 0) ? Math.min(Math.max(0, x), gridA.getSize()[0]-1) : x;
					int yIdx = (y >= gridA.getSize()[1] || y < 0) ? Math.min(Math.max(0, y), gridA.getSize()[1]-1) : y;
					

					if(offsetleft)
						gridResult.setValue(gridA.getValue(new int[]{xIdx,yIdx}) - gridB.getValue(new int[]{x-xOffset,y-yOffset}),
								new int[]{x-xOffset,y-yOffset});
					else
						gridResult.setValue(gridA.getValue(new int[]{x-xOffset,y-yOffset}) - gridB.getValue(new int[]{xIdx,yIdx}),
								new int[]{x-xOffset,y-yOffset});
				}
		}
		gridResult.notifyAfterWrite();
	}
	
	public static float [] compareSinogrammsLinewise(Grid2D sino1, Grid2D sino2){
		
		NumericGridOperator op = new NumericGridOperator();
		
		if(sino1.getSize()[0] != sino2.getSize()[0] || sino1.getSize()[1] != sino2.getSize()[1] ){
			System.err.println("Sinogramms have different sizes so they can not be subtracted.");
		}		
		
		float [] result = new float[sino1.getHeight()];
		
		for(int projectionLine = 0; projectionLine < sino1.getHeight(); projectionLine++){
			
			Grid1D lineSino1 = sino1.getSubGrid(projectionLine);
			Grid1D lineSino2 = sino2.getSubGrid(projectionLine);
			
			result[projectionLine] = op.weightedSSD(lineSino1, lineSino2, 1.0, 0.0);			
		}
		
		return result;
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
	
	public static void main(String[] args) {
		new ImageJ();
		
		//Parameters for all methods
		//GaussianBlob
		int imageWidth = 250;
		int imageHeight = 250;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {15.d,15.d};
		
		//MovingGaussian
		double frequency = 1.3d;		
		double [] changedMeanValue = {0.0d,0.0d};		
		double [] changedStandardDeviation = {30.d,30.d};
		
		//Projection
		int numberProjections = 379;
		double detectorSpacing = 1.3d;
		int numberPixel = 500;
		double timeFactor = 0.7d; // time associated with one projection
		
		//create GaussianBlob
		GaussianBlob object = new GaussianBlob(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);
		object.show("GaussianBlob");	
		
		GaussianBlob changedObject = new GaussianBlob(imageWidth, imageHeight, imageSpacing, changedMeanValue, changedStandardDeviation);
		changedObject.show("changedGaussianBlob");
		
		MovingGaussian movingObject = new MovingGaussian(imageWidth,imageHeight,
				imageSpacing, meanValue, standardDeviation, frequency, changedMeanValue, changedStandardDeviation);
		
		//create sinogramm of gaussianBlob
		ParallelProjection sinogramm = new ParallelProjection(numberProjections, detectorSpacing, numberPixel, timeFactor);
		Grid2D sino1 = sinogramm.createSinogrammMoving(movingObject);
		sino1.show("Sinogramm");
		
		//backproject sinogramm
		ParallelBackprojection image = new ParallelBackprojection(object);
		Grid2D reconstructed =  image.filteredBackprojection(sinogramm, detectorSpacing, numberProjections);
		image.show("Backprojected image");		
		
		Grid2D sino2 = sinogramm.createSinogramm(reconstructed);
		sino2.show("Sinogramm2");
		
		
		
		// Idea 1: Look at fourier space, motion introduces high frequencies
		//compare FFT from both sinogramms (ssd, high pass filter, usw.)

/*		Grid2DComplex sino1C = new Grid2DComplex(sino1);
		sino1C.transformForward();
		sino1C.show();
		
		Grid2DComplex sino2C = new Grid2DComplex(sino2);
		sino2C.transformForward();
		sino2C.show();

		float ssd = op.weightedSSD(sino1C, sino2C, 1.0, 1.0);
		System.out.print(ssd);
*/
		
		//Idea 2: Look at gradient images
		
/*		// compute Gradient image for sino1		
		Grid2D result =  new Grid2D(sino1);
		op.fill(result, 0.0f);		
		gradProjectionGauss(result,sino1,3,true);
		result.show("Gradient1");
		float mean = op.sum(result);
		mean /= result.getNumberOfElements();		
		System.out.println(mean);	
		
		// compute Gradient image for sino2
		Grid2D result2 =  new Grid2D(sino2);
		op.fill(result2, 0.0f);		
		gradProjectionGauss(result2,sino2,3,true);
		result2.show("Gradient2");
		float mean2 = op.sum(result2);
		mean2 /= result2.getNumberOfElements();		
		System.out.println(mean2);		
*/
		
		//Idea 3: compare each projection line of the 2 sinogramms with SSD
		
		float [] ssd = compareSinogrammsLinewise(sino1,sino2);		
		float mean = computeMean(ssd);
		float variance = computeVariance(ssd,mean);		
		System.out.println("MeanSSD: " + mean + "  VarianzSSD: " + variance);
		
		
//		Grid2D subtract = (Grid2D) NumericPointwiseOperators.subtractedBy(sino2, sino1);		
//		subtract.show();
	}
}
