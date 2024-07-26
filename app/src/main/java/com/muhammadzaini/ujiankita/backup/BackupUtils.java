//package com.muhammadzaini.ujiankita.backup;
//
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.MediaStore;
//
//import com.google.gson.Gson;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.Map;
//
//public class BackupUtils {
//
//    public static void backupSharedPreferencesToDownloads(Context context, String backupFileName) {
//        // Ensure the filename ends with .sws
//        String fileExtension = ".sws";
//        if (!backupFileName.endsWith(fileExtension)) {
//            backupFileName += fileExtension;
//        }
//
//        SharedPreferences prefs = context.getSharedPreferences("UJIAN_KITA", Context.MODE_PRIVATE);
//        Map<String, ?> allEntries = prefs.getAll();
//
//        // Convert SharedPreferences to JSON
//        Gson gson = new Gson();
//        String json = gson.toJson(allEntries);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Android 10 and later
//            Uri queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
//            String[] projection = {MediaStore.Downloads._ID};
//            String selection = MediaStore.Downloads.DISPLAY_NAME + "=?";
//            String[] selectionArgs = new String[]{backupFileName};
//
//            // Check if the file already exists
//            try (Cursor cursor = context.getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    // If the file exists, delete it
//                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
//                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);
//                    context.getContentResolver().delete(contentUri, null, null);
//                }
//            }
//
//            // Create a new file
//            ContentValues values = new ContentValues();
//            values.put(MediaStore.Downloads.DISPLAY_NAME, backupFileName);
//            values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
//            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
//
//            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
//
//            if (uri != null) {
//                try (OutputStreamWriter writer = new OutputStreamWriter(context.getContentResolver().openOutputStream(uri))) {
//                    writer.write(json);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            // For devices below Android 10
//            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File backupFile = new File(downloadsDir, backupFileName);
//
//            // Delete the existing file if it exists
//            if (backupFile.exists()) {
//                if (!backupFile.delete()) {
//                    System.err.println("Failed to delete existing backup file.");
//                }
//            }
//
//            // Create a new file
//            try (FileOutputStream fos = new FileOutputStream(backupFile);
//                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {
//                writer.write(json);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
