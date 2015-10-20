package edu.stanford.rsl.tutorial.FranziFAU;

import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

public class MyPhantom extends Grid2D{

	public MyPhantom(int width, int height){
		
		super(width,height);
		
		
		int midw = width / 2;
		int midh = height / 2;
		
		// Dreieck malen
		int currentWidth = width/5;
		
		for(int i = 2; i < (midh - 3); i++){
			for(int j = 0; j < width; j++){
				if(j > (midw - (currentWidth/2)) && j < (midw + (currentWidth/2)) && currentWidth >= 1){
					this.setAtIndex(j,i,0.5f);
				}
				
			}
			currentWidth -= 2;
		}
		
		
		// Quadrat malen

		

		for(int i = 0; i < midh; i++){
			for(int j = 0 ; j < midw -1; j ++){
				//if(j >  ){                           ){
			//		this.setAtIndex(i,j,0.25f);
			//	}				
				
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		new ImageJ();
		MyPhantom bild = new MyPhantom(512,512);
		bild.show();
	}	
	
}
