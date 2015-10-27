package edu.stanford.rsl.tutorial.FranziFAU;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;

public class RadonTransform extends Grid2D{	
	
	protected int spacing;
	protected int pixel;
	protected double pixelWidth;
	protected double angleWidthR;
	protected int projections;
	
	public RadonTransform(int numberProjections,int detectorSpacing, int numberPixel){
		// Resultierende Bild
		super(numberPixel,numberProjections);
		//Detektorgroessen
		spacing = detectorSpacing;
		pixel = numberPixel;
		pixelWidth = (detectorSpacing / numberPixel);
		angleWidthR = (Math.PI / numberProjections);	
		projections = numberProjections;
	}
	
	
	
	public Grid2D createSinogramm(Grid2D image){		
		//ueber die einzelnen Projektionen laufen
		for(int y = 0; y < projections; y++){
			// Entlang des Detektors laufen
			for(int x = 0; x < pixel; x++){
				// Projektorlinien bestimmen
				double angle = angleWidthR * y;
				double s = spacing - (x*pixelWidth) - ((spacing-1)/2)*pixelWidth;
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
				
				PointND p1 = new PointND(cos*s,sin*s,0.0d);
				PointND p2 = new PointND(cos*s - sin, sin*s + cos, 0.0d);
				
				StraightLine line = new StraightLine(p1,p2);
				
				// Bounding Box des Bildes
				
				
				
				
				
				
			}
		}
		
		
		
		
		return this;
	}
	

}
