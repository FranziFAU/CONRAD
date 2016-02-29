
#pragma OPENCL EXTENSION cl_khr_fp64: enable

kernel void addImages(global float* image1,global float* image2,global float *result, int width, int height){
	
	const unsigned int idx = get_global_id(0);
	const unsigned int idy = get_global_id(1);
	
	if(idx > width || idy > height){
		return;
	}
	
	int index = idy*width + idx;

	result[index] = image1[index] + image2[index];

}

kernel void parallelBackProjection(

	global float *filteredSino, 
	global float *result, 
	int numberProjections,
	int numberDetectorpixel,
	float scanAngle, 
	int width, 
	int height,
	double spacingX,
	double spacingY,
	double originX,
	double originY,
	float spacingSinoXF,
	double spacingSinoY,
	double originSinoX,
	double originSinoY
	){
	
	double spacingSinoX = (double)spacingSinoXF;
	int widthSino = (int)numberDetectorpixel;
	
	
	const unsigned int pixelX = get_global_id(0);
	const unsigned int pixelY = get_global_id(1);
	
	if(pixelX > width || pixelY > height){
		return;
	}
	
	double physicalX = pixelX*spacingX + originX;
	double physicalY= pixelY*spacingY + originY;
	
	float value= 0.0f;
	
	for(int indexP = 0; indexP < numberProjections; indexP++){
		
		double s = ((physicalX*cos(spacingSinoY*indexP)) + (physicalY*sin(spacingSinoY*indexP)));			
		
		double indexDX = (s - originSinoX) / spacingSinoX;		
		
		//lineare Interpolation:
		//
		// val1      value    val2
		// xLower    indexDX  xUpper
		
		// implement error checking?
		int xLower = (int)floor(indexDX);
		int xUpper = xLower + 1;
			
		double d_value_val1 = indexDX-xLower;
		double d_val2_value = xUpper - indexDX;
		double d_val2_val1 = xUpper - xLower;
	
		//values of the corners(pixel values)
		float val1 = filteredSino[indexP*widthSino + xLower];
		float val2 = filteredSino[indexP*widthSino + xUpper];	
		
		value = value + ((d_value_val1/d_val2_val1)*val2 + (d_val2_value/d_val2_val1)*val1);

	}
	
	result[pixelY*width + pixelX] = value;

}