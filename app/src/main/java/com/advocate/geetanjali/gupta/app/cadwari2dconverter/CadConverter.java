package com.advocate.geetanjali.gupta.app.cadwari2dconverter;

public class CadConverter {

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("cad2x");
    }

    public static native int nativeInit(String fontsDir);
    public static native String nativeGetVersion();
    public static native int nativeConvert(String inputFile, String outputFile, String format);
}