package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;


public class GaussianBlob extends Grid2D {

	protected double meanValueW;
	protected double meanValueH;
	protected double standardDeviationW;
	protected double standardDeviationH;
	
	
	public GaussianBlob(int imageWidth, int imageHeight, double spacingWidth,
			double spacingHeight, double meanValueW,double meanValueH, double standardDeviationW,double standardDeviationH){
		
		// create Image with the desired size
		super(imageWidth, imageHeight);
		this.setSpacing(spacingWidth, spacingHeight);
		this.setOrigin((-((imageWidth)*spacingWidth)/2),(-((imageHeight)*spacingHeight)/2));
		// call Initialization function
		initialise(meanValueW,meanValueH, standardDeviationW,standardDeviationH);
	}
	
	// design Gaussian Blob with the given Parameters
	private void initialise(double meanW,double meanH, double deviationW, double deviationH){
		
		//set Variables
		this.meanValueW = meanW;
		this.meanValueH = meanH;
		this.standardDeviationW = deviationW;
		this.standardDeviationH = deviationH;
		
	
		//Set Pixel Values of the Gaussian
		for(int width = 0; width < this.getWidth(); width ++){
			for(int height = 0; height < this.getHeight(); height ++){
				
				double [] worldCoordinates = this.indexToPhysical(width, height);				
				float value = gaussian2D(worldCoordinates[0], worldCoordinates[1]);				
				this.setAtIndex(width, height, value);
			}
		}	
	}
	
	//Compute Value of the 2D Gaussian for the given Position
	private float gaussian2D (double x, double y){
		double exponent = -0.5*(Math.pow((x-this.meanValueW)/this.standardDeviationW, 2) + Math.pow((y-this.meanValueH)/this.standardDeviationH, 2));
		double gaussianValue = Math.exp(exponent);
		gaussianValue += Math.sqrt(2*Math.PI)*this.standardDeviationW*this.standardDeviationH;
		return (float)gaussianValue;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		
		int imageWidth = 512;
		int imageHeight = 512;
		double imageSpacingW = 1.0d;
		double imageSpacingH = 1.0d;
		double meanValueW = 0.0d;
		double meanValueH = 0.0d;
		double standardDeviationW = 30.d;
		double standardDeviationH = 30.d;
		
		
		int numberProjections = 379;
		double detectorSpacing = 1.0d;
		int numberPixel = 200;
		
		GaussianBlob object = new GaussianBlob(imageWidth,imageHeight,imageSpacingW,imageSpacingH,
				meanValueW,meanValueH,standardDeviationW,standardDeviationH);
		object.show("GaussianBlob");
		
		ParallelProjection sinogramm = new ParallelProjection(numberProjections,detectorSpacing,numberPixel);
		sinogramm.createSinogramm(object);
		sinogramm.show("Sinogramm");
		
		ParallelBackprojection image = new ParallelBackprojection(object);
		image.filteredBackprojection(sinogramm, detectorSpacing, numberProjections);
		image.show();

	}
}
