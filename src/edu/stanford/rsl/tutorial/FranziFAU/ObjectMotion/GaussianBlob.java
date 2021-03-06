package edu.stanford.rsl.tutorial.FranziFAU.ObjectMotion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import edu.stanford.rsl.conrad.data.generic.datatypes.Complex;
import edu.stanford.rsl.conrad.data.numeric.Grid1D;
import edu.stanford.rsl.conrad.data.numeric.Grid1DComplex;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.NumericGrid;
import edu.stanford.rsl.conrad.data.numeric.NumericGridOperator;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;

public class GaussianBlob extends Grid2D {

	protected double[] meanValue;
	protected double[] standardDeviation;

	public GaussianBlob(int imageWidth, int imageHeight, double[] imageSpacing,
			double[] meanValue, double[] standardDeviation, double sigma) {

		// create Image with the desired size
		super(imageWidth, imageHeight);
		this.setSpacing(imageSpacing[0], imageSpacing[1]);
		this.setOrigin((-((imageWidth) * imageSpacing[0]) / 2),
				(-((imageHeight) * imageSpacing[1]) / 2));
		this.meanValue = meanValue;
		this.standardDeviation = standardDeviation;
		// call Initialization function
		initializeGauss(this, meanValue, standardDeviation, sigma);
	}

	public double[] getMeanValue() {
		return this.meanValue;
	}

	public double[] getStandardDeviation() {
		return this.standardDeviation;
	}

	// design Gaussian Blob with the given Parameters
	protected void initializeGauss(GaussianBlob gauss, double[] mean,
			double[] deviation, double sigma) {

		// Set Pixel Values of the Gaussian
		for (int width = 0; width < gauss.getWidth(); width++) {
			for (int height = 0; height < gauss.getHeight(); height++) {

				double[] worldCoordinates = gauss
						.indexToPhysical(width, height);
				float value = gaussian2D(worldCoordinates[0],
						worldCoordinates[1], mean, deviation);
				gauss.setAtIndex(width, height, value);
			}
		}

		Grid2D blurred = gaussianFilter(gauss, sigma);

		if (blurred.getWidth() != gauss.getWidth()
				|| blurred.getHeight() != gauss.getHeight()) {
			System.out.println("Falsch");
		}

		for (int width = 0; width < gauss.getWidth(); width++) {
			for (int height = 0; height < gauss.getHeight(); height++) {
				gauss.setAtIndex(width, height,
						blurred.getAtIndex(width, height));
			}
		}

	}

	// Compute Value of the 2D Gaussian for the given position
	private float gaussian2D(double x, double y, double[] mean,
			double[] deviation) {

		float circle = 0.0f;

		double left = Math.pow(x - mean[0], 2) + Math.pow(y - mean[1], 2);
		double right = Math.pow(deviation[0], 2);
		if (left <= right) {
			circle = 1.0f;
		}

		return circle;

	}

	public static Grid2D gaussianFilter(Grid2D noisy, double sigma) {
		FloatProcessor fp = ImageUtil.wrapGrid2D(new Grid2D(noisy));
		GaussianBlur gb = new GaussianBlur();

		// System.out.println("Gaussian filtering.");
		gb.blurFloat(fp, sigma, sigma, 0.01);

		return ImageUtil.wrapImageProcessor(fp);
	}

