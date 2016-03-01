package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class MovingGaussian extends GaussianBlob {
	
	private double frequency; // how often per projection
	private double [] changedMeanValue;
	private double [] changedStandardDeviation;
	private int counter;	
	private SimpleVector currentPosition;
	private SimpleVector originalPosition;
	private SimpleVector changedPosition;
	private SimpleVector direction;
	private double stepsize;
	
	public MovingGaussian (int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue,
			double [] standardDeviation, double frequency, double [] changedMeanValue, double [] changedStandardDeviation){
		
		//create GaussianBlob
		super(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);		
		this.frequency = frequency % (Math.floor(frequency));
		this.changedMeanValue = changedMeanValue;
		this.changedStandardDeviation = changedStandardDeviation;
		this.counter = 0;
		this.originalPosition = new SimpleVector(meanValue[0],meanValue[1],standardDeviation[0],standardDeviation[1]);
		this.currentPosition = originalPosition;
		this.changedPosition = new SimpleVector(changedMeanValue[0],changedMeanValue[1],changedStandardDeviation[0],changedStandardDeviation[1]);
		
		computeSteps();
	
	}	
	
	private void computeSteps(){
		
		this.direction = changedPosition;
		this.direction.subtract(originalPosition);
		double length = 2 * direction.normL2();
		this.stepsize = length * this.frequency;
		this.direction.normalizedL2();
		
	}
	
	public void setFrequency(double frequency){
		this.frequency = frequency % (Math.floor(frequency));
		computeSteps();
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

	
	//move Gaussian in each projection step	
	public Grid2D moveGaussian(){		
		
	//	currentPosition.add(direction.multipliedBy(stepsize));
		
		if(counter % 2 == 0){
			initializeGauss(this, this.changedMeanValue, this.changedStandardDeviation);
		}else{
			initializeGauss(this, this.meanValue, this.standardDeviation);
		}
		
		counter = (counter + 1) % 2;
		return this;
	}
	

	
	public static void main(String[] args) {
		new ImageJ();		
		int imageWidth = 128;
		int imageHeight = 128;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {10.0d,-10.0d};		
		double [] standardDeviation = {20.d,5.d};
		double frequency = 0.5d;		
		double [] newmeanValue = {-10.0d,40.0d};		
		double [] newstandardDeviation = {20.d,5.d};
		
		MovingGaussian gauss = new MovingGaussian(imageWidth,imageHeight,imageSpacing,	meanValue ,standardDeviation, frequency,newmeanValue, newstandardDeviation);
		
		gauss.show("ursprünglich");	
		gauss.moveGaussian();
		gauss.show("bewegt");

	}

}
