package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import java.util.ArrayList;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;

public class ParallelProjection extends Grid2D{
	
	private int numberPixel;
	private double detectorSize;
	private double detectorSpacing;
	private double angleSpacing;
	private int numberProjections;
	private double timeFactor;
	
	public ParallelProjection(int numberProjections, double detectorSpacing, int numberPixel, double timeFactor){
		//create Grid2D for sinogramm
		super(numberPixel, numberProjections);
		
		// initialize Variables with the given values
		this.numberPixel = numberPixel;
		this.detectorSize = numberPixel*detectorSpacing;
		this.detectorSpacing = detectorSpacing;
		this.angleSpacing = (Math.PI/numberProjections);
		this.numberProjections = numberProjections;
		this.timeFactor = timeFactor;
		
		this.setOrigin(-detectorSize/2,0);
		this.setSpacing(detectorSpacing,angleSpacing);
	}
	
	public Grid2D createSinogramm(Grid2D phantom){
		
	
		Box imageBox = new Box(phantom.getWidth()*phantom.getSpacing()[0],phantom.getHeight()*phantom.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(phantom.getOrigin()[0],phantom.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-phantom.getOrigin()[0],-phantom.getOrigin()[1],1.0));
		
		//walk over each projection
		for(int indexProjections = 0; indexProjections < numberProjections; indexProjections++){
			// walk along the detector			
			
			for(int indexDetektor = 0; indexDetektor < numberPixel; indexDetektor++){
				
				
				
				// define the parallel lines of the detector				
				double [] indexWorld = this.indexToPhysical(indexDetektor, indexProjections);				
				
				double angle = angleSpacing * indexProjections;
				double s = indexWorld[0];
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
								
								
				PointND p1 = new PointND(cos*s, sin*s,0.0d);
				PointND p2 = new PointND(cos*s - sin, sin*s + cos, 0.0d);
				
				
				StraightLine line = new StraightLine(p1,p2);				
				
				
				//intersection of the box and the line
									
				ArrayList<PointND> crossingPoints = imageBox.intersect(line);
				
				//if there is no intersection -> change direction of the line and look again
				if(crossingPoints.size() == 0){
					p2 = new PointND(cos*s + sin, sin*s - cos, 0.0d);
					line = new StraightLine(p1,p2);
					crossingPoints = imageBox.intersect(line);
				}

				if(crossingPoints.size() == 2){
						PointND c1 = crossingPoints.get(0);						
						PointND c2 = crossingPoints.get(1);					

						//compute distance between intersectoin points
						double distance = c1.euclideanDistance(c2);

						// define the direction of the line
						double deltax = (c2.get(0)-c1.get(0))/(distance);
						double deltay = (c2.get(1)-c1.get(1))/(distance);
						double deltaz = (c2.get(2)-c1.get(2))/(distance);				
						
						PointND richtung = new PointND(deltax,deltay,deltaz);
						//result value at the current position in the sinogramm
						float val = 0.f;
						// stepsize of the integral
						double delta = 0.5; // in mm
						//line integral
						for(double k = 0; k < (distance); k=k+delta){
							
							double indexX = c1.get(0) + k*(richtung.get(0));
							double indexY = c1.get(1) + k*(richtung.get(1));							
																					
							double [] indexImage = phantom.physicalToIndex(indexX, indexY);			
		
							val += InterpolationOperators.interpolateLinear(phantom, indexImage[0], indexImage[1]);		

						}
						
						this.setAtIndex(indexDetektor, indexProjections, (float)(val*delta));
				}
			
			}
		}
		
		Grid2D result = new Grid2D(this);
		
		return result;
	}
	
	
public Grid2D createSinogrammMoving(MovingGaussian phantom){		
		
	
		Box imageBox = new Box(phantom.getWidth()*phantom.getSpacing()[0],phantom.getHeight()*phantom.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(phantom.getOrigin()[0],phantom.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-phantom.getOrigin()[0],-phantom.getOrigin()[1],1.0));
		
		//walk over each projection
		for(int indexProjections = 0; indexProjections < numberProjections; indexProjections++){
			// walk along the detector
			phantom.moveGaussian(indexProjections * this.timeFactor);
		
			for(int indexDetektor = 0; indexDetektor < numberPixel; indexDetektor++){
				
			
				
				// define the parallel lines of the detector				
				double [] indexWorld = this.indexToPhysical(indexDetektor, indexProjections);				
				
				double angle = angleSpacing * indexProjections;
				double s = indexWorld[0];
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
								
								
				PointND p1 = new PointND(cos*s, sin*s,0.0d);
				PointND p2 = new PointND(cos*s - sin, sin*s + cos, 0.0d);
				
				
				StraightLine line = new StraightLine(p1,p2);				
				
				
				//intersection of the box and the line
									
				ArrayList<PointND> crossingPoints = imageBox.intersect(line);
				
				//if there is no intersection -> change direction of the line and look again
				if(crossingPoints.size() == 0){
					p2 = new PointND(cos*s + sin, sin*s - cos, 0.0d);
					line = new StraightLine(p1,p2);
					crossingPoints = imageBox.intersect(line);
				}

				if(crossingPoints.size() == 2){
						PointND c1 = crossingPoints.get(0);						
						PointND c2 = crossingPoints.get(1);					

						//compute distance between intersectoin points
						double distance = c1.euclideanDistance(c2);

						// define the direction of the line
						double deltax = (c2.get(0)-c1.get(0))/(distance);
						double deltay = (c2.get(1)-c1.get(1))/(distance);
						double deltaz = (c2.get(2)-c1.get(2))/(distance);				
						
						PointND richtung = new PointND(deltax,deltay,deltaz);
						//result value at the current position in the sinogramm
						float val = 0.f;
						// stepsize of the integral
						double delta = 0.5; // in mm
						//line integral
						for(double k = 0; k < (distance); k=k+delta){
							
							double indexX = c1.get(0) + k*(richtung.get(0));
							double indexY = c1.get(1) + k*(richtung.get(1));							
																					
							double [] indexImage = phantom.physicalToIndex(indexX, indexY);			
		
							val += InterpolationOperators.interpolateLinear(phantom, indexImage[0], indexImage[1]);		

						}
						
						this.setAtIndex(indexDetektor, indexProjections, (float)(val*delta));
				}
			
			}
		}
		
		Grid2D result = new Grid2D(this);
		
		return result;
	}
}
	

