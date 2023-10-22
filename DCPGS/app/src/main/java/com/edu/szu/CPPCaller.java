package com.edu.szu;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class CPPCaller {
    public interface CLib extends Library{
        CLib INSTANCE = (CLib) Native.loadLibrary("cpp/test.so", CLib.class);
        int add(int a, int b);
        Pointer get_alg_visual();
        String saveMatrix_toString(Pointer alg_visual);
    }

    private static Pointer self = CLib.INSTANCE.get_alg_visual();

    public static int add(int a, int b){
        return CLib.INSTANCE.add(a, b);
    }

    public static String saveMatrix_toString(){
        return CLib.INSTANCE.saveMatrix_toString(self);
    }

    public static void main(String[] args) {
        System.out.println(CPPCaller.saveMatrix_toString());
    }
}
