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
		this.initialise(meanValueW,meanValueH, standardDeviationW,standardDeviationH, frequency);
	}
	
	private void initialise(double meanW,double meanH, double deviationW, double deviationH, double frequency){
		this.meanValueW = meanW;
		this.meanValueH = meanH;
		this.standardDeviationW = deviationW;
		this.standardDeviationH = deviationH;
		this.frequency = frequency;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		GaussianBlob object = new GaussianBlob(512,512,1.0d,1.0d, 0.0d,0.0d,1.0d,1.0d,1.0);
		object.show();
	}
}
