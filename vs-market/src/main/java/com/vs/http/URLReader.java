package com.vs.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLReader {
    public static String getContext(String urlString, String encode) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            InputStreamReader input = new InputStreamReader(httpConn.getInputStream(), encode);

            BufferedReader bufReader = new BufferedReader(input);
            String line = "";

            while ((line = bufReader.readLine()) != null) {
                content.append(line);
//				content.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

        return content.toString();
    }
}
