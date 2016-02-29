package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class GaussianBlob extends Grid2D {

	protected double meanValueW;
	protected double meanValueH;
	protected double standardDeviationW;
	protected double standardDeviationH;
	protected double frequency;
	
	public GaussianBlob(int imageWidth, int imageHeight, double spacingWidth, double spacingHeight, double meanValueW,double meanValueH, double standardDeviationW,double standardDeviationH, double frequency){
		
		super(imageWidth, imageHeight);
		this.setSpacing(spacingWidth, spacingHeight);
		this.setOrigin((-((imageWidth-1)*spacingWidth)/2),(-((imageHeight-1)*spacingHeight)/2));
		initialise(meanValueW,meanValueH, standardDeviationW,standardDeviationH, frequency);
	}
	
	private void initialise(double meanW,double meanH, double deviationW, double deviationH, double frequency){
		this.meanValueW = meanW;
		this.meanValueH = meanH;
		this.standardDeviationW = deviationW;
		this.standardDeviationH = deviationH;
		this.frequency = frequency;
	
		for(int width = 0; width < this.getWidth(); width ++){
			for(int height = 0; height < this.getHeight(); height ++){
				
				double [] worldCoordinates = this.indexToPhysical(width, height);				
				float value = gaussian2D(worldCoordinates[0], worldCoordinates[1]);				
				this.setAtIndex(width, height, value);
			}
		}	
	}
	
	private float gaussian2D (double x, double y){
		double exponent = -0.5*(Math.pow((x-this.meanValueW)/this.standardDeviationW, 2) + Math.pow((y-this.meanValueH)/this.standardDeviationH, 2));
		double gaussianValue = Math.exp(exponent);
		gaussianValue += Math.sqrt(2*Math.PI)*this.standardDeviationW*this.standardDeviationH;
		return (float)gaussianValue;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		GaussianBlob object = new GaussianBlob(512,512,1.0d,1.0d, 0.0d,0.0d,30.0d,30.0d,1.0);
		object.show();
	}
}
