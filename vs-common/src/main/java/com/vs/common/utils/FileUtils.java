package com.vs.common.utils;

import com.vs.common.domain.enums.TimePeriod;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by erix-mac on 15/8/4.
 */
public final class FileUtils {

    private final static String PATH_DIVIDENDS = "dividends";
    private final static String FILE_DIVIDENDS = "div";


    public static String getCSVFileName(String rootPath, String stock, TimePeriod timePeriod, boolean isDividends){
        return rootPath + (isDividends ? PATH_DIVIDENDS : timePeriod.getName())  + File.separator + stock + "_" + (isDividends ? FILE_DIVIDENDS : timePeriod.getPeriod()) + ".csv";
    }

    public static File[] getMarketDataCSVFilePath(File root, TimePeriod timePeriod, boolean isDividends){

        final String nameFilter = "_" + (isDividends ? FILE_DIVIDENDS : timePeriod.getPeriod());
        File rootPath = new File(root.getPath() + File.separator + (isDividends ? PATH_DIVIDENDS : timePeriod.getName()));

        File[] filteredFiles = rootPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (pathname.isFile() && pathname.getName().contains(nameFilter));
            }
        });

        return filteredFiles;
    }

    public static String getStockCode(String fileName){

        if ( StringUtils.isEmpty(fileName) )
            return "";

        return fileName.substring(0,fileName.indexOf("_"));
    }
}
