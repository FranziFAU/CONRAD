package edu.stanford.rsl.tutorial.FranziFAU;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.data.numeric.NumericPointwiseOperators;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;

public class OpenCLReconstruction {
	
	protected OpenCLGrid2D image;
	
	public OpenCLReconstruction(Grid2D phantom){
		image = new OpenCLGrid2D(phantom);
	}
	
	public Grid2D adding(){
		
		for(int i = 0; i < 1000000; i ++){
			NumericPointwiseOperators.addBy(image, image);
		}
		
		
		Grid2D result = new Grid2D(image);
		return result;
	}

}
