package edu.stanford.rsl.tutorial.FranziFAU.FlatPanelReconstruction;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class RadonTransform extends Grid2D{	
	
	private float detectorSize;
	private int pixel;
	private double pixelWidth;
	private double angleWidthR;
	private int projections;
	
	public RadonTransform(int numberProjections,float detectorSpacing, int numberPixel){
		// result image (sinogramm)
		super(numberPixel,numberProjections);
		
		//detector parameter
		detectorSize = detectorSpacing*numberPixel;
		pixel = numberPixel;
		pixelWidth = ((float)detectorSpacing );		
		angleWidthR = (Math.PI / numberProjections);	
		projections = numberProjections;
		this.setSpacing(detectorSpacing,angleWidthR);
		this.setOrigin(-detectorSize/2,0);
	}
		
	
	public Grid2D createSinogramm(Grid2D phantom, boolean filter) {
		
		// Bouding Box 	
		Box imageBox = new Box(phantom.getWidth()*phantom.getSpacing()[0],phantom.getHeight()*phantom.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(phantom.getOrigin()[0],-phantom.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-phantom.getOrigin()[0],phantom.getOrigin()[1],1.0));
		
		//walk over each projection
		for(int indexProjections = 0; indexProjections < projections; indexProjections++){
			// walk along the detector
			for(int indexDetektor = 0; indexDetektor < pixel; indexDetektor++){
				
				// define the parallel lines of the detector				
				double [] indexWorld = this.indexToPhysical(indexDetektor, indexProjections);				
				
				double angle = angleWidthR * indexProjections;
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
		
		if(filter == true){
			filtering(this);
		}
		
		
		return this;
	}
	
	
	private void filtering(Grid2D sinogramm){
		//construction of the ram lak filter
		Grid1DComplex filter = new Grid1DComplex(sinogramm.getSubGrid(0));
		
		//watch out x-axes is from 0 to - infinity and then from infinity to 0!
		for(int n = 0; n < filter.getSize()[0]/2; n++){
			float val;
			if(n == 0){
				val = (float)(1.f/(4.f*pixelWidth));
				filter.setRealAtIndex(n, val);
				filter.setImagAtIndex(n, 0.f);
			}else if((n%2) == 0){
				val = 0.f;
			}else{
				val = (-(1.f/(float)(Math.pow(n*pixelWidth, 2)*Math.pow(Math.PI, 2))));
			}
			
			if(n != 0){
				filter.setRealAtIndex(n, val);
				filter.setImagAtIndex(n, 0.f);
				filter.setRealAtIndex(filter.getSize()[0]-n, val);
				filter.setImagAtIndex(filter.getSize()[0]-n, 0.f);
			}

		}
		
		filter.transformForward();
		
		for(int s = 0; s < sinogramm.getHeight(); s++){
			Grid1DComplex line_c = new Grid1DComplex(sinogramm.getSubGrid(s));			
			//filter the line
			
			line_c.transformForward();
			
			// multiply line with filter
			for(int index = 0; index < line_c.getSize()[0]; index++){
				
				Complex lineVal = new Complex(line_c.getRealAtIndex(index),line_c.getImagAtIndex(index));
				Complex filterVal = new Complex(filter.getRealAtIndex(index),filter.getImagAtIndex(index));
				
				Complex result = lineVal.mul(filterVal);
				line_c.setRealAtIndex(index, (float)result.getReal());
				line_c.setImagAtIndex(index, (float)result.getImag());
			}

			// IFT of the detector line
			line_c.transformInverse();

				
			//save filtered line
			for(int indexWidth = 0; indexWidth < sinogramm.getWidth(); indexWidth++){
				sinogramm.setAtIndex(indexWidth, s, line_c.getRealAtIndex(indexWidth));
			}		
		}
	}
	

}
