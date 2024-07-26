package com.muhammadzaini.ujiankita.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammadzaini.ujiankita.CsvDownloader;
import com.muhammadzaini.ujiankita.R;
import com.muhammadzaini.ujiankita.backup.AppChecker;
import com.muhammadzaini.ujiankita.browser.BrowserData;
import com.muhammadzaini.ujiankita.login.LoginActivity;
import com.muhammadzaini.ujiankita.login.URLHelper;
import com.muhammadzaini.ujiankita.login.UserData;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSchoolData(URLHelper.school_database);
        RecyclerView testRecycler = findViewById(R.id.test_recycler);
        ConstraintLayout top_layout = findViewById(R.id.top_layout);


        ExamListLoader loader = new ExamListLoader(MainActivity.this, testRecycler);
        loader.loadExams();

        if (!isDndPermissionGranted()) {
            requestDndPermission();
        }

        findViewById(R.id.filter).setOnClickListener(view -> {

        });
        findViewById(R.id.logout).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

//        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                startActivity(new Intent(MainActivity.this, LoginActivity.class));
//            }
//        };
//
//        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);


        findViewById(R.id.refresh).setOnClickListener(view -> loader.loadExams());

        top_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to prevent it from being called multiple times
                top_layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the height of the top_layout
                int topLayoutHeight = top_layout.getHeight();

                // Set the top margin of your element
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) testRecycler.getLayoutParams();
                layoutParams.topMargin = topLayoutHeight;
                testRecycler.setLayoutParams(layoutParams);
            }
        });
    }

    public String buildCsvUrl(String spreadsheetId) {
        return "https://spreadsheet.google.com/tq?tqx=out:csv&key=" + spreadsheetId +
                "&gid=0&tq=select%20*%20where%20A='" + "DATA:" + "'";
    }

    private void loadSchoolData(String database) {
        String spreadsheetId = extractSpreadsheetId(database);
        if (spreadsheetId == null) {
            Toast.makeText(this, "Invalid Google Sheets URL", Toast.LENGTH_SHORT).show();
            return;
        }

        String csvUrl = buildCsvUrl(spreadsheetId);
        // Example CSV file storage path
        File destinationFile = new File(getFilesDir(), "school.csv");

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
                Toast.makeText(MainActivity.this, "Failed to download CSV", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void readCsv(File csvFile) {
        runOnUiThread(() -> {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Data sekolah tidak ditemukan", Toast.LENGTH_SHORT).show();
                } else {

                    for (String[] row : allRows) {

                        if (row.length < 1) {
                            continue;
                        }

                        if (row[1] == null || row[2] == null) {
                            return;
                        }
                        String schoolLogo = row[1];
                        String schoolName = row[2];

                        if (row[3] != null && !row[3].equals("BLOKIR APLIKASI")) {
                            BrowserData.appList = row[3];
                            AppChecker.openApplications(this, row[3]);
                        }

                        if (row[4] != null && !row[4].equals("VERSI EXAM")) {

                            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            if (!packageInfo.versionName.equals(row[4].trim())) {
                                Toast.makeText(this, "Tolong install aplikasi \"Ujian Kita\" versi " + row[4].trim() + " untuk mengikuti ujian", Toast.LENGTH_LONG).show();
                                finish();
                            }


                        }

                        ImageView school_logo = findViewById(R.id.school_logo);
                        TextView school_name = findViewById(R.id.main_title_info_school);
                        TextView user_info = findViewById(R.id.main_title_info_identity);

                        Picasso.get()
                                .load(schoolLogo)
                                .placeholder(R.drawable.baseline_school_24)
                                .error(R.drawable.baseline_school_24)
                                .into(school_logo);

                        school_name.setText(schoolName);

                        user_info.setText(
                                "ID Sekolah: " + UserData.school_id +
                                        "\nNama: " + UserData.name +
                                        "\nKelas: " + UserData.grade +
                                        "\nStatus: " + UserData.status);
                    }
                }
            } catch (IOException ignored) {
            } catch (CsvException e) {
                throw new RuntimeException(e);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String extractSpreadsheetId(String url) {
        Pattern pattern = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public void requestDndPermission() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setCancelable(false);
        alertDialog.setTitle("Aktifkan izin jangan ganggu");
        alertDialog.setMessage("Izin ini diperlukan agar aplikasi tidak mengalami salah deteksi saat ujian");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ya", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Tidak", ((dialogInterface, i) -> {
            alertDialog.dismiss();
            Toast.makeText(this, "Anda bisa mengaktifkan ini nanti", Toast.LENGTH_LONG).show();
        }));

        alertDialog.show();

    }

    public boolean isDndPermissionGranted() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return notificationManager.isNotificationPolicyAccessGranted();
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }
}
