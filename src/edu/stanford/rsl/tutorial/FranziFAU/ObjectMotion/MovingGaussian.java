package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class MovingGaussian extends GaussianBlob {
	
	private double frequency;
	
	public MovingGaussian (int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue, double [] standardDeviation, double frequency){
		//create GaussianBlob
		super(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);		
		this.frequency = frequency;
	}
	
	public Grid2D moveGaussian(){
		return this;
	}

}
