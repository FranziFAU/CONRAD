package edu.stanford.rsl.tutorial.FranziFAU.FlatPanelReconstruction;

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
	
	public Grid2D filteredBackProjection(Grid2D sinogramm, float detectorSpacing, int numberProjections,
			double scanAngle, boolean ramLak){
		
		// filtered sinogramm
		Grid2D filteredSinogramm = new Grid2D(sinogramm);
		
		//construction of the ram lak filter
		Grid1DComplex filter = new Grid1DComplex(filteredSinogramm.getSubGrid(0));
		
		//watch out x-axes is from 0 to - infinity and then from infinity to 0!
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
			
		
		//read out each line of the sinogramm and apply filtering
		for(int s = 0; s < sinogramm.getHeight(); s++){
			
			Grid1DComplex line_c = new Grid1DComplex(filteredSinogramm.getSubGrid(s));			
			//filter the line
			if(ramLak){
				filteringRamLak(line_c,filter);
			}else{
				filtering(line_c,detectorSpacing);
			}
				
			//save filtered line
			for(int indexWidth = 0; indexWidth < filteredSinogramm.getWidth(); indexWidth++){
				filteredSinogramm.setAtIndex(indexWidth, s, line_c.getRealAtIndex(indexWidth));
			}		
		}			
		if(ramLak){
			filteredSinogramm.show("RamLak");
		}else{
//			filteredSinogramm.show("Ramp Filter");
		}
		
		//backprojection
		backProjection(filteredSinogramm,numberProjections,scanAngle);
		
		Grid2D result = new Grid2D(this);
		
		return result;
 		
	}
	
	private Grid1DComplex filteringRamLak (Grid1DComplex line, Grid1DComplex filter){		
		//FFT of the detector line	
		line.transformForward();
		
		// multiply line with filter
		for(int index = 0; index < line.getSize()[0]; index++){
			
			Complex lineVal = new Complex(line.getRealAtIndex(index),line.getImagAtIndex(index));
			Complex filterVal = new Complex(filter.getRealAtIndex(index),filter.getImagAtIndex(index));
			
			Complex result = lineVal.mul(filterVal);
			line.setRealAtIndex(index, (float)result.getReal());
			line.setImagAtIndex(index, (float)result.getImag());
		}

		// IFT of the detector line
		line.transformInverse();
		
		return line;
	}
	
	private Grid1DComplex filtering(Grid1DComplex line, float spacing){
		
		
		//FFT of the detector line
		line.transformForward();	
		
		// compute the frequency spacing
		int k = line.getSize()[0];		
	
		float deltaf = 1.f/(spacing*k);
		
		// set up filter (ramp filter)

		//value of omega at the current position		
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
		
		// IFT of the detector line
		line.transformInverse();
	
		return line;
	}
	
	private void backProjection(Grid2D filteredS, int numberProjections, double scanAngle){
		
		// compute angular spacing
		double angleWidthR = scanAngle / numberProjections;
		// walk over the reconstructed image
		for(int width = 0; width < this.getWidth(); width++){
			for(int height = 0; height < this.getHeight(); height++){
				// index converted into physical coordinate system
				double [] imageWorld = this.indexToPhysical(width, height);		
				// look in all projections
				for(int indexProj = 0; indexProj < numberProjections; indexProj ++){
					// value that is on the line (line equation s) 
					//will be added to the current position of the reconstructed image					
					double s = (imageWorld[0]*Math.cos(angleWidthR*indexProj) + imageWorld[1]*Math.sin(angleWidthR*indexProj));
					// back into pixel coordinate system
					double [] sinoIndex = filteredS.physicalToIndex(s, indexProj);
					//read out value of the sinogramm
					float val =InterpolationOperators.interpolateLinear(filteredS, sinoIndex[0], indexProj);
					// set value
					this.setAtIndex(width, height, this.getAtIndex(width,height) + val);
				}
				
			}
			
		}
		
		NumericPointwiseOperators.divideBy(this, (float) (numberProjections / scanAngle));
			
	}

}
