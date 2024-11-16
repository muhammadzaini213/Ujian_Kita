package com.muhammadzaini.ujiankita.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammadzaini.ujiankita.R;
import com.muhammadzaini.ujiankita.TimeCheckTask;
import com.muhammadzaini.ujiankita.AppChecker;
import com.muhammadzaini.ujiankita.browser.BrowserActivity;
import com.muhammadzaini.ujiankita.browser.BrowserData;
import com.muhammadzaini.ujiankita.login.UserData;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.WebExamViewHolder> {
    private MainActivity activity;
    private List<ExamListData> examList;

    public ExamListAdapter(MainActivity activity, List<ExamListData> examList) {
        this.activity = activity;
        this.examList = examList;
    }

    public void setExamList(List<ExamListData> examList) {
        this.examList = examList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WebExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_web_exam, parent, false);
        return new WebExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebExamViewHolder holder, int position) {
        ExamListData examListData = examList.get(position);

        holder.itemView.setOnClickListener(view -> {});

        Picasso.get()
                .load(examListData.getLogoUrl())
                .placeholder(R.drawable.baseline_school_24) // Optional placeholder image
                .error(R.drawable.baseline_school_24) // Optional error image
                .into(holder.test_logo);

        setTestStateNormal(holder, examListData);

        SharedPreferences sp = activity.getSharedPreferences("UJIAN_KITA", Activity.MODE_PRIVATE);
        activateJoinBtn(holder, examListData, sp);

        if (sp.getBoolean(examListData.getExcelUrl(), false)) {
            setTestStateFinished(holder, examListData, sp);
            holder.join_btn.setOnClickListener(view -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Masuk Kembali");
                builder.setMessage("Masukkan token dari guru untuk kembali ke ujian");

                final EditText input = new EditText(activity);
                builder.setView(input);

                builder.setPositiveButton("ENTER", (dialog, which) -> {
                    String userInput = input.getText().toString();

                    if (userInput.isEmpty()) {
                        Toast.makeText(activity, "Token tidak boleh kosong", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (userInput.equals(examListData.getEnterCode())) {
                        activateJoinBtn(holder, examListData, sp);
                        sp.edit().putBoolean(examListData.getExcelUrl(), false).apply();
                        setTestStateNormal(holder, examListData);

                        Toast.makeText(activity, "Token masuk kembali benar, silahkan kembali ke ujian", Toast.LENGTH_LONG).show();
                    } else if (userInput.equals(examListData.getExitCode())) {
                        activateJoinBtn(holder, examListData, sp);
                        sp.edit().putBoolean(examListData.getExcelUrl(), false).apply();
                        sp.edit().putInt(BrowserData.excelUrl + "outCounter", 0).apply();
                        sp.edit().putInt(examListData.getExcelUrl() + "Counter", 0).apply();
                        sp.edit().putString(examListData.getExcelUrl() + "time", "").apply();
                        sp.edit().putString(examListData.getExcelUrl() + "timeStamp", "").apply();
                        setTestStateNormal(holder, examListData);

                        Toast.makeText(activity, "Token reset benar, semua pelanggaran yang tercatat akan dihapus", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, "Token salah", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("KEMBALI", (dialog, which) -> dialog.cancel());

                builder.setOnCancelListener(dialogInterface -> {
//                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                });

                builder.setOnDismissListener(dialogInterface -> {
//                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                });

//                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

                builder.show();

            });
        }


    }

    private void enableDND() {
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        }
    }




    @Override
    public int getItemCount() {
        return examList.size();
    }

    // Define your ViewHolder class here
    public static class WebExamViewHolder extends RecyclerView.ViewHolder {
        ImageView test_logo;
        TextView test_name;
        TextView test_grade;
        TextView test_date;
        TextView test_time;
        TextView test_duration;
        Button join_btn;

        public WebExamViewHolder(@NonNull View itemView) {
            super(itemView);

            test_logo = itemView.findViewById(R.id.test_icon);
            test_name = itemView.findViewById(R.id.test_name_text);
            test_grade = itemView.findViewById(R.id.test_class_text);
            test_date = itemView.findViewById(R.id.test_date_text);
            test_time = itemView.findViewById(R.id.test_time_text);
            test_duration = itemView.findViewById(R.id.test_duration_text);
            join_btn = itemView.findViewById(R.id.button_join);
        }
    }

    public String getCurrentTimeIn24HourFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void activateJoinBtn(WebExamViewHolder holder, ExamListData examListData, SharedPreferences sp){
        holder.join_btn.setOnClickListener(view -> {

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            android.os.Handler handler = new Handler(Looper.getMainLooper());

            executorService.submit(() -> {
                boolean isValid = TimeSettingsUtil.isAutoDateTimeEnabled(activity);
                handler.post(() -> {
                    if (isValid) {
                        Calendar calendar = Calendar.getInstance();
                        Date currentDateTime = calendar.getTime();

                        // Format string tanggal dan waktu dari examListData
                        String dateStr = examListData.getDate(); // format: dd-mm-yy
                        String timeStr = examListData.getTime(); // format: hh:mm

                        // Formatter untuk tanggal dan waktu
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());

                        try {
                            // Parse tanggal dan waktu ujian
                            Date examDate = dateFormatter.parse(dateStr);
                            Date examTime = timeFormatter.parse(timeStr);

                            // Gabungkan tanggal dan waktu menjadi satu objek DateTime
                            Calendar examDateTimeCalendar = Calendar.getInstance();
                            examDateTimeCalendar.setTime(examDate);

                            // Set jam dan menit dari waktu ujian
                            Calendar examTimeCalendar = Calendar.getInstance();
                            examTimeCalendar.setTime(examTime);

                            examDateTimeCalendar.set(Calendar.HOUR_OF_DAY, examTimeCalendar.get(Calendar.HOUR_OF_DAY));
                            examDateTimeCalendar.set(Calendar.MINUTE, examTimeCalendar.get(Calendar.MINUTE));

                            Date examDateTime = examDateTimeCalendar.getTime();

                            if (examDateTime.before(currentDateTime)) {
                                BrowserData.user_name = UserData.name;
                                BrowserData.test_name = examListData.getName();
                                BrowserData.duration = examListData.getDuration();
                                BrowserData.excelUrl = examListData.getExcelUrl();
                                enableDND();
                                AppChecker.openApplications(activity, BrowserData.appList);


                                sp.edit().putBoolean(BrowserData.excelUrl, true).apply();
                                sp.edit().putString(BrowserData.excelUrl + "timeStamp", getCurrentTimeIn24HourFormat().toString()).apply();

                                Intent intent = new Intent(activity, BrowserActivity.class);
                                activity.startActivity(intent);
                                activity.finish();

                            } else {
                                Toast.makeText(activity, "Ujian belum dimulai", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ParseException e) {
                            Toast.makeText(activity, "Format tanggal atau waktu tidak valid", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
                        activity.startActivity(intent);
                        Toast.makeText(activity, "Aktifkan tanggal dan waktu otomatis di pengaturan", Toast.LENGTH_LONG).show();

                    }
                });
            });


        });
    }

    private void setTestStateNormal(WebExamViewHolder holder, ExamListData examListData){
        holder.join_btn.setText("MASUK");
        holder.join_btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

        holder.test_name.setText(examListData.getName());
        holder.test_grade.setText("KELAS: " + examListData.getGrade());
        holder.test_date.setText("TANGGAL: " + examListData.getDate());
        holder.test_time.setText("WAKTU: " + examListData.getTime());
        holder.test_duration.setText("DURASI: " + examListData.getDuration() + " MENIT");
    }


    private void setTestStateFinished(WebExamViewHolder holder, ExamListData examListData, SharedPreferences sp){
        holder.test_duration.setText("PELANGGARAN: " + sp.getInt(examListData.getExcelUrl() + "Counter", 0));
        holder.test_grade.setText("LOGIN: " + sp.getString(examListData.getExcelUrl() + "timeStamp", ""));
        holder.test_date.setText("LOGOUT: " + sp.getString(examListData.getExcelUrl() + "time", ""));
        holder.test_time.setText("TOTAL LOGOUT: " + sp.getInt(examListData.getExcelUrl() + "outCounter", 0));
        holder.join_btn.setText("SELESAI");
        holder.join_btn.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
    }
}
