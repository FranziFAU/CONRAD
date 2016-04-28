package edu.stanford.rsl.tutorial.FranziFAU;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
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
	*/
	}
}
