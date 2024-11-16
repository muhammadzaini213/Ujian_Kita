package com.muhammadzaini.ujiankita.browser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.muhammadzaini.ujiankita.R;
import com.muhammadzaini.ujiankita.AppChecker;
import com.muhammadzaini.ujiankita.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class BrowserActivity extends AppCompatActivity {

    WebView mWebview;

    boolean dialogOpened = false;
    boolean isDetecting = true;

    Counter counter;
    Random random;
    int randomChecker;

    SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        mWebview = findViewById(R.id.exam_webview);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            this.getWindow().setHideOverlayWindows(true);
        }


        AppChecker.openApplications(this, BrowserData.appList);

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);


        counter = new Counter();
        random = new Random();
        randomChecker = 2 + random.nextInt(8);

        sp = getSharedPreferences("UJIAN_KITA", Activity.MODE_PRIVATE);


        startLockTask();

        pinChecker();

        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        };

        // Add the callback to the back press dispatcher
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        TextView textClock = findViewById(R.id.test_duration);
        TextView user_name = findViewById(R.id.user_name);
        TextView test_name = findViewById(R.id.test_name);

        user_name.setText(BrowserData.user_name);
        test_name.setText(BrowserData.test_name);

        new TimeCounter(textClock);

        findViewById(R.id.exit).setOnClickListener(view -> {
            isDetecting = false;
            dialogOpened = true;
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Apa anda yakin?")
                    .setMessage("Anda tidak akan bisa memasuki tes ini lagi.")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        stopLockTask();
                        disableDND();
                        finish();

                        startActivity(new Intent(this, MainActivity.class));
                    })
                    .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();

            alertDialog.setOnCancelListener(dialogInterface -> {
                isDetecting = true;
                dialogOpened = false;
            });

            alertDialog.setOnDismissListener(dialogInterface -> {
                isDetecting = true;
                dialogOpened = false;
            });

        });

        findViewById(R.id.dropdown_btn).setOnClickListener(view -> {
            pinChecker();
            LinearLayout top_layout = findViewById(R.id.top_browser_layout);
            if (top_layout.getVisibility() == View.VISIBLE) {
                top_layout.setVisibility(View.GONE);
            } else {
                top_layout.setVisibility(View.VISIBLE);
            }
        });
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        WebSettings webSettings = mWebview.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(mWebview, true);

        webSettings.setAllowContentAccess(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAllowContentAccess(true);
        mWebview.setKeepScreenOn(true);

        mWebview.loadUrl(BrowserData.excelUrl);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (dialogOpened) {
            return;
        }

        if (!hasFocus) {
            counter.addCount();
        }

        if (!hasFocus && isDetecting && counter.getCount() > randomChecker) {
            TextView suspicious_actions = findViewById(R.id.suspicious_actions);
            suspicious_actions.setText("Aktivitas Mencurigakan: " + counter.getCount());
        }
    }


    private boolean isScreenPinned() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
    }


    private void pinChecker() {
        if (isScreenPinned()) {
            TextView lock_status = findViewById(R.id.lock_status);
            lock_status.setText("Kunci layar: Aktif");
            ImageView lock_btn = findViewById(R.id.lock_btn);
            lock_btn.setOnClickListener(view -> {
                stopLockTask();
                LinearLayout top_layout = findViewById(R.id.top_browser_layout);
                top_layout.setVisibility(View.GONE);
            });
            lock_btn.setImageResource(R.drawable.outline_lock_24);
        } else {
            TextView lock_status = findViewById(R.id.lock_status);
            lock_status.setText("Kunci layar: Mati");
            ImageView lock_btn = findViewById(R.id.lock_btn);
            lock_btn.setOnClickListener(view -> {
                startLockTask();
                LinearLayout top_layout = findViewById(R.id.top_browser_layout);
                top_layout.setVisibility(View.GONE);
            });
            lock_btn.setImageResource(R.drawable.baseline_lock_open_24);
        }
    }

    @Override
    protected void onStop() {
        sp.edit().putInt(BrowserData.excelUrl + "Counter", counter.getCount()).commit();
        sp.edit().putString(BrowserData.excelUrl + "time", getCurrentTimeIn24HourFormat()).commit();
        sp.edit().putInt(BrowserData.excelUrl + "outCounter", sp.getInt(BrowserData.excelUrl + "outCounter", 0) + 1).commit();
        disableDND();

        finish();
        super.onStop();
    }

    private String getCurrentTimeIn24HourFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void disableDND() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager.isNotificationPolicyAccessGranted()) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        }
    }
}
