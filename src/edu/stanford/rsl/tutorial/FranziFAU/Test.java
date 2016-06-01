package edu.stanford.rsl.tutorial.FranziFAU;
import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.NumericGrid;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion.GaussianBlob;
import ij.ImagePlus;
import ij.io.FileSaver;


public class Test {

	public static void main(String[] args) {
		
		
		int imageWidth = 300;
		int imageHeight = 300;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {15.d,15.d};
		
		GaussianBlob object = new GaussianBlob(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);
		Grid2D bild = object;
		
		String bildString = bild.toString();
		
		
		
		// Idea 1: Look at fourier space, motion introduces high frequencies
		//compare FFT from both sinogramms (ssd, high pass filter, usw.)

/*		Grid2DComplex sino1C = new Grid2DComplex(sino1);
		sino1C.transformForward();
		sino1C.show();
		
		Grid2DComplex sino2C = new Grid2DComplex(sino2);
		sino2C.transformForward();
		sino2C.show();

		float ssd = op.weightedSSD(sino1C, sino2C, 1.0, 1.0);
		System.out.print(ssd);
*/
		
		//Idea 2: Look at gradient images
		
/*		// compute Gradient image for sino1		
		Grid2D result =  new Grid2D(sino1);
		op.fill(result, 0.0f);		
		gradProjectionGauss(result,sino1,3,true);
		result.show("Gradient1");
		float mean = op.sum(result);
		mean /= result.getNumberOfElements();		
		System.out.println(mean);	
		
		// compute Gradient image for sino2
		Grid2D result2 =  new Grid2D(sino2);
		op.fill(result2, 0.0f);		
		gradProjectionGauss(result2,sino2,3,true);
		result2.show("Gradient2");
		float mean2 = op.sum(result2);
		mean2 /= result2.getNumberOfElements();		
		System.out.println(mean2);		

		
		//Idea 3: compare each projection line of the 2 sinogramms with SSD
		
		float [] ssd = compareSinogrammsLinewise(sino1,sino2);		
		float mean = computeMean(ssd);
		float variance = computeVariance(ssd,mean);		
		System.out.println("MeanSSD: " + mean + "  VarianzSSD: " + variance + "  Mean/Variance:  " + (mean/variance));
		
		
	}
	
	
		public static Grid2D filterSino(Grid2D sinogramm){
		
		Grid2D filteredSinogramm = new Grid2D(sinogramm);
		//construction of the ramLak filter
		Grid1DComplex filter = new Grid1DComplex(filteredSinogramm.getSubGrid(0));
		
		//watch out x-axes is from 0 to - infinity and then from infinity to 0!
		for(int n = 0; n < filter.getSize()[0]/2; n++){
			
			float val;
			if(n == 0){
				val = (float)(1.f/(4.f*1.0d));
				filter.setRealAtIndex(n, val);
				filter.setImagAtIndex(n, 0.f);
			}else if((n%2) == 0){
				val = 0.f;
			}else{
				val = (-(1.f/(float)(Math.pow(n*1.0d, 2)*Math.pow(Math.PI, 2))));
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
			
			Grid1DComplex line = new Grid1DComplex(filteredSinogramm.getSubGrid(s));			
			
			//filter the line			
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
				
			//save filtered line
			for(int indexWidth = 0; indexWidth < filteredSinogramm.getWidth(); indexWidth++){
				filteredSinogramm.setAtIndex(indexWidth, s, line.getRealAtIndex(indexWidth));
			}		
		}
		return filteredSinogramm;
	}
	
		//Compute Value of the 2D Gaussian for the given position
	private float gaussian2D (double x, double y, double [] mean, double [] deviation){		
		
		double exponent = -0.5*(Math.pow((x - mean[0])/deviation[0], 2) 
				+ Math.pow((y - mean[1])/deviation[1], 2));
		double gaussianValue = Math.exp(exponent);
		gaussianValue /= Math.sqrt(2*Math.PI)*deviation[0]*deviation[1];
		
		return (float)gaussianValue;
	}
	
	public static void gradProjectionGauss(NumericGrid gridRes, final NumericGrid grid, int value, boolean offsetleft) {
		subtractOffset(gridRes,grid,grid,0,value,offsetleft);
		gridRes.notifyAfterWrite();
	}
	
	private static void subtractOffset(NumericGrid gridResult, final NumericGrid gridA, final NumericGrid gridB, int xOffset, int yOffset,boolean offsetleft) {

		if(gridA.getSize()[0] != gridB.getSize()[0] || gridA.getSize()[1] != gridB.getSize()[1] ){
			System.err.println("Grids have different sizes so they can not be subtracted.");
		}
		
		for (int x = xOffset; x < gridA.getSize()[0]+xOffset; ++x){
			for (int y = yOffset; y < gridA.getSize()[1]+yOffset; ++y){
				
					
					int xIdx = (x >= gridA.getSize()[0] || x < 0) ? Math.min(Math.max(0, x), gridA.getSize()[0]-1) : x;
					int yIdx = (y >= gridA.getSize()[1] || y < 0) ? Math.min(Math.max(0, y), gridA.getSize()[1]-1) : y;
					

					if(offsetleft)
						gridResult.setValue(gridA.getValue(new int[]{xIdx,yIdx}) - gridB.getValue(new int[]{x-xOffset,y-yOffset}),
								new int[]{x-xOffset,y-yOffset});
					else
						gridResult.setValue(gridA.getValue(new int[]{x-xOffset,y-yOffset}) - gridB.getValue(new int[]{xIdx,yIdx}),
								new int[]{x-xOffset,y-yOffset});
				}
		}
		gridResult.notifyAfterWrite();
	}
	
	public static float [] compareSinogrammsLinewise(Grid2D sino1, Grid2D sino2){
		
		NumericGridOperator op = new NumericGridOperator();
		
		if(sino1.getSize()[0] != sino2.getSize()[0] || sino1.getSize()[1] != sino2.getSize()[1] ){
			System.err.println("Sinogramms have different sizes so they can not be subtracted.");
		}		
		
		float [] result = new float[sino1.getHeight()];
		
		for(int projectionLine = 0; projectionLine < sino1.getHeight(); projectionLine++){
			
			Grid1D lineSino1 = sino1.getSubGrid(projectionLine);
			Grid1D lineSino2 = sino2.getSubGrid(projectionLine);
			
			result[projectionLine] = op.weightedSSD(lineSino1, lineSino2, 1.0, 0.0);			
		}
		
		return result;
	}
	
	
	*/
	}
}
