//package com.muhammadzaini.ujiankita.backup;
//
//import android.content.ContentUris;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.MediaStore;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.lang.reflect.Type;
//import java.util.Map;
//
//public class RestoreUtils {
//
//    public static void restoreSharedPreferencesFromDownloads(Context context, String backupFileName) {
//        String json = null;
//        String fileExtension = ".sws";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            // Android 10 and later
//            Uri queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
//            String[] projection = {MediaStore.Downloads._ID, MediaStore.Downloads.DISPLAY_NAME};
//            String selection = MediaStore.Downloads.DISPLAY_NAME + " LIKE ?";
//            String[] selectionArgs = new String[]{"%" + backupFileName + "%" + fileExtension};
//
//            try (Cursor cursor = context.getContentResolver().query(queryUri, projection, selection, selectionArgs, null)) {
//                if (cursor != null && cursor.moveToFirst()) {
//                    do {
//                        String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME));
//                        if (fileName.contains(backupFileName) && fileName.endsWith(fileExtension)) { // Ensure it ends with .sws
//                            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID));
//                            Uri contentUri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id);
//
//                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getContentResolver().openInputStream(contentUri)))) {
//                                StringBuilder sb = new StringBuilder();
//                                String line;
//                                while ((line = reader.readLine()) != null) {
//                                    sb.append(line);
//                                }
//                                json = sb.toString();
//                                break; // Exit loop after finding the first matching file
//                            }
//                        }
//                    } while (cursor.moveToNext());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // For devices below Android 10
//            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File[] files = downloadsDir.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile() && file.getName().contains(backupFileName) && file.getName().endsWith(fileExtension)) {
//                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
//                            StringBuilder sb = new StringBuilder();
//                            String line;
//                            while ((line = reader.readLine()) != null) {
//                                sb.append(line);
//                            }
//                            json = sb.toString();
//                            break; // Exit loop after finding the first matching file
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//
//        if (json != null) {
//            // Deserialize JSON and restore SharedPreferences
//            Gson gson = new Gson();
//            Type type = new TypeToken<Map<String, Object>>() {}.getType();
//            Map<String, ?> data = gson.fromJson(json, type);
//
//            SharedPreferences prefs = context.getSharedPreferences("UJIAN_KITA", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = prefs.edit();
//
//            for (Map.Entry<String, ?> entry : data.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                if (value instanceof Boolean) {
//                    editor.putBoolean(key, (Boolean) value);
//                } else if (value instanceof Float) {
//                    editor.putFloat(key, (Float) value);
//                } else if (value instanceof Integer) {
//                    editor.putInt(key, (Integer) value);
//                } else if (value instanceof Long) {
//                    editor.putLong(key, (Long) value);
//                } else if (value instanceof String) {
//                    editor.putString(key, (String) value);
//                }
//            }
//
//            editor.apply(); // Apply changes
//        }
//    }
//}
