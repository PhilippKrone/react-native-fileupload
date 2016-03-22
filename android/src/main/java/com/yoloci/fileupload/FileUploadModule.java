package com.yoloci.fileupload;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.bridge.*;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.provider.MediaStore;
import android.net.Uri;
import android.database.Cursor;
import android.provider.DocumentsContract;

public class FileUploadModule extends ReactContextBaseJavaModule {

    private Activity mActivity;

    @Override
    public String getName() {
        return "FileUpload";
    }

    public FileUploadModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        mActivity = activity;
    }

    public String getFilePath(String uri) {
        Uri contentUri = Uri.parse(uri);
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = mActivity.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @ReactMethod
    public void upload(final ReadableMap options, final Callback callback) {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        String uploadUrl = options.getString("uploadUrl");
        String method;
        if (options.hasKey("method")) {
            method = options.getString("method");
        } else {
            method = "POST";
        }

        ReadableMap headers = options.getMap("headers");
        ReadableArray files = options.getArray("files");
        ReadableMap fields = options.getMap("fields");



        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        URL connectURL = null;
        FileInputStream fileInputStream = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;

        try {

            connectURL = new URL(uploadUrl);


            connection = (HttpURLConnection) connectURL.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod(method);

            // set headers
            ReadableMapKeySetIterator iterator = headers.keySetIterator();
            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                connection.setRequestProperty(key, headers.getString(key));
            }



            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

            outputStream = new DataOutputStream( connection.getOutputStream() );

            // set fields
            ReadableMapKeySetIterator fieldIterator = fields.keySetIterator();
            while (fieldIterator.hasNextKey()) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                String key = fieldIterator.nextKey();
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key +  "\"" + lineEnd + lineEnd);
                outputStream.writeBytes(fields.getString(key));
                outputStream.writeBytes(lineEnd);
            }


            for (int i = 0; i < files.size(); i++) {

                ReadableMap file = files.getMap(i);
                String filepath = file.getString("filepath");
                String filename = null;
                Map<String,String> fileInfo = null;
                if (filepath.matches("content://.*")) {
                    filepath = getFilePath(filepath);
                }
                if (file.hasKey("filename")) {
                    filename = file.getString("fileName");
                } else {
                    int j = filepath.lastIndexOf('/');
                    int k = filepath.lastIndexOf('.');
                    filename = filepath.substring(j,k);
                }
                filepath = filepath.replace("file://", "");
                fileInputStream = new FileInputStream(filepath);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + filename + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            if (serverResponseCode != 200) {
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                callback.invoke("Error happened: " + serverResponseMessage, null);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String data = sb.toString();
                JSONObject mainObject = new JSONObject();
                mainObject.put("data", data);
                mainObject.put("status", serverResponseCode);

                BundleJSONConverter bjc = new BundleJSONConverter();
                Bundle bundle = bjc.convertToBundle(mainObject);
                WritableMap map = Arguments.fromBundle(bundle);

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                callback.invoke(null, map);
            }



        } catch(Exception ex) {
            callback.invoke("Error happened: " + ex.getMessage(), null);
        }
    }
}
