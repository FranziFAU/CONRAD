





kernel void addImages(global float* image1,global float* image2,global float *result, int imageSize){
	
	const unsigned int idx = get_global_id(0);
	
	if(idx > imageSize){
		return;
	}

	result[idx] = image1[idx] + image2[idx];

}