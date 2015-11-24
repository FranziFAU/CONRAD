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
import edu.stanford.rsl.tutorial.parallel.ParallelProjector2D;

public class MyPhantom extends Grid2D{

	public MyPhantom(int width, int height, double d, double e){
		
		super(width,height);
		
		this.setSpacing(d,e);
		this.setOrigin( -(width*d/2), -(height*e/2));		
		
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


	
 	public static void main(String[] args) {
		new ImageJ();
		MyPhantom bild = new MyPhantom(350,350,1.0,1.0);
	//	bild.show("Phantom");
		//String filenameShepp = "C:/Users/Franziska/Desktop/Shepp_logan.png";
		//Grid3D sheppLoganVolume = ImageUtil.wrapImagePlus(IJ.openImage(filenameShepp));
		//ImageGridBuffer a = new ImageGridBuffer();
		//a.set(sheppLoganVolume);
		//Grid2D b = a.get(0);
		//b.show();
	//	b.setOrigin(-b.getWidth()/2, -b.getHeight()/2);
		
		int numberProjections = 379;
		float detectorSpacing = 1.0f;
		int numberPixel = 500;

//		RadonTransform rad = new RadonTransform(numberProjections,detectorSpacing,numberPixel);
//		rad.createSinogramm(bild);
//		rad.show("Sinogramm");
//		FilteredBP fbp = new FilteredBP(bild);
//		fbp.filteredBackProjection(rad, detectorSpacing,numberProjections,false);
//		fbp.show("Reconstruction");
//		
//		FilteredBP fbpRL = new FilteredBP(bild);
//		fbpRL.filteredBackProjection(rad,detectorSpacing,numberProjections,true);
//		fbpRL.show("Reconstruction Ram-Lak");
//		
//		Grid2D differenceImage = (Grid2D)NumericPointwiseOperators.subtractedBy(fbp, fbpRL);
//		differenceImage.show("Unterschiede");
		
		FanBeamReconstruction fanbeam = new FanBeamReconstruction(300,700,389,1.f,700,(float)Math.PI*2);
		fanbeam.fanBeam(bild);
	}	
	
}
