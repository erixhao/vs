package com.vs.common.utils;

import java.text.DecimalFormat;

public class DataFormatUtil {
	private static DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##");
	
	public static double formatDouble(double value){
		return Double.parseDouble(DOUBLE_FORMAT.format(value));
	}
	
	public static double formatPercentage(double value){
		return Double.parseDouble(DOUBLE_FORMAT.format(value));
	}
}
