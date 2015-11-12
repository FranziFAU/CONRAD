package edu.stanford.rsl.tutorial.FranziFAU;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.geometry.shapes.simple.Box;
import edu.stanford.rsl.conrad.geometry.shapes.simple.PointND;
import edu.stanford.rsl.conrad.geometry.shapes.simple.StraightLine;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

public class RadonTransform extends Grid2D{	
	
	protected float detectorSize;
	protected int pixel;
	protected double pixelWidth;
	protected double angleWidthR;
	protected int projections;
	
	public RadonTransform(int numberProjections,float detectorSpacing, int numberPixel){
		// Resultierende Bild
		super(numberPixel,numberProjections);
		
		//Detektorgroessen
		detectorSize = detectorSpacing*numberPixel;
		pixel = numberPixel;
		pixelWidth = ((float)detectorSpacing );		
		angleWidthR = (Math.PI / numberProjections);	
		projections = numberProjections;
		this.setSpacing(detectorSpacing,angleWidthR);
		this.setOrigin(-detectorSize/2,0);
	}
		
	
	public Grid2D createSinogramm(Grid2D image) {
		
		// Bouding Box erstellen		
		Box imageBox = new Box(image.getWidth()*image.getSpacing()[0],image.getHeight()*image.getSpacing()[1],2.0d);
		imageBox.setLowerCorner(new PointND(image.getOrigin()[0],-image.getOrigin()[1],-1.0));
		imageBox.setUpperCorner(new PointND(-image.getOrigin()[0],image.getOrigin()[1],1.0));
		
		for(int i = 142; i < image.getHeight(); i++){
			for(int j = 0; j < image.getWidth(); j++){
				if(i == 0 || i == (image.getHeight()-1) || j == 0 || j == (image.getWidth()-1)){
					double[] index = image.indexToPhysical(j, i);
					PointND check = new PointND(index[0],index[1], 1);
					if(!(imageBox.isMember(check))){
						System.out.println("(" + i + "/" + j + ")" + "("+ index[0]+ "/" +index[1]+ ")");
					}
				}
			}
		}

		
		//ueber die einzelnen Projektionen laufen
		for(int indexProjections = 0; indexProjections < projections; indexProjections++){
			// Entlang des Detektors laufen
			for(int indexDetektor = 0; indexDetektor < pixel; indexDetektor++){
				
				// Projektorlinien bestimmen				
				double [] indexWorld = this.indexToPhysical(indexDetektor, indexProjections);				
				
				double angle = angleWidthR * indexProjections;
				double s = indexWorld[0];
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);
								
								
				PointND p1 = new PointND(cos*s,sin*s,0.0d);
				PointND p2 = new PointND(cos*s - sin, sin*s + cos, 0.0d);
				
				
				StraightLine line = new StraightLine(p1,p2);	
				
				
				
				//Schnittpunkte von Box und Gerade berechnen
									
				ArrayList<PointND> crossingPoints = imageBox.intersect(line);

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
						
						//Linienintegral
						for(double k = 0; k < (distance); k=k+delta){
							
							double indexX = c1.get(0) + k*(richtung.get(0));
							double indexY = c1.get(1) + k*(richtung.get(1));							
																					
							double [] indexImage = image.physicalToIndex(indexX, indexY);			
		
							val += InterpolationOperators.interpolateLinear(image,indexImage[0] , indexImage[1]);		

						}
						
						this.setAtIndex(indexDetektor, indexProjections, (float)(val*delta));
				}
			
			}
		}
		
	
		
		
		return this;
	}
	

}
