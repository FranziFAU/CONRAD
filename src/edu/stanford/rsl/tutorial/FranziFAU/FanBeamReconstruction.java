package edu.stanford.rsl.tutorial.FranziFAU;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.numerics.DecompositionSVD;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;
import edu.stanford.rsl.conrad.numerics.Solvers;

public class FanBeamReconstruction extends Grid2D {
	
	protected Grid2D fanOGram;
	protected float detectorSize;
	protected float deltaBeta;
	protected float openingAngle;
	protected int projections;
	protected float dSI;
	protected float dSD;
	protected int numberPixel;
	protected float detectorSpacing;
	
	public FanBeamReconstruction( float dsi, float dsd, int numberProjections, float detectorspacing, int numberOfPixel, float projectionAngle){
		super(numberOfPixel,numberProjections);

		detectorSize = detectorSpacing*numberPixel;	
		openingAngle = (float)(2.f*Math.atan(((detectorSize/2.d)/dsd)));
		deltaBeta = projectionAngle / numberProjections;		
		projections = numberProjections;
		dSI = dsi;
		dSD = dsd;
		numberPixel = numberOfPixel;
		detectorSpacing = detectorspacing; 
		this.setOrigin(-detectorSize/2,0);
		this.setSpacing(detectorSpacing,deltaBeta);
	}
	
	public Grid2D fanBeam(Grid2D phantom){
		
		Box imageBox = new Box(phantom.getWidth()*phantom.getSpacing()[0],phantom.getHeight()*phantom.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(phantom.getOrigin()[0],-phantom.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-phantom.getOrigin()[0],phantom.getOrigin()[1],1.0));

		
		for(int indexProjection = 1; indexProjection < projections; indexProjection++){
			
			// Source position
			float Beta = deltaBeta*indexProjection;
			float sin = (float) Math.sin(Beta + (Math.PI/2));
			float cos = (float) Math.cos(Beta + (Math.PI/2));
			
			PointND sourcePosition = new PointND(sin*dSI,cos*dSI,0.0);
			SimpleVector source = sourcePosition.getAbstractVector();
			
			// Direction of the detector
			SimpleVector eD = new SimpleVector(-sourcePosition.get(1), sourcePosition.get(0), 0.0);	
			eD.normalizeL2();		
			eD.multiplyBy(detectorSpacing);
			
			//Direction from source to detector
			SimpleVector eISO = sourcePosition.getAbstractVector();
			eISO.negate();
			eISO.normalizeL2();
			eISO.multiplyBy(dSD);			
	
			for(int indexT = 0; indexT < numberPixel; indexT++){
				
				
				int indexPosition = indexT - ((numberPixel)/2);
				SimpleVector eDirection = eD.multipliedBy(indexPosition);
				//determine current position on the detector
				SimpleVector result = SimpleOperators.add(source,eISO,eDirection);				
				PointND detectorPosition = new PointND(result.getElement(0),result.getElement(1),0.0);
				
				// line between source and position on detector
				StraightLine line = new StraightLine(sourcePosition,detectorPosition);
				
				// find intersection with bounding box
				ArrayList<PointND> crossingPoints = imageBox.intersect(line);
				if(crossingPoints.size()==0){
					PointND detectorPositionNew = new PointND(-result.getElement(0),-result.getElement(1),0.0);
					line = new StraightLine(sourcePosition,detectorPositionNew);
					crossingPoints = imageBox.intersect(line);
				}
				
				if(crossingPoints.size() == 2){

					PointND c1 = crossingPoints.get(0);						
					PointND c2 = crossingPoints.get(1);					

					double distance = c1.euclideanDistance(c2);
					double delta = 0.5; // in mm
					// Richtungsvektor bestimmen
					double deltax = (c2.get(0)-c1.get(0))/(distance);
					double deltay = (c2.get(1)-c1.get(1))/(distance);
					double deltaz = (c2.get(2)-c1.get(2))/(distance);				
				
					PointND richtung = new PointND(deltax,deltay,deltaz);
				
					float val = 0.f;
				
					//Line integral
					for(double k = 0; k < (distance); k=k+delta){
					
						double indexX = c1.get(0) + k*(richtung.get(0));
						double indexY = c1.get(1) + k*(richtung.get(1));							
																			
						double [] indexImage = phantom.physicalToIndex(indexX, indexY);			
						
						val += InterpolationOperators.interpolateLinear(phantom, indexImage[0], indexImage[1]);		
						
					}
					this.setAtIndex(indexT, indexProjection, (float)(val*delta));
				}
				
			}
		}
		this.show();
		
		return this;
	}

	public Grid2D rebinning(){
		Grid2D sinogramm = new Grid2D(this.getWidth(),this.getHeight());
		sinogramm.setOrigin(this.getOrigin());
		
		for(int indexDetector = 0; indexDetector < numberPixel; indexDetector++){
			for(int indexBeta = 0; indexBeta < projections; indexBeta++){
			
			}
		}
		return sinogramm;
		
	}
	
}