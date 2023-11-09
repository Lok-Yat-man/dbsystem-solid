#include "alg_visual.h"
#include <emscripten/bind.h>
#include <emscripten/val.h>

using namespace emscripten;

std::string compute(int kdv_type, int num_threads, double x_L, double x_U, double y_L, double y_U,
        int row_pixels, int col_pixels, int kernel_s_type, double bandwidth_s, double t_L, double t_U,
        int kernel_t_type, double bandwidth_t, double cur_time){
	char empty2[] = " ";
    char pa1[32]= "./data/cases.csv"; //dataFIleName

    char pa2[32]= "1"; //KDV_TYPE
    sprintf(pa2,"%d",kdv_type);

    char pa3[32]= "1";//num_threads
    sprintf(pa2,"%d",num_threads);

    char pa4[32]= "113.5252";//x_L
    sprintf(pa4,"%.10f",x_L);

    char pa5[32]= "113.5729";//x_U
    sprintf(pa5,"%.10f",x_U);

    char pa6[32]= "22.1776";//y_L
    sprintf(pa6,"%.10f",y_L);

    char pa7[32]= "22.2178";//y_U
    sprintf(pa7,"%.10f",y_U);

    char pa8[32]= "16";//row_pixels
    sprintf(pa8,"%d",row_pixels);

    char pa9[32]= "1";//col_pixels
    sprintf(pa9,"%d",col_pixels);

    char pa10[32]= "1";//kernel_s_type
    sprintf(pa10,"%d",kernel_s_type);

    char pa11[32]= "1000";//bandwidth_s
    sprintf(pa11,"%f",bandwidth_s);

    char pa12[32] = "0";//t_L
    sprintf(pa12,"%f",t_L);

    char pa13[32] = "100";//t_U
    sprintf(pa13,"%f",t_U);

    char pa14[32] = "100";//kernel_t_type
    sprintf(pa14,"%d",kernel_t_type);

    char pa15[32] = "1";//bandwidth_t
    sprintf(pa14,"%f",bandwidth_t);

    char pa16[32] = "7.1";//cur_time
    sprintf(pa16,"%f",cur_time);

    char *argv_comp[] = {empty2,pa1,pa2,pa3,pa4,pa5,pa6,pa7,pa8,pa9,pa10,pa11,pa12,pa13,pa14,pa15,pa16};

    alg_visual algorithm;
    algorithm.load_datasets_CSV(argv_comp);
    std::string ans =  algorithm.compute(17, argv_comp);
    algorithm.clear_basic_memory();
    return ans;
}

EMSCRIPTEN_BINDINGS(module) {
    emscripten::function("compute", &compute);
}