	public static void main(String[] args) throws IOException {

		new ImageJ();

		// Loading Trajcectory

		String file = "/media/usb/Projektarbeit/Simulation7/KleinerKreis/Trajectory125.tif";
		Grid2D trajectoryImage = ImageUtil.wrapImagePlus(IJ.openImage(file))
				.getSubGrid(0);
		// trajectoryImage.show("Trajectory");

		// Parameters for all methods

		int imageWidth = 300;
		int imageHeight = 300;
		double[] imageSpacing = { 1.0d, 1.0d };
		double s = 1.d;
		double sigma;
		boolean writeFFT = true;
		boolean saveImages = true;
		String speicherort = "/media/usb/Projektarbeit/Simulation7/KleinerKreis/";

		// Projection
		int numberProjections = 2 * 180;
		double detectorSpacing = 1.0d;
		int numberPixel = 500;
		double t = 1.0d / numberProjections; // time associated with one
												// projection in seconds
		double timeFactor = 0.0;
		double frequency = 1.0d; // in 1/second

		// GaussianBlob pulsating

		double[] meanValue = { 0.0d, 0.0d };
		double[] standardDeviation = { 20.0d, 20.0d };
		double[] changedMeanValue = { 0.0d, 0.0d };
		double[] changedStandardDeviation = { 5d, 5d };

		double sigmaC = (standardDeviation[0] + changedStandardDeviation[0]) / 2;
		double shift = standardDeviation[0] - sigmaC;

		//Gaussian Blob shifted
		double[] meanValueOff = { 0.0d, -shift};
		double[] changedMeanValueOff = { 0.0d, shift };
		double[] standardDeviationOff = { sigmaC, sigmaC };
		double[] changedStandardDeviationOff = { sigmaC, sigmaC };

		for (int curT = 21; curT <= 23; curT++) {
			for (int curS = 0; curS <= 10; curS++) {

				timeFactor = curT * t;
				sigma = curS * s;

				// create GaussianBlob
				GaussianBlob object = new GaussianBlob(imageWidth, imageHeight,
						imageSpacing, meanValue, standardDeviation, sigma);
//				object.show("GaussianBlob");

//				GaussianBlob changedObject = new GaussianBlob(imageWidth,
//						imageHeight, imageSpacing, changedMeanValue,
//						changedStandardDeviation, sigma);
//				changedObject.show("changedGaussianBlob");

				MovingGaussian movingObject = new MovingGaussian(imageWidth,
						imageHeight, imageSpacing, meanValue,
						standardDeviation, frequency, changedMeanValue,
						changedStandardDeviation, sigma);

				// create sinogramm of gaussianBlob
				ParallelProjection sinogramm = new ParallelProjection(
						numberProjections, detectorSpacing, numberPixel,
						timeFactor);
				Grid2D sino1 = sinogramm.createSinogrammMoving(movingObject);
//				sino1.show("Sinogramm");

				// backproject sinogramm
				ParallelBackprojection image = new ParallelBackprojection(
						object);
				Grid2D reconstruct = image.filteredBackprojection(sino1,
						detectorSpacing, numberProjections);
//				image.show("Backprojected image");

				// create sinogramm of backprojected image
				Grid2D sino2 = sinogramm.createSinogramm(reconstruct);
//				sino2.show("Sinogramm2");

				// Gaussian Blob offset

				GaussianBlob objectOff = new GaussianBlob(imageWidth,
						imageHeight, imageSpacing, meanValueOff,
						standardDeviationOff, sigma);
//				objectOff.show("Off");

//				GaussianBlob changedObjectOff = new GaussianBlob(imageWidth,
//						imageHeight, imageSpacing, changedMeanValueOff,
//						changedStandardDeviationOff, sigma);
////				changedObjectOff.show("Off2");

				MovingGaussian movingObjectOff = new MovingGaussian(imageWidth,
						imageHeight, imageSpacing, meanValueOff,
						standardDeviationOff, frequency, changedMeanValueOff,
						changedStandardDeviationOff, sigma);

				// create sinogramm of gaussianBlob
				ParallelProjection sinogrammOff = new ParallelProjection(
						numberProjections, detectorSpacing, numberPixel,
						timeFactor);
				Grid2D sino1Off = sinogrammOff
						.createSinogrammMoving(movingObjectOff);
//				sino1Off.show("SinogrammOff");

				// backproject sinogramm
				ParallelBackprojection imageOff = new ParallelBackprojection(
						objectOff);
				Grid2D reconstructOff = imageOff.filteredBackprojection(
						sino1Off, detectorSpacing, numberProjections);
//				imageOff.show("Backprojected imageOff");

				// create sinogramm of backprojected image
				Grid2D sino2Off = sinogrammOff.createSinogramm(reconstructOff);
//				sino2Off.show("Sinogramm2Off");

				// saving the images

				if (saveImages) {
					String title = "Pulsating";
					
					String a = String.format("%sBilder/1.%d.%d_Sino1.tif",speicherort, curT,curS);

					ImagePlus imageSino1 = ImageUtil.wrapGrid(sino1, title);
					IJ.save(imageSino1,a);
					
					String b = String.format("%sBilder/1.%d.%d_Back.tif",speicherort, curT,curS);
					ImagePlus imageBack = ImageUtil.wrapGrid(reconstruct, title);
					IJ.save(imageBack,b);

					String c = String.format("%sBilder/1.%d.%d_Sino2.tif",speicherort, curT,curS);
					ImagePlus imageSino2 = ImageUtil.wrapGrid(sino2, title);
					IJ.save(imageSino2,c);

					String titleOff = "Offset";

					String d = String.format("%sBilder/2.%d.%d_Sino1.tif",speicherort, curT,curS);
					ImagePlus imageSino1Off = ImageUtil.wrapGrid(sino1Off,titleOff);
					IJ.save(imageSino1Off,d);

					String e = String.format("%sBilder/2.%d.%d_Back.tif",speicherort,curT,curS);
					ImagePlus imageBackOff = ImageUtil.wrapGrid(reconstructOff,titleOff);
					IJ.save(imageBackOff,e);

					String g = String.format("%sBilder/2.%d.%d_Sino2.tif", speicherort, curT,curS);
					ImagePlus imageSino2Off = ImageUtil.wrapGrid(sino2Off,titleOff);
					IJ.save(imageSino2Off,g);
				}

				if (writeFFT) {

					float[] pulsating1 = new float[numberProjections];
					float[] pulsating2 = new float[numberProjections];
					float[] offset2 = new float[numberProjections];
					float[] offset1 = new float[numberProjections];

					for (int y = 0; y < trajectoryImage.getHeight(); y++) {

						int[] index = new int[2];
						int i = 0;

						for (int x = 1; x < trajectoryImage.getWidth(); x++) {

							if (trajectoryImage.getAtIndex(x, y) != 0 && i < 2) {
								index[i] = x;
								i++;
							}
						}

						float sum = trajectoryImage.getAtIndex(index[0], y)
								+ trajectoryImage.getAtIndex(index[1], y);
						float first = trajectoryImage.getAtIndex(index[0], y)
								/ sum;
						float second = trajectoryImage.getAtIndex(index[1], y)
								/ sum;
						pulsating2[y] = first * sino2.getAtIndex(index[0], y)
								+ second * sino2.getAtIndex(index[1], y);
						pulsating1[y] = first * sino1.getAtIndex(index[0], y)
								+ second * sino1.getAtIndex(index[1], y);
						offset2[y] = first * sino2Off.getAtIndex(index[0], y)
								+ second * sino2Off.getAtIndex(index[1], y);
						offset1[y] = first * sino1Off.getAtIndex(index[0], y)
								+ second * sino1Off.getAtIndex(index[1], y);

					}

					String filename1 = String.format("%sFrequenzdaten SigmaC/Pulsating1.%d.%d.txt",speicherort,curT,curS);

					BufferedWriter outputWriter1 = null;
					outputWriter1 = new BufferedWriter(
							new FileWriter(filename1));

					for (int j = 0; j < numberProjections; j++) {
						String b = Double.toString(pulsating1[j]);
						outputWriter1.write(b);
						outputWriter1.write(' ');

					}
					outputWriter1.close();

					String filename2 = String.format("%sFrequenzdaten SigmaC/Pulsating2.%d.%d.txt",speicherort,curT,curS);

					BufferedWriter outputWriter2 = null;
					outputWriter2 = new BufferedWriter(
							new FileWriter(filename2));

					for (int j = 0; j < numberProjections; j++) {
						String b = Double.toString(pulsating2[j]);
						outputWriter2.write(b);
						outputWriter2.write(' ');

					}
					outputWriter2.close();

					String filenameOff1 = String.format("%sFrequenzdaten SigmaC/Offset1.%d.%d.txt",speicherort,curT,curS);

					BufferedWriter outputWriterOff1 = null;
					outputWriterOff1 = new BufferedWriter(new FileWriter(
							filenameOff1));

					for (int j = 0; j < numberProjections; j++) {
						String Off = Double.toString(offset1[j]);
						outputWriterOff1.write(Off);
						outputWriterOff1.write(' ');

					}
					outputWriterOff1.close();

					String filenameOff2 = String.format("%sFrequenzdaten SigmaC/Offset2.%d.%d.txt",speicherort,curT,curS);

					BufferedWriter outputWriterOff2 = null;
					outputWriterOff2 = new BufferedWriter(new FileWriter(
							filenameOff2));

					for (int j = 0; j < numberProjections; j++) {
						String Off = Double.toString(offset2[j]);
						outputWriterOff2.write(Off);
						outputWriterOff2.write(' ');

					}
					outputWriterOff2.close();
				}

			}
		}

		// Design trajectory

//		 Grid2D singlePoint = new Grid2D(imageWidth, imageHeight);
//		 singlePoint.setSpacing(imageSpacing);
//		 singlePoint.setOrigin((-((imageWidth)*imageSpacing[0])/2),(-((imageHeight)*imageSpacing[1])/2));
//		 double [] idx = {0.0d,10};
//		 double [] tmp = singlePoint.physicalToIndex(idx[0], idx[1]);
//		 int [] pixel = {(int)Math.floor(tmp[0]),(int)Math.floor(tmp[1])};
//		 singlePoint.setValue(1.0f, pixel);
//		 singlePoint.show();
//		
//		 ParallelProjection sinogrammT = new
//		 ParallelProjection(numberProjections, detectorSpacing, numberPixel,
//		 timeFactor);
//		 Grid2D trajectory = sinogrammT.createSinogramm(singlePoint);
//		 trajectory.show("Result");
//		 String titleTrajectory = "Trajectory";
//		
//		 ImagePlus trajectoryImg = ImageUtil.wrapGrid(trajectory,
//		 titleTrajectory);
//		 IJ.save(trajectoryImg,"/home/cip/medtech2011/ef58ozyd/Projektarbeit/Trajectory10.tif");

		System.out.print("Ende");

	}
}
