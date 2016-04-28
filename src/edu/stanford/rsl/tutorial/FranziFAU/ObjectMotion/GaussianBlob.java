package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;



import ij.ImageJ;
import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;

import edu.stanford.rsl.conrad.data.numeric.NumericGrid;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;




public class GaussianBlob extends Grid2D {

	protected double[] meanValue;	
	protected double [] standardDeviation;	
	
	public GaussianBlob(int imageWidth, int imageHeight, double [] imageSpacing, double [] meanValue, double [] standardDeviation){
		
		// create Image with the desired size
		super(imageWidth, imageHeight);
		this.setSpacing(imageSpacing[0], imageSpacing[1]);
		this.setOrigin((-((imageWidth)*imageSpacing[0])/2),(-((imageHeight)*imageSpacing[1])/2));
		this.meanValue = meanValue;		
		this.standardDeviation = standardDeviation;
		// call Initialization function
		initializeGauss(this, meanValue, standardDeviation);
	}
	
	public double [] getMeanValue(){
		return this.meanValue;
	}
	
	public double [] getStandardDeviation(){
		return this.standardDeviation;
	}
	
	// design Gaussian Blob with the given Parameters
	protected void initializeGauss(GaussianBlob gauss, double [] mean, double [] deviation){
	
		//Set Pixel Values of the Gaussian
		for(int width = 0; width < gauss.getWidth(); width ++){
			for(int height = 0; height < gauss.getHeight(); height ++){

				double [] worldCoordinates = gauss.indexToPhysical(width, height);				
				float value = gaussian2D(worldCoordinates[0], worldCoordinates[1],mean ,deviation);	
				gauss.setAtIndex(width, height, value);
			}
		}	
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
	
	//computes the mean of an array
	public static float computeMean(float [] array){
		float mean = 0;		
		
		for(int i = 0; i < array.length; i++){
			mean += array[i];
		}
		
		mean /= array.length;
		
		return mean;
		
	}
	
	public static float computeVariance(float [] array, float mean){
		float variance = 0.f;
		
		for(int i = 0; i < array.length; i++){			
			variance += (array[i]-mean)*(array[i]-mean);			
		}
		
		variance /= array.length;
		
		return variance;
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
	
	public static void main(String[] args)  {
		new ImageJ();
		
		//Parameters for all methods
		//GaussianBlob
		int imageWidth = 300;
		int imageHeight = 300;
		double[] imageSpacing = {1.0d,1.0d};		
		double [] meanValue = {0.0d,0.0d};		
		double [] standardDeviation = {30.d,30.d};
		
		//MovingGaussian
		double frequency = 1.0d;	// in 1/second	
		double [] changedMeanValue = {0.0d,0.0d};		
		double [] changedStandardDeviation = {15.d,15.d};
		
		//Projection
		int numberProjections = 2*180;
		double detectorSpacing = 1.0d;
		int numberPixel = 500;
		double timeFactor = 5.0d/numberProjections; // time associated with one projection in seconds
		
		//create GaussianBlob
		GaussianBlob object = new GaussianBlob(imageWidth, imageHeight, imageSpacing, meanValue, standardDeviation);
		object.show("GaussianBlob");	

		GaussianBlob changedObject = new GaussianBlob(imageWidth, imageHeight, imageSpacing, changedMeanValue, changedStandardDeviation);
		changedObject.show("changedGaussianBlob");
		
		MovingGaussian movingObject = new MovingGaussian(imageWidth,imageHeight,
				imageSpacing, meanValue, standardDeviation, frequency, changedMeanValue, changedStandardDeviation);
							
		//create sinogramm of gaussianBlob
		ParallelProjection sinogramm = new ParallelProjection(numberProjections, detectorSpacing, numberPixel, timeFactor);
		Grid2D sino1 = sinogramm.createSinogrammMoving(movingObject);
		sino1.show("Sinogramm");

		//backproject sinogramm
		ParallelBackprojection image = new ParallelBackprojection(object);
		Grid2D reconstruct =  image.filteredBackprojection(sino1, detectorSpacing, numberProjections);
		image.show("Backprojected image");				
	
		//create sinogramm of backprojected image
		Grid2D sino2 = sinogramm.createSinogramm(reconstruct);
		sino2.show("Sinogramm2");
			

			
/*		Grid2D resultSino1 = new Grid2D (numberPixel, 2*numberProjections);
		Grid2D resultSino2 = new Grid2D(numberPixel, 2*numberProjections);
		Grid2D resultBackprojected = new Grid2D(imageWidth,2*imageHeight );
		
		for(int i = 0; i < 2; i++){
			
			if(i == 1){
				timeFactor *= 4.0d; 
			}

			for(int x = 0; x < sino1.getWidth(); x ++){
				for(int y = i*sino1.getHeight(); y < (i*sino1.getHeight() + sino1.getHeight()); y ++ ){
					resultSino1.setAtIndex(x, y, sino1.getAtIndex(x, y- i*sino1.getHeight()));
				}
			}
		

			
			for(int x = 0; x < reconstruct.getWidth(); x ++){
				for(int y = i*reconstruct.getHeight(); y < (i*reconstruct.getHeight() + reconstruct.getHeight()); y ++ ){
					resultBackprojected.setAtIndex(x, y, reconstruct.getAtIndex(x, y- i*reconstruct.getHeight()));
				}
			}
			
		
	
			
			for(int x = 0; x < sino2.getWidth(); x ++){
				for(int y = i*sino2.getHeight(); y < (i*sino2.getHeight() + sino2.getHeight()); y ++ ){
					resultSino2.setAtIndex(x, y, sino2.getAtIndex(x, y - i*sino2.getHeight()));
				}
			}
		
		}
		
		resultSino1.show("sino1result");
		resultSino2.show("sino2result");
		resultBackprojected.show("backresult");
	
*/

		
	}
}
