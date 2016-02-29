package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class MovingGaussian extends GaussianBlob {
	
	private double frequency; // how often per projection
	private double [] newMeanValue;
	private double [] newStandardDeviation;
	
	public MovingGaussian (int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue,
			double [] standardDeviation, double frequency, double [] newMeanValue, double [] newStandardDeviation){
		//create GaussianBlob
		super(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);		
		this.frequency = frequency;
		this.newMeanValue = newMeanValue;
		this.newStandardDeviation = newStandardDeviation;
	}
	
	//move Gaussian in each projection step
	public Grid2D moveGaussian(){
		
		initialize(newMeanValue, newStandardDeviation);
		return this;
	}
	
	public static void main(String[] args) {
		new ImageJ();
		
		int imageWidth = 128;
		int imageHeight = 128;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {10.0d,-10.0d};		
		double [] standardDeviation = {20.d,30.d};
		
		double [] newmeanValue = {-10.0d,10.0d};		
		double [] newstandardDeviation = {50.d,0.d};
		
		MovingGaussian gauss = new MovingGaussian(imageWidth,imageHeight,imageSpacing,	meanValue ,standardDeviation, 1.0d,newmeanValue, newstandardDeviation);
		gauss.show();
		gauss.moveGaussian();
		gauss.show();
		
	}

}
