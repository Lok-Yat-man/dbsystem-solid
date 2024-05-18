#include "alg_visual.h"
#include <emscripten/bind.h>
#include <emscripten/val.h>

using namespace emscripten;
alg_visual algorithm;
char empty2[] = " ";
int load_data()
{
	char dataFileName_GPS[] = "./cases.csv";
	char *argv_load[] = {empty2,dataFileName_GPS};
	algorithm.load_datasets_CSV(argv_load);
	return 0;
}

std::string compute(int kernel_type,int kdv_type,float bw_s,
int row_pixels,int col_pixels,int st_id,int ed_id,float long_L,
float long_U, float lat_L,float lat_U,float t_L,float t_U,int t_pixels,float bw_t){
	
    char pa1[32]= "./cases.csv";
    char pa2[32]= "1";
    sprintf(pa2,"%d",kdv_type);
    char pa3[32]= "1";

    char pa4[32]= "113.5252";
    sprintf(pa4,"%.10f",long_L);
    char pa5[32]= "113.5729";
    sprintf(pa5,"%.10f",long_U);

    char pa6[32]= "22.1776";
    sprintf(pa6,"%.10f",lat_L);
    char pa7[32]= "22.2178";
    sprintf(pa7,"%.10f",lat_U);

    char pa8[32]= "16";
    sprintf(pa8,"%d",row_pixels);
    char pa9[32]= "1";
    sprintf(pa9,"%d",col_pixels);

    char pa10[32]= "1";
    sprintf(pa10,"%d",kernel_type);

    char pa11[32]= "1000";
    sprintf(pa11,"%f",bw_s);

    char pa12[32] = "0";
    sprintf(pa12,"%f",t_L);
    char pa13[32] = "100";
    sprintf(pa13,"%f",t_U);

    char pa14[32] = "100";
    sprintf(pa14,"%d",t_pixels);
    char pa15[32] = "1";
    char pa16[32] = "7";
    sprintf(pa16,"%f",bw_t);

    char pa17[8] = "0";
    sprintf(pa17,"%d",st_id);
    char pa18[8] = "100";
    sprintf(pa18,"%d",ed_id);
    char *argv_comp[] = {empty2,pa1,pa2,pa3,pa4,pa5,pa6,pa7,pa8,pa9,pa10,pa11,pa12,pa13,pa14,pa15,pa16,pa17,pa18};

    return algorithm.compute(18, argv_comp);
}


EMSCRIPTEN_BINDINGS(module) {
  emscripten::function("compute", &compute);
  emscripten::function("load_data", &load_data);
}
