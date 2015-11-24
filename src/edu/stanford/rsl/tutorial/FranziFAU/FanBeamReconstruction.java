package edu.stanford.rsl.tutorial.FranziFAU;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;

public class FanBeamReconstruction extends Grid2D {
	
	protected Grid2D fanOGram;
	protected float detectorSize;
	protected float deltaBeta;
	protected float openingAngle;
	protected int projections;
	protected float dSI;
	protected float dSD;
	protected int numberPixel;

	
	public FanBeamReconstruction( float dsi, float dsd, int numberProjections, float detectorSpacing, int numberOfPixel, float projectionAngle){
		super(numberOfPixel,numberProjections);

		detectorSize = detectorSpacing*numberPixel;	
		openingAngle = detectorSize / dSD;
		deltaBeta = projectionAngle / numberProjections;		
		projections = numberProjections;
		dSI = dsi;
		dSD = dsd;
		numberPixel = numberOfPixel;
		this.setOrigin(-detectorSize/2,0);
		this.setSpacing(detectorSpacing,deltaBeta);
	}
	
	public Grid2D fanBeam(Grid2D phantom){
		
		Box imageBox = new Box(phantom.getWidth()*phantom.getSpacing()[0],phantom.getHeight()*phantom.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(phantom.getOrigin()[0],-phantom.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-phantom.getOrigin()[0],phantom.getOrigin()[1],1.0));

		
		for(int indexProjection = 0; indexProjection < projections; indexProjection++){
			float Beta = deltaBeta*indexProjection;
			float sin = (float) Math.sin(Beta + (Math.PI/2));
			float cos = (float) Math.cos(Beta + (Math.PI/2));
			
			PointND source = new PointND(sin*dSI,cos*dSI);			
			
			for(int indexT = 0; indexT < numberPixel; indexT++){
				
			}
		}
		
		return this;
	}

}
