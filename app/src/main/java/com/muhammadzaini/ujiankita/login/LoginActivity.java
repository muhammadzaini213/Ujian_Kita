package com.muhammadzaini.ujiankita.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    String userDatabase, testDatabase, schoolDatabase;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_login);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        sp = getSharedPreferences("UJIAN_KITA", Activity.MODE_PRIVATE);
        editor = sp.edit();

        school_id_input = findViewById(R.id.school_id_input);
        username_input = findViewById(R.id.username_input);
        password_input = findViewById(R.id.password_input);
        login_btn = findViewById(R.id.login_button);

        restoreData();


        login_btn.setOnClickListener(view -> {
            school_id = school_id_input.getText().toString();
            username = username_input.getText().toString();
            password = password_input.getText().toString();

            if (school_id.isEmpty()) {
                Toast.makeText(this, getString(R.string.id_empty), Toast.LENGTH_LONG).show();
                return;
            } else if (username.isEmpty()) {
                Toast.makeText(this, getString(R.string.username_empty), Toast.LENGTH_LONG).show();
                return;
            } else if (password.isEmpty()) {
                Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_LONG).show();
                return;
            }

            login_btn.setText(getString(R.string.loading));
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
                        Toast.makeText(this, getString(R.string.id_not_found), Toast.LENGTH_LONG).show();
                        login_btn.setText(getString(R.string.log_in_btn));
                        login_btn.setEnabled(true);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                    login_btn.setText(getString(R.string.log_in_btn));
                    login_btn.setEnabled(true);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                login_btn.setText(getString(R.string.log_in_btn));
                login_btn.setEnabled(true);
            });
        });

    }

    private void restoreData() {
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
    }


    public String buildCsvUrl(String spreadsheetId, String username) {
        return "https://spreadsheet.google.com/tq?tqx=out:csv&key=" + spreadsheetId +
                "&gid=0&tq=select%20*%20where%20B='" + username + "'";
    }

    private void login(String database, String username) {
        String spreadsheetId = extractSpreadsheetId(database);
        if (spreadsheetId == null) {
            Toast.makeText(this, getString(R.string.database_not_found), Toast.LENGTH_LONG).show();
            resetLoginBtnState();
            return;
        }

        String csvUrl = buildCsvUrl(spreadsheetId, username);
        File destinationFile = new File(getFilesDir(), "data.csv");

        CsvDownloader csvDownloader = new CsvDownloader();
        csvDownloader.downloadFile(csvUrl, destinationFile, new CsvDownloader.DownloadCallback() {
            @Override
            public void onSuccess() {
                readCsv(destinationFile);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(LoginActivity.this, getString(R.string.database_load_fail), Toast.LENGTH_LONG).show();
                resetLoginBtnState();
            }
        });
    }

    public void readCsv(File csvFile) {
        runOnUiThread(() -> {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    resetLoginBtnState();
                } else {

                    for (String[] row : allRows) {

                        if (row.length < 2) {
                            continue;
                        }

                        if (row[0] == null || row[1] == null || row[2] == null || row[3] == null || row[4] == null) {
                            Toast.makeText(this, getString(R.string.credential_issue), Toast.LENGTH_LONG).show();
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

                            Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            resetLoginBtnState();
                            break;
                        } else {
                            resetLoginBtnState();
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

    private void resetLoginBtnState() {
        login_btn.setText(getString(R.string.log_in_btn));
        login_btn.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}