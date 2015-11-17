package edu.stanford.rsl.tutorial.FranziFAU;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;

public class FilteredBP extends Grid2D {
	
	
	
	public FilteredBP(Grid2D image){
		super(image.getWidth(),image.getHeight());
		this.setOrigin(image.getOrigin());
		this.setSpacing(image.getSpacing());
	
	}
	
	public Grid2D filteredBackProjection(Grid2D sinogramm, float detectorSpacing, int numberProjections, boolean ramLak){
		
		Grid2D filteredSinogramm = new Grid2D(sinogramm);
		
		Grid1DComplex filter = new Grid1DComplex(filteredSinogramm.getSubGrid(0));
		
		for(int i = 0; i < filter.getSize()[0]/2; i ++){
			float val;
			if(i == 0){
				val = 1.f/4.f;
			}else if((i%2) == 0){
				val = 0.f;
			}else{
				val = (-(1.f/(float)(Math.pow(i/detectorSpacing, 2)*Math.pow(Math.PI, 2))));
			}
			filter.setRealAtIndex((filter.getSize()[0]/2)+i, val);
			filter.setRealAtIndex((filter.getSize()[0]/2)-i, val);
			filter.setImagAtIndex((filter.getSize()[0]/2)+i, 0.f);
			filter.setImagAtIndex((filter.getSize()[0]/2)-i, 0.f);
		}
		filter.getRealSubGrid(0,filter.getSize()[0]).show("Filter");		
		filter.transformForward();
		filter.getRealSubGrid(0,filter.getSize()[0]).show("Filter Fourier");
		
		
		
		//Sinogramm zeilenweise auslesen
		for(int i = 0; i < sinogramm.getHeight(); i++){
			Grid1DComplex line_c = new Grid1DComplex(filteredSinogramm.getSubGrid(i));			
			//Linie filtern
			if(ramLak){				
				filteringRamLak(line_c, detectorSpacing,filter);
			}else{
				filtering(line_c,detectorSpacing);
			}
				
			//gefilterte Linie speichern
			for(int j = 0; j < filteredSinogramm.getWidth(); j++){
				filteredSinogramm.setAtIndex(j, i, line_c.getRealAtIndex(j));
			}		
		}			
		filteredSinogramm.show();
		
		backProjection(filteredSinogramm,numberProjections);
		
		return this;
 		
	}
	
	protected Grid1DComplex filteringRamLak (Grid1DComplex line, float spacing, Grid1DComplex filter){
		

		line.transformForward();
		
		for(int index = 0; index < line.getSize()[0]; index++){
		//	float real = 
		//	line.setRealAtIndex
		}
		
			
		line.transformInverse();
		
		return line;
	}
	
	protected Grid1DComplex filtering(Grid1DComplex line, float spacing){
		//FFT der Detektorlinie		
		
		
		line.transformForward();	
		
		// Berechung von frequency spacing
		int k = line.getSize()[0];		
	
		float deltaf = 1/(spacing*k);
		
		// Filter aufstellen
				
		float j = 0;
		for(int i = 0; i < k; i++){
			
			if(i < (k/2)){
				line.setRealAtIndex(i, line.getRealAtIndex(i)*j);
				line.setImagAtIndex(i, line.getImagAtIndex(i)*j);
				j += deltaf;
				
			}else {
				line.setRealAtIndex(i, line.getRealAtIndex(i)*j);
				line.setImagAtIndex(i, line.getImagAtIndex(i)*j);
				j -= deltaf;
			}
			
		}	
		
		line.transformInverse();
	
		return line;
	}
	
	protected void backProjection(Grid2D filteredS, int numberProjections){
		
		double angleWidthR = Math.PI / numberProjections;

		for(int j = 0; j < this.getWidth(); j++){
			for(int k = 0; k < this.getHeight(); k++){
				
				double [] imageWorld = this.indexToPhysical(j, k);		
				
				for(int i = 0; i < numberProjections; i ++){
										
					double s = (imageWorld[0]*Math.cos(angleWidthR*i) + imageWorld[1]*Math.sin(angleWidthR*i));
					double [] sinoIndex = filteredS.physicalToIndex(s, i);
					float val =InterpolationOperators.interpolateLinear(filteredS, sinoIndex[0], i);
					this.setAtIndex(j, k, this.getAtIndex(j,k) + val);
				}
				
			}
			
		}
			
	}

}
