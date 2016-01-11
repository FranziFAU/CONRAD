


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

global float* filteredSino, 
global float *result, 
int numberProjections,
double scanAngle, 
int width, 
int height,
double spacingX,
double spacingY,
double originX,
double originY,
double spacingSinoX,
double spacingSinoY,
double originSinoX,
double originSinoY
){

	const unsigned int pixelX = get_global_id(0);
	const unsigned int pixelY = get_global_id(1);
	
	if(pixelX > width || pixelY > height){
		return;
	}
	
	double angleWidthR = scanAngle / numberProjections;
	
	double physicalX = pixelX*spacingX - originX;
	double physicalY= pixelY*spacinY - originY;
	
	for(int indexP = 0; indexP < numberProjections; indexP ++){
		
		double s = ((physicalX*sin(angleWidthR*indexP) + (physicalY*sin(angleWidthR*indexP)));
		
		double indexDX = (s - originSinoX) / spacingSinoX;
		double indexDY = (indexP - originSinoY) / spacingSinoY;
		
		//bilineare Interpolation:
		//
		// val11      valup    val21
		//
		//			  value
		//
		// val12      valdown  val22
		
		//index values of the corners
		int index1X = round(indexDX);
		int index2X;
		int index1Y = round(indexDY);
		int index2Y;
		
		if(index1X > indexDX){
			index2X = index1X;
			index1X = index1X -1;
		}else{
			index2X = index1X + 1;
		}	
		
		if(index1Y > indexDY){
			index2Y = index1Y;
			index1Y = index1Y -1;
		}else{
			index2Y = index1Y + 1;
		}
		//values of the corners(pixel values)
		float val11 = filteredSino[index1Y*width + index1X];
		float val12 = filteredSino[index2Y*width + index1X];
		float val21 = filteredSino[index1Y*width + index2X];
		float val22 = filteredSino[index2Y*width + index2X];
		
		float valUp = ((index2X - indexDX)/(index2X - index1X))*val11 + ((indexDX - index1X)/(index2X - index1X))*val21 ;
		float valDown = ((index2X - indexDX)/(index2X - index1X))*val12 + ((indexDX - index1X)/(index2X - index1X))*val22;		
		
		float value = ((index2Y - indexDY)/(index2Y - index1Y))*valUp + ((indexDY- index1Y)/(index2Y - index1Y))*valDown;
		
		result[pixelY*width + pixelX] = result[pixelY*width + pixelX] + value;
		
	}
	
	

}