package edu.stanford.rsl.tutorial.FranziFAU;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class FanBeamReconstruction extends Grid2D {
	
	protected Grid2D fanOGram;
	protected float detectorSize;
	protected float deltaBeta;
	protected float openingAngle;
	protected int projections;

	
	public FanBeamReconstruction(Grid2D image, float dSI, float dSD, int numberProjections, float detectorSpacing, int numberPixel){
		super(image.getWidth(),image.getHeight());
		this.setOrigin(image.getOrigin());
		this.setSpacing(image.getSpacing());
		detectorSize = detectorSpacing*numberPixel;	
		openingAngle = detectorSize / dSD;
		deltaBeta = (float)((2*Math.PI) / numberProjections);		
		projections = numberProjections;
		
	}
	
	public Grid2D fanBeam(Grid2D phantom){
		

		
		for(int indexProjection = 0; indexProjection < projections; indexProjection++){
			float Beta = deltaBeta*indexProjection;
		}
		
		return this;
	}

}
