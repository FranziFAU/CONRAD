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
		openingAngle = detectorSize / dSD;
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

		
		for(int indexProjection = 0; indexProjection < projections; indexProjection++){
			
			// Source position
			float Beta = deltaBeta*indexProjection;
			float sin = (float) Math.sin(Beta + (Math.PI/2));
			float cos = (float) Math.cos(Beta + (Math.PI/2));
			
			PointND source = new PointND(sin*dSI,cos*dSI,0.0);
			SimpleVector s = source.getAbstractVector();
			SimpleVector eISO = source.getAbstractVector();
			eISO.negate();
			eISO.normalizeL2();
			eISO.multiplyBy(dSD);
			
			// Direction of the detector
			SimpleMatrix A = new SimpleMatrix(2,2);
			A.setElementValue(0, 0, sin*dSI);
			A.setElementValue(0, 1, cos*dSI);
			
			DecompositionSVD svd = new DecompositionSVD(A);		
			
			SimpleVector b = new SimpleVector(0,0);
			SimpleVector eD2 = SimpleOperators.multiply(svd.inverse(true), b);
			eD2.normalizeL2();		
			SimpleVector eD = new SimpleVector(eD2.getElement(0),eD2.getElement(1),0.f);
			
			for(int indexT = 0; indexT < numberPixel; indexT++){
				
				eD.multiplyBy(detectorSpacing);
				int indexPosition = indexT - ((numberPixel-1)/2);
				eD.multiplyBy(indexPosition);
				//determine current position on the detector
				SimpleVector result = SimpleOperators.add(s,eISO,eD);				
				PointND detectorPosition = new PointND(result.getElement(0),result.getElement(1),0.0);
				
				// line between source and position on detector
				StraightLine line = new StraightLine(source,detectorPosition);
				
				// find intersection with bounding box
				ArrayList<PointND> crossingPoints = imageBox.intersect(line);
				if(crossingPoints.size() == 2){
					System.out.println("cross");
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

}
