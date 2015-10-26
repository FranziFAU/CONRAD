package edu.stanford.rsl.tutorial.FranziFAU;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;

public class MyPhantom extends Grid2D{

	public MyPhantom(int width, int height){
		
		super(width,height);
		
		
		int midw = width / 2;
		int midh = height / 2;
		
		// Dreieck malen
		int currentWidth = width/5;
		
		for(int i = (height / 10); i < (midh - 3); i++){
			for(int j = 0; j < width; j++){
				if(j > (midw - (currentWidth/2)) && j < (midw + (currentWidth/2)) && currentWidth >= 1){
					this.setAtIndex(j,i,0.5f);
				}
				
			}
			currentWidth -= 2;
		}	
		
		// Quadrat malen		

		for(int i = midh ; i < height; i++){
			for(int j = width/10 ; j < midw -1; j ++){
				if(i < (((3*height)/10)+5 + midh) && j < ((4*width)/10)){
					this.setAtIndex(j, i, 0.25f);
				}
			}			
		}
		
		// Rechteck malen
		
		for(int i = height -6 ; i > midh; i--){
			for(int j = width - 6; j > midw; j --){
				if(j > (width -(((4*width) / 10) + 5)) && i > (height - ((height/ 20) + 5))){
					this.setAtIndex(j, i, 0.75f);
				}
			}
		}
		
		
	}

	private Grid2D radonTransform(Grid2D image,int numberProjections,int detectorSpacing, int detectorPixel){
		Grid2D result = new Grid2D(detectorPixel,numberProjections);
		
		for(int y = 0; y < numberProjections; y++){
			for(int x = 0; x < detectorPixel; x++){
				int startx = 0;
				int starty = 0;
				int endx = 0;
				int endy = 0;
				
				// Integral ueber Gerade
				float resultValue = 0;
				for(int i = startx; i < endx; i++){
					resultValue += InterpolationOperators.interpolateLinear(this,x,y);					
				}
				resultValue /= (endx-startx);
				this.setAtIndex(x, y, resultValue);
				
			}
		}
		
		
		
		
		return result;
	}
	
 	public static void main(String[] args) {
		new ImageJ();
		MyPhantom bild = new MyPhantom(512,512);
		bild.show();
		System.out.println(NumericPointwiseOperators.max(bild));
		NumericPointwiseOperators.sum(bild);
	}	
	
}
