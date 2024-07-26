package com.muhammadzaini.ujiankita.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivityCsvBuilder {
    public static String buildCsvUrl(String spreadsheetId, String username) {
        return "https://spreadsheet.google.com/tq?tqx=out:csv&key=" + spreadsheetId +
                "&gid=0&tq=select%20*%20where%20B='" + username + "'";
    }

    public static String extractSpreadsheetId(String url) {
        Pattern pattern = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
