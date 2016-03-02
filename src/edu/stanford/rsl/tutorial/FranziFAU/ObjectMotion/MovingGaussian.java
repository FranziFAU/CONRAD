package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class MovingGaussian extends GaussianBlob {
	
	private double frequency; // how often per projection
	private double [] changedMeanValue;
	private double [] changedStandardDeviation;	
	private SimpleVector midpoint;	
	private SimpleVector direction;
	
	
	public MovingGaussian (int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue,
			double [] standardDeviation, double frequency, double [] changedMeanValue, double [] changedStandardDeviation){
		
		//create GaussianBlob
		super(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);		
		this.frequency = frequency;
		this.changedMeanValue = changedMeanValue;
		this.changedStandardDeviation = changedStandardDeviation;
		
		this.midpoint = new SimpleVector(meanValue[0],meanValue[1],standardDeviation[0],standardDeviation[1]);
		this.midpoint.add(new SimpleVector(changedMeanValue[0],changedMeanValue[1],changedStandardDeviation[0],changedStandardDeviation[1]));
		this.midpoint.multiplyBy(0.5);
		this.direction = new SimpleVector(meanValue[0],meanValue[1],standardDeviation[0],standardDeviation[1]);
		this.direction.subtract(midpoint);
	}	
	

	
	public void setFrequency(double frequency){
		this.frequency = frequency;
	}
	
	public double getFrequency(){
		return this.frequency;
	}
	
	public double [] getChangedMeanValue(){
		return this.changedMeanValue;
	}
	
	public double [] getChangedStandardDeviation(){
		return this.changedStandardDeviation;
	}

	
	//move Gaussian in each projection step, each Projections needs a certain time (in seconds)	
	public Grid2D moveGaussian(double time){
		
		//compute phase of the Object:
		double argument = 2 * Math.PI * this.frequency * time;
		double phase = Math.cos(argument);		
		
		// compute the current Position of the moving Object
		// multiply the argument with 0.5 to stretch the sin that it is 1 at the Position PI. That way it is easier to multiply with the direction.
		SimpleVector currentPosition = direction.multipliedBy(phase);
		currentPosition.add(midpoint);
		
		double [] mean = {currentPosition.getElement(0),currentPosition.getElement(1)};
		double [] deviation = {currentPosition.getElement(2),currentPosition.getElement(3)};		
		
		initializeGauss(this, mean, deviation);
		
		return this;
	}
	

	
	public static void main(String[] args) {
		new ImageJ();		
		int imageWidth = 128;
		int imageHeight = 128;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {5.d,5.d};
		double frequency = 1.d;		
		double [] newmeanValue = {-10.0d,0.0d};		
		double [] newstandardDeviation = {5.d,5.d};
		
		MovingGaussian gauss = new MovingGaussian(imageWidth,imageHeight,imageSpacing,	meanValue ,standardDeviation, frequency,newmeanValue, newstandardDeviation);
		
		gauss.show("ursprünglich");	
		gauss.moveGaussian(0.7);
		gauss.show("bewegt");

	}

}
