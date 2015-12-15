





kernel void addImages(global float* image1,global float* image2,global float *result, int width, int height){
	
	const unsigned int idx = get_global_id(0);
	const unsigned int idy = get_global_id(1);
	
	if(idx > width || idy > height){
		return;
	}
	
	int index = idy*width + idx;

	result[index] = image1[index] + image2[index];

}