package com.muhammadzaini.ujiankita;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeCheckTask {

    public static boolean checkTimeSync() {
        String urlString = "http://worldtimeapi.org/api/timezone/Etc/UTC";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Debugging: Print the entire response
            System.out.println("API Response: " + response.toString());

            // Extract datetime string
            String responseString = response.toString();
            int startIndex = responseString.indexOf("\"datetime\":\"") + 12;
            int endIndex = responseString.indexOf("\"", startIndex);
            String dateTimeString = responseString.substring(startIndex, endIndex);
            System.out.println("Extracted datetime: " + dateTimeString);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date networkTime = format.parse(dateTimeString);

            if (networkTime == null) {
                System.out.println("Failed to parse date/time.");
                return false;
            }

            long currentTimeMillis = System.currentTimeMillis();
            long networkTimeMillis = networkTime.getTime();

            System.out.println("Current time: " + new Date(currentTimeMillis));
            System.out.println("Network time: " + new Date(networkTimeMillis));

            // 5-minute tolerance in milliseconds
            return Math.abs(currentTimeMillis - networkTimeMillis) < 5 * 60 * 1000;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
