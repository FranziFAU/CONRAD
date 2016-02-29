package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class ParallelProjection extends Grid2D{
	
	private int numberPixel;
	private float detectorSize;
	private double detectorSpacing;
	private double angleSpacing;
	private int numberProjections;
	
	public ParallelProjection(int numberProjections, float detectorSpacing, int numberPixel){
		super(numberPixel, numberProjections);
		
		this.numberPixel = numberPixel;
		this.detectorSize = numberPixel*detectorSpacing;
		this.detectorSpacing = detectorSpacing;
		this.angleSpacing = (Math.PI/numberProjections);
		this.numberProjections = numberProjections;
		this.setOrigin(-detectorSize/2,0);
		this.setSpacing(detectorSpacing,angleSpacing);
	}
	
}
