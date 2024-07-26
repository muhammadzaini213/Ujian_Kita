package com.muhammadzaini.ujiankita.login;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.muhammadzaini.ujiankita.CsvDownloader;
import com.muhammadzaini.ujiankita.R;
import com.muhammadzaini.ujiankita.main.MainActivity;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    String school_id, username, password;
    SharedPreferences.Editor editor;
    Button login_btn;

    EditText school_id_input, username_input, password_input;

    SharedPreferences sp;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_login);

        school_id_input = findViewById(R.id.school_id_input);
        username_input = findViewById(R.id.username_input);
        password_input = findViewById(R.id.password_input);
        login_btn = findViewById(R.id.login_button);

//        checkAndRequestStoragePermission();

//        RestoreUtils.restoreSharedPreferencesFromDownloads(this, ".backup12");


        sp = getSharedPreferences("UJIAN_KITA", Activity.MODE_PRIVATE);
        editor = sp.edit();
        String saved_school_id = sp.getString("SCHOOL_ID", "");
        String saved_username = sp.getString("USERNAME", "");
        String saved_password = sp.getString("PASSWORD", "");

        if (!saved_school_id.isEmpty() || !saved_username.isEmpty() || !saved_password.isEmpty()) {
            school_id_input.setText(saved_school_id);
            username_input.setText(saved_username);
            password_input.setText(saved_password);

            UserData.school_id = sp.getString("SCHOOL_ID", "");
            UserData.username = sp.getString("USERNAME", "");
            UserData.password = sp.getString("PASSWORD", "");
            UserData.name = sp.getString("NAME", "");

            UserData.grade = sp.getString("GRADE", "");
            UserData.status = sp.getString("STATUS", "");

            URLHelper.user_database = sp.getString("USER_DATABASE", "");
            URLHelper.test_database = sp.getString("TEST_DATABASE", "");
            URLHelper.school_database = sp.getString("SCHOOL_DATABASE", "");


            startActivity(new Intent(this, MainActivity.class));
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        login_btn.setOnClickListener(view -> {
            school_id = school_id_input.getText().toString();
            username = username_input.getText().toString();
            password = password_input.getText().toString();

            if (school_id.isEmpty()) {
                Toast.makeText(this, "ID Sekolah kosong!", Toast.LENGTH_LONG).show();
                return;
            } else if (username.isEmpty()) {
                Toast.makeText(this, "Username kosong!", Toast.LENGTH_LONG).show();
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Password kosong!", Toast.LENGTH_LONG).show();
                return;
            }

            login_btn.setText("LOADING...");
            login_btn.setEnabled(false);

            DocumentReference schoolDocRef = db.collection("school_id").document(school_id);

            schoolDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        userDatabase = document.getString("user_data");
                        testDatabase = document.getString("test_data");
                        schoolDatabase = document.getString("school_data");

                        login(userDatabase, username);

                    } else {
                        Toast.makeText(this, "ID Sekolah tidak ditemukan", Toast.LENGTH_LONG).show();
                        login_btn.setText("LOG IN");
                        login_btn.setEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Login gagal.", Toast.LENGTH_SHORT).show();
                    login_btn.setText("LOG IN");
                    login_btn.setEnabled(true);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Login gagal atau jaringan kurang stabil.", Toast.LENGTH_LONG).show();
                login_btn.setText("LOG IN");
                login_btn.setEnabled(true);
            });
        });

    }

    String userDatabase, testDatabase, schoolDatabase;


    public String buildCsvUrl(String spreadsheetId, String username) {
        return "https://spreadsheet.google.com/tq?tqx=out:csv&key=" + spreadsheetId +
                "&gid=0&tq=select%20*%20where%20B='" + username + "'";
    }

    @SuppressLint("SetTextI18n")
    private void login(String database, String username) {
        String spreadsheetId = extractSpreadsheetId(database);
        if (spreadsheetId == null) {
            Toast.makeText(this, "Database tidak ditemukan", Toast.LENGTH_LONG).show();
            login_btn.setText("LOG IN");
            login_btn.setEnabled(true);
            return;
        }

        String csvUrl = buildCsvUrl(spreadsheetId, username);
        File destinationFile = new File(getFilesDir(), "data.csv");

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
                Toast.makeText(LoginActivity.this, "Gagal mengunduh data", Toast.LENGTH_LONG).show();
                login_btn.setText("LOG IN");
                login_btn.setEnabled(true);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void readCsv(File csvFile) {
        runOnUiThread(() -> {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_LONG).show();
                    login_btn.setText("LOG IN");
                    login_btn.setEnabled(true);
                } else {

                    for (String[] row : allRows) {

                        if (row.length < 2) {
                            continue;
                        }

                        if (row[0] == null || row[1] == null || row[2] == null || row[3] == null || row[4] == null) {
                            Toast.makeText(this, "Kredensial pengguna tidak lengkap dalam database", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String nameData = row[0];
                        String usernameData = row[1];
                        String passwordData = row[2];
                        String gradeData = row[3];
                        String statusData = row[4];

                        if (username.equals(usernameData) && password.equals(passwordData)) {

                            URLHelper.user_database = userDatabase;
                            URLHelper.test_database = testDatabase;
                            URLHelper.school_database = schoolDatabase;

                            UserData.school_id = school_id;
                            UserData.name = nameData;
                            UserData.username = usernameData;
                            UserData.password = passwordData;
                            UserData.grade = gradeData;
                            UserData.status = statusData;

                            editor.putString("SCHOOL_ID", school_id).apply();
                            editor.putString("USERNAME", usernameData).apply();
                            editor.putString("NAME", nameData).apply();
                            editor.putString("PASSWORD", passwordData).apply();
                            editor.putString("GRADE", gradeData).apply();
                            editor.putString("STATUS", statusData).apply();
                            editor.putString("USER_DATABASE", userDatabase).apply();
                            editor.putString("TEST_DATABASE", testDatabase).apply();
                            editor.putString("SCHOOL_DATABASE", schoolDatabase).apply();

//                            BackupUtils.backupSharedPreferencesToDownloads(this, BACKUP_FILE_NAME);

                            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            login_btn.setText("LOG IN");
                            login_btn.setEnabled(true);
                            break;
                        } else {
                            login_btn.setText("LOG IN");
                            login_btn.setEnabled(true);
                        }
                    }
                }
            } catch (IOException ignored) {
            } catch (CsvException e) {
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


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private static final String BACKUP_FILE_NAME = ".backups12.sws";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkAndRequestStoragePermission() {
        if (!hasStoragePermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionExplanationDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
        }
    }

    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Izin dibutuhkan")
                .setMessage("Aplikasi ini membutuhkan izin penyimpanan eksternal.")
                .setPositiveButton("Ya", (dialog, which) -> ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE))
                .setNegativeButton("Tidak", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with backup or restore
//                RestoreUtils.restoreSharedPreferencesFromDownloads(this, BACKUP_FILE_NAME);
                String saved_school_id = sp.getString("SCHOOL_ID", "");
                String saved_username = sp.getString("USERNAME", "");
                String saved_password = sp.getString("PASSWORD", "");

                if (!saved_school_id.isEmpty() || !saved_username.isEmpty() || !saved_password.isEmpty()) {
                    school_id_input.setText(saved_school_id);
                    username_input.setText(saved_username);
                    password_input.setText(saved_password);
                }
                // Re-trigger the action that required the permission
            } else {
                checkAndRequestStoragePermission();
            }
        }
    }
}