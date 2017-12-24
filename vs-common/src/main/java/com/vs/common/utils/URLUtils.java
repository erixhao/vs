package com.vs.common.utils;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by erix-mac on 15/12/13.
 */
@Slf4j
public final class URLUtils {

    public static String extractURLConext(String webURL){
        return extractURLConext(webURL,"UTF-8");
    }

    //@SneakyThrows(IOException.class)
    public static String extractURLConext(String webURL, String charset){

        StringBuilder content = new StringBuilder();

        try{
            parseURL(webURL, charset, content);


        }catch (Exception e){
            try {
                Thread.sleep(1000 * 3); // wait for stream to be ready.
                parseURL(webURL,charset,content);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (IOException ioe){
                log.error("ERROR : >>>>>>>>>>>>>>>>>>>>> extractURLConext url: " + webURL);
                log.error(ioe.toString());
            }
        }

        return content.toString();
    }

    private static void parseURL(String webURL, String charset, StringBuilder content) throws IOException {
        URL url = new URL(webURL);
        URLConnection con = url.openConnection();

        @Cleanup
        InputStreamReader ins = new InputStreamReader(con.getInputStream(), charset);
        @Cleanup
        BufferedReader in = new BufferedReader(ins);

        String newLine = "";
        while( !in.ready() ){
            System.out.println("Buffer Reader is NOT ready yet, waitting for some time.");
            try {
                Thread.sleep(100); // wait for stream to be ready.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        final int BUFFER_SIZE=1024;
        char[] buffer = new char[BUFFER_SIZE];
        int charsRead = 0;
        while ( (charsRead  = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
            content.append(buffer, 0, charsRead);
        }

        /*while ((newLine = in.readLine()) != null) {
            content.append(newLine);
        }*/
    }
}
