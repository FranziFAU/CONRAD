package edu.stanford.rsl.tutorial.FranziFAU;

import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
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
		
		// gefiltertes Sinogramm 
		Grid2D filteredSinogramm = new Grid2D(sinogramm);
		
		//Filter fuer Ram-Lak
		Grid1DComplex filter = new Grid1DComplex(filteredSinogramm.getSubGrid(0));
		
		for(int n = 0; n < filter.getSize()[0]/2; n++){
			float val;
			if(n == 0){
				val = (1.f/(4.f*detectorSpacing));
				filter.setRealAtIndex(n, val);
				filter.setImagAtIndex(n, 0.f);
			}else if((n%2) == 0){
				val = 0.f;
			}else{
				val = (-(1.f/(float)(Math.pow(n*detectorSpacing, 2)*Math.pow(Math.PI, 2))));
			}
			
			if(n != 0){
				filter.setRealAtIndex(n, val);
				filter.setImagAtIndex(n, 0.f);
				filter.setRealAtIndex(filter.getSize()[0]-n, val);
				filter.setImagAtIndex(filter.getSize()[0]-n, 0.f);
			}

		}
		
		filter.transformForward();	
			
		
		//Sinogramm zeilenweise auslesen
		for(int s = 0; s < sinogramm.getHeight(); s++){
			Grid1DComplex line_c = new Grid1DComplex(filteredSinogramm.getSubGrid(s));			
			//Linie filtern
			if(ramLak){
				filteringRamLak(line_c,filter);
			}else{
				filtering(line_c,detectorSpacing);
			}
				
			//gefilterte Linie speichern
			for(int indexWidth = 0; indexWidth < filteredSinogramm.getWidth(); indexWidth++){
				filteredSinogramm.setAtIndex(indexWidth, s, line_c.getRealAtIndex(indexWidth));
			}		
		}			
		if(ramLak){
//			filteredSinogramm.show("RamLak");
		}else{
//			filteredSinogramm.show("Ramp Filter");
		}
		
		//Rueckprojektion
		backProjection(filteredSinogramm,numberProjections);
		
		return this;
 		
	}
	
	protected Grid1DComplex filteringRamLak (Grid1DComplex line, Grid1DComplex filter){		
		//FFT der Detektorlinie		
		line.transformForward();
		
		// Linie mit Filter Multiplizieren		
		for(int index = 0; index < line.getSize()[0]; index++){
			
			Complex lineVal = new Complex(line.getRealAtIndex(index),line.getImagAtIndex(index));
			Complex filterVal = new Complex(filter.getRealAtIndex(index),filter.getImagAtIndex(index));
			
			Complex result = lineVal.mul(filterVal);
			line.setRealAtIndex(index, (float)result.getReal());
			line.setImagAtIndex(index, (float)result.getImag());
		}

		// IFT der Detektorlinie	
		line.transformInverse();
		
		return line;
	}
	
	protected Grid1DComplex filtering(Grid1DComplex line, float spacing){
		
		
		//FFT der Detektorlinie		
		line.transformForward();	
		
		// Berechung von frequency spacing
		int k = line.getSize()[0];		
	
		float deltaf = 1.f/(spacing*k);
		
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
		
		// IFT der Detektorlinie
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
