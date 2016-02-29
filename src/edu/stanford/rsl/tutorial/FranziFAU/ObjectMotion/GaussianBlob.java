package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;



public class GaussianBlob extends Grid2D {

	protected double[] meanValue;	
	protected double [] standardDeviation;

	
	
	public GaussianBlob(int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue, double [] standardDeviation){
		
		// create Image with the desired size
		super(imageWidth, imageHeight);
		this.setSpacing(imageSpacing[0], imageSpacing[1]);
		this.setOrigin((-((imageWidth)*imageSpacing[0])/2),(-((imageHeight)*imageSpacing[1])/2));
		// call Initialization function
		initialize(meanValue, standardDeviation);
	}
	
	// design Gaussian Blob with the given Parameters
	protected void initialize(double [] mean, double [] deviation){
		
		//set Variables
		this.meanValue = mean;		
		this.standardDeviation = deviation;	
		
	
		//Set Pixel Values of the Gaussian
		for(int width = 0; width < this.getWidth(); width ++){
			for(int height = 0; height < this.getHeight(); height ++){
				
				double [] worldCoordinates = this.indexToPhysical(width, height);
				float value = gaussian2D(worldCoordinates[0], worldCoordinates[1]);				
				this.setAtIndex(width, height, value);
			}
		}	
	}
	
	//Compute Value of the 2D Gaussian for the given position
	private float gaussian2D (double x, double y){		
		
		double exponent = -0.5*(Math.pow((x - this.meanValue[0])/this.standardDeviation[0], 2) + Math.pow((y - this.meanValue[1])/this.standardDeviation[1], 2));
		double gaussianValue = Math.exp(exponent);
		gaussianValue *= Math.sqrt(2*Math.PI)*this.standardDeviation[0]*this.standardDeviation[1];
		return (float)gaussianValue;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		
		//Parameters for all methods
		//GaussianBlob
		int imageWidth = 512;
		int imageHeight = 512;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {30.0d,-30.0d};		
		double [] standardDeviation = {30.d,70.d};
		
		//Projection
		int numberProjections = 251;
		double detectorSpacing = 1.0d;
		int numberPixel = 700;
		
		//create GaussianBlob
		GaussianBlob object = new GaussianBlob(imageWidth,imageHeight,imageSpacing,	meanValue ,standardDeviation);
		object.show("GaussianBlob");		
		
		//create sinogramm of gaussianBlob
		ParallelProjection sinogramm = new ParallelProjection(numberProjections,detectorSpacing,numberPixel);
		sinogramm.createSinogramm(object);
		sinogramm.show("Sinogramm");
		
		//backproject sinogramm
		ParallelBackprojection image = new ParallelBackprojection(object);
		image.filteredBackprojection(sinogramm, detectorSpacing, numberProjections);
		image.show();
		
		Grid2D subtract = new Grid2D(object);		

	}
}
