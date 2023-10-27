package com.edu.szu;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.Arrays;

public class CPPCaller {
    public interface CLib extends Library{
        CLib INSTANCE = (CLib) Native.loadLibrary("LIBKDV_to_Pak_Lon/kdv.so", CLib.class);
        String compute(int kernel_type,int kdv_type,float bw_s,
                       int row_pixels,int col_pixels,int st_id,int ed_id,float long_L,
                       float long_U, float lat_L,float lat_U,float t_L,float t_U,int t_pixels,float bw_t);
        String load_data();
        String add(int a,int b);
    }


    public static String load_data(){
        return CLib.INSTANCE.load_data();
    }

    public static String compute(int kernel_type,int kdv_type,float bw_s,
                                 int row_pixels,int col_pixels,int st_id,int ed_id,float long_L,
                                 float long_U, float lat_L,float lat_U,float t_L,float t_U,int t_pixels,float bw_t){
        return CLib.INSTANCE.compute(kernel_type,kdv_type,bw_s,row_pixels,col_pixels,st_id,ed_id,long_L,long_U,
                lat_L,lat_U,t_L,t_U,t_pixels,bw_t);
    }

    public static String add(int a,int b){return CLib.INSTANCE.add(a,b);}

    public static void main(String[] args) {
        System.out.println(CPPCaller.add(1, 2));
        System.out.println(CPPCaller.load_data());
        System.out.println(CPPCaller.compute(1,2,1,256,256,1,1,1,1,
                1,1,1,1,1,1));
    }
}
