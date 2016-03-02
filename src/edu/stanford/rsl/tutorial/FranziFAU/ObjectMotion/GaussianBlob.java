package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
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
		gaussianValue *= Math.sqrt(2*Math.PI)*deviation[0]*deviation[1];
		
		return (float)gaussianValue;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		
		//Parameters for all methods
		//GaussianBlob
		int imageWidth = 250;
		int imageHeight = 250;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {30.d,2.d};
		
		//MovingGaussian
		double frequency = 0.4d;
		
		double [] changedMeanValue = {0.0d,-40.0d};		
		double [] changedStandardDeviation = {30.d,2.d};
		
		//Projection
		int numberProjections = 379;
		double detectorSpacing = 1.0d;
		int numberPixel = 400;
		
		//create GaussianBlob
		GaussianBlob object = new GaussianBlob(imageWidth,imageHeight,imageSpacing,	meanValue ,standardDeviation);
		object.show("GaussianBlob");	
		
		GaussianBlob changedObject = new GaussianBlob(imageWidth, imageHeight,imageSpacing, changedMeanValue, changedStandardDeviation);
		changedObject.show("changedGaussianBlob");
		
		MovingGaussian movingObject = new MovingGaussian(imageWidth,imageHeight,
				imageSpacing,meanValue ,standardDeviation,frequency, changedMeanValue,changedStandardDeviation);
		
		//create sinogramm of gaussianBlob
		ParallelProjection sinogramm = new ParallelProjection(numberProjections,detectorSpacing,numberPixel);
		Grid2D sino1 = sinogramm.createSinogrammMoving(movingObject);
		sino1.show("Sinogramm");
		
		//backproject sinogramm
		ParallelBackprojection image = new ParallelBackprojection(object);
		Grid2D reconstructed =  image.filteredBackprojection(sinogramm, detectorSpacing, numberProjections);
		image.show("Backprojected image");
		
		
		Grid2D sino2 = sinogramm.createSinogramm(reconstructed);
		sino2.show("Sinogramm2");

		Grid2D reconstructed2 = image.filteredBackprojection(sino2, detectorSpacing, numberProjections);
		reconstructed2.show("2nd backprojection");

		
		Grid2D subtract = (Grid2D) NumericPointwiseOperators.subtractedBy(sino2, sino1);		
		subtract.show();
	}
}
