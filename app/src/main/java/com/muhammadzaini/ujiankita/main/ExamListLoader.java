package com.muhammadzaini.ujiankita.main;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammadzaini.ujiankita.CsvDownloader;
import com.muhammadzaini.ujiankita.login.URLHelper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamListLoader {
    MainActivity activity;
    RecyclerView test_recycler;

    private List<ExamListData> examList;
    private ExamListAdapter adapter;

    public ExamListLoader(MainActivity activity, RecyclerView test_recycler){
        this.activity = activity;
        this.test_recycler = test_recycler;
    }

    public void loadExams(){
        test_recycler.removeAllViews();
        examList = new ArrayList<>();
        adapter = new ExamListAdapter(activity, examList);

        test_recycler.setLayoutManager(new LinearLayoutManager(activity));
        test_recycler.setAdapter(adapter);

        loadTestData(URLHelper.test_database);
    }

    public String buildCsvUrl(String spreadsheetId) {
        return "https://spreadsheet.google.com/tq?tqx=out:csv&key=" + spreadsheetId +
                "&gid=0&tq=select%20*%20where%20H='" +"AKTIF" + "'";
    }

    private void loadTestData(String database) {
        String spreadsheetId = extractSpreadsheetId(database);
        if (spreadsheetId == null) {
            Toast.makeText(activity, "Invalid Google Sheets URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String csvUrl = buildCsvUrl(spreadsheetId);
        // Example CSV file storage path
        File destinationFile = new File(activity.getFilesDir(), "test.csv");

        CsvDownloader csvDownloader = new CsvDownloader();
        csvDownloader.downloadFile(csvUrl, destinationFile, new CsvDownloader.DownloadCallback() {
            @Override
            public void onSuccess() {
                // CSV downloaded successfully, read the CSV
                readCsv(destinationFile);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle download error
                Toast.makeText(activity, "Failed to download CSV", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    public void readCsv(File csvFile) {
        activity.runOnUiThread(() -> {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    Toast.makeText(activity, "Data sekolah tidak ditemukan", Toast.LENGTH_SHORT).show();
                } else {
                    for (String[] row : allRows) {

                        if (Objects.equals(row[7], "AKTIF")) {
                            ExamListData examListData = getExamData(row);
                            examList.add(examListData);
                        }
                    }

                    // Sort the examList by date and time
                    sortExamListByDateTime(examList);

                    // Update the adapter with the sorted list
                    adapter.setExamList(examList);
                    adapter.notifyDataSetChanged(); // Notify the adapter of changes
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CsvException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static @NonNull ExamListData getExamData(String[] row) {
        String name = row[0];
        String grade = row[1];
        String date = row[2];
        String time = row[3];
        String duration = row[4];
        String logoUrl = row[5];
        String excelUrl = row[6];
        String isActive = row[7];
        String enterCode = row[8];
        String exitCode = row[9];

        return new ExamListData(logoUrl, name, grade, date, time, duration, excelUrl, isActive, enterCode, exitCode);
    }

    // Method to sort the list based on date and time
    private void sortExamListByDateTime(List<ExamListData> examList) {
        examList.sort((data1, data2) -> {
            Date date1 = parseDateTime(data1.getDate(), data1.getTime());
            Date date2 = parseDateTime(data2.getDate(), data2.getTime());

            if (date1 == null || date2 == null) {
                return 0;
            }
            // Reverse the comparison to sort from newest to oldest
            return date2.compareTo(date1);
        });
    }

    // Method to parse date and time
    public Date parseDateTime(String dateString, String timeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
        try {
            // Combine date and time strings
            String dateTimeString = dateString + " " + timeString;
            return dateFormat.parse(dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String extractSpreadsheetId(String url) {
        Pattern pattern = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
