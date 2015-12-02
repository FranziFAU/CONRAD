package edu.stanford.rsl.tutorial.FranziFAU;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;

import ij.IJ;
import ij.ImageJ;
import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.Grid3D;
import edu.stanford.rsl.conrad.data.numeric.InterpolationOperators;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.utils.ImageGridBuffer;
import edu.stanford.rsl.conrad.utils.ImageUtil;




public class MyPhantom extends Grid2D{

	public MyPhantom(int width, int height, double d, double e){
		
		super(width,height);
		
		this.setSpacing(d,e);
		this.setOrigin( -(width*d/2), -(height*e/2));		
		
		int midw = width / 2;
		int midh = height / 2;
		
		// draw triangle
		int currentWidth = width/5;
		
		for(int i = (height / 10); i < (midh - 3); i++){
			for(int j = 0; j < width; j++){
				if(j > (midw - (currentWidth/2)) && j < (midw + (currentWidth/2)) && currentWidth >= 1){
					this.setAtIndex(j,i,0.5f);
				}
				
			}
			currentWidth -= 2;
		}	
		
		// draw square		

		for(int i = midh ; i < height; i++){
			for(int j = width/10 ; j < midw -1; j ++){
				if(i < (((3*height)/10)+5 + midh) && j < ((4*width)/10)){
					this.setAtIndex(j, i, 0.25f);
				}
			}			
		}
		
		// draw rectangle
		
		for(int i = height -6 ; i > midh; i--){
			for(int j = width - 6; j > midw; j --){
				if(j > (width -(((4*width) / 10) + 5)) && i > (height - ((height/ 20) + 5))){
					this.setAtIndex(j, i, 0.75f);
				}
			}
		}		
		
	}


	
 	public static void main(String[] args) {
		new ImageJ();
		//create phantom
		MyPhantom bild = new MyPhantom(150,150,1.0,1.0);
		bild.show("Phantom");
//		String filenameShepp = "C:/Users/Franziska/Desktop/Shepp_logan.png";
//		Grid3D sheppLoganVolume = ImageUtil.wrapImagePlus(IJ.openImage(filenameShepp));
//		ImageGridBuffer allSheppLogans = new ImageGridBuffer();
//		allSheppLogans.set(sheppLoganVolume);
//		Grid2D firstSheppLogan = allSheppLogans.get(0);
//		firstSheppLogan.show();
//		firstSheppLogan.setOrigin(-firstSheppLogan.getWidth()/2, -firstSheppLogan.getHeight()/2);
		
		int numberProjectionsParallel = 379;
		float detectorSpacingParallel = 1.0f;
		int numberOfPixelParallel = 500;
		double scanAngleParallel = Math.PI;
		//create sinogramm of the phantom
//		RadonTransform rad = new RadonTransform(numberProjectionsParallel,detectorSpacingParallel,numberOfPixelParallel);
//		rad.createSinogramm(bild);
//		rad.show("Sinogramm");
//		//filtered backprojection with ramp filter		
//		FilteredBP fbp = new FilteredBP(bild);
//		fbp.filteredBackProjection(rad, detectorSpacingParallel,numberProjectionsParallel,scanAngleParallel,false);
//		fbp.show("Reconstruction");
//		//filtered backprojection with ram lak		
//		FilteredBP fbpRL = new FilteredBP(bild);
//		fbpRL.filteredBackProjection(rad,detectorSpacingParallel,numberProjectionsParallel,scanAngleParallel,true);
//		fbpRL.show("Reconstruction Ram-Lak");
//		
//		Grid2D differenceImage = (Grid2D)NumericPointwiseOperators.subtractedBy(fbp, fbpRL);
//		differenceImage.show("Unterschiede");
		//ran beam recontruction
		float sourceIsocenterDistance = 300.f;
		float sourceDetectorDistance = 600.f;
		int numberOfProjectionsFan = 301;
		float detectorSpacingFan = 1.f;
		int numberOfPixelFan = 599;
		float scanAngleFan = (float)Math.PI*2;
		
		FanBeamReconstruction fanbeam = new FanBeamReconstruction(sourceIsocenterDistance,sourceDetectorDistance,numberOfProjectionsFan,detectorSpacingFan,numberOfPixelFan,scanAngleFan);
		fanbeam.fanBeam(bild);

	
	}	
	
}
