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

public class LoginActivity extends AppCompatActivity {

    SharedPreferences.Editor editor;

    EditText school_id_input, username_input, password_input;
    String userDatabase, testDatabase, schoolDatabase;
    String school_id, username, password;

    Button login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_login);

        //Initialize Firebase and SharedPreferences to get excel URL and get saved inputs
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences sp = getSharedPreferences("UJIAN_KITA", Activity.MODE_PRIVATE);
        editor = sp.edit();

        school_id_input = findViewById(R.id.school_id_input);
        username_input = findViewById(R.id.username_input);
        password_input = findViewById(R.id.password_input);
        login_btn = findViewById(R.id.login_button);

        login_btn.setOnClickListener(view -> {
            //Get inputs
            school_id = school_id_input.getText().toString();
            username = username_input.getText().toString();
            password = password_input.getText().toString();

            //check if inputs is empty
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

            //Deactivate login btn so user didn't accidentally spam it
            login_btn.setText(getString(R.string.loading));
            login_btn.setEnabled(false);


            //Searching database using provided school ID
            DocumentReference schoolDocRef = db.collection("school_id").document(school_id);
            schoolDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        //If document exits, get excel urls
                        userDatabase = document.getString("user_data");
                        testDatabase = document.getString("test_data");
                        schoolDatabase = document.getString("school_data");

                        login(userDatabase, username); // login
                    } else {
                        setMessage(getString(R.string.id_not_found), Toast.LENGTH_LONG);
                    }
                } else {
                    setMessage(getString(R.string.login_fail), Toast.LENGTH_SHORT);
                }
            }).addOnFailureListener(e -> setMessage(getString(R.string.login_error), Toast.LENGTH_LONG));
        });

        restoreData(sp);
    }


    // Restore input data so user didn't need to login again
    private void restoreData(SharedPreferences sp) {
        // This code is used to preventing database overload
        // Would change this later after get a real database for server
        String saved_school_id = sp.getString("SCHOOL_ID", "");
        String saved_username = sp.getString("USERNAME", "");
        String saved_password = sp.getString("PASSWORD", "");

        if (!saved_school_id.isEmpty() || !saved_username.isEmpty() || !saved_password.isEmpty()) {
            // Getting all data from SharedPreferences
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

            // Start MainActivity after sets all data
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void login(String database, String username) {
        // Extract Spreadsheet ID
        String spreadsheetId = LoginActivityCsvBuilder.extractSpreadsheetId(database);

        // Check if database found or not
        if (spreadsheetId == null) {
            setMessage(getString(R.string.database_not_found), Toast.LENGTH_LONG);
            return;
        }

        // Build CSV Url
        String csvUrl = LoginActivityCsvBuilder.buildCsvUrl(spreadsheetId, username);
        File destinationFile = new File(getFilesDir(), "data.csv");

        // Download Csv using csv donwloader
        CsvDownloader csvDownloader = new CsvDownloader();
        csvDownloader.downloadFile(csvUrl, destinationFile, new CsvDownloader.DownloadCallback() {
            @Override
            public void onSuccess() {
                // Read Csv File
                readCsv(destinationFile);
            }

            @Override
            public void onFailure(Exception e) {
                setMessage(getString(R.string.database_load_fail), Toast.LENGTH_LONG);
            }
        });
    }

    public void readCsv(File csvFile) {
        runOnUiThread(() -> {
            try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) {
                    setMessage(getString(R.string.error), Toast.LENGTH_LONG);
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

                            editor.putString("SCHOOL_ID", school_id);
                            editor.putString("USERNAME", usernameData).apply();
                            editor.putString("NAME", nameData).apply();
                            editor.putString("PASSWORD", passwordData).apply();
                            editor.putString("GRADE", gradeData).apply();
                            editor.putString("STATUS", statusData).apply();
                            editor.putString("USER_DATABASE", userDatabase).apply();
                            editor.putString("TEST_DATABASE", testDatabase).apply();
                            editor.putString("SCHOOL_DATABASE", schoolDatabase).apply();

                            setMessage(getString(R.string.login_success), Toast.LENGTH_SHORT);
                            startActivity(new Intent(this, MainActivity.class));
                            break;
                        } else {
                            setMessage(getString(R.string.login_fail), Toast.LENGTH_LONG);
                        }
                    }
                }
            } catch (IOException ignored) {
            } catch (CsvException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setMessage(String message, int timeLength) {
        Toast.makeText(this, message, timeLength).show();
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