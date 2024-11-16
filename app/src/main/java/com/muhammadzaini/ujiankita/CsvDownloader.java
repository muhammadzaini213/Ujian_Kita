package com.muhammadzaini.ujiankita;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CsvDownloader {

    public interface DownloadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void downloadFile(String url, File destination, DownloadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (callback != null) {
                        callback.onFailure(new IOException("Unexpected code " + response));
                    }
                    return;
                }

                // Write the file to the specified destination
                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(destination)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                    return;
                }

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }
}
