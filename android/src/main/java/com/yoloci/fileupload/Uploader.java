package com.yoloci.fileupload;

import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by kys on 7/20/2016.
 */
public class Uploader extends AsyncTask<UploadParams, int[], UploadResult> {

    private UploadParams mParam;
    private AtomicBoolean mAbort = new AtomicBoolean(false);

    @Override
    protected UploadResult doInBackground(UploadParams... params) {
        mParam = params[0];
        UploadResult res = new UploadResult();
        try {
            this.upload(mParam, res);
            mParam.onTaskCompleted.onTaskCompleted(res);

        } catch (Exception ex) {
            res.exception = ex;
            mParam.onTaskCompleted.onTaskCompleted(res);
            return res;
        }
        return res;
    }

    public void upload(final UploadParams options, UploadResult res) {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        String uploadUrl = options.options.getString("uploadUrl");
        String method;
        if (options.options.hasKey("method")) {
            method = options.options.getString("method");
        } else {
            method = "POST";
        }

        ReadableMap headers = options.options.getMap("headers");
        ReadableArray files = options.options.getArray("files");
        ReadableMap fields = options.options.getMap("fields");



        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        URL connectURL = null;
        FileInputStream fileInputStream = null;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024*1024*1024;
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
                outputStream.write(fields.getString(key).getBytes());
                outputStream.writeBytes(lineEnd);
            }

            for (int i = 0; i < files.size(); i++) {
                ReadableMap file = files.getMap(i);
                String name = file.getString("name");
                String filename = file.getString("filename");
                String filepath = file.getString("filepath");
                String filetype = file.getString("filetype");
                filepath = filepath.replace("file://", "");
                fileInputStream = new FileInputStream(filepath);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                //outputStream.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + filename + "\"" + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\";filename=\"" + filename + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: " + filetype + lineEnd);
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
            if (serverResponseCode > 206) {
                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                res.statusCode = serverResponseCode;
                res.responseStr = "";
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
                res.statusCode = serverResponseCode;
                res.responseStr = data;
            }
        } catch(Exception ex) {
            //callback.invoke("Error happened: " + ex.getMessage(), null);
        }
    }

    @Override
    protected void onProgressUpdate(int[]... values) {
        super.onProgressUpdate(values);
        mParam.onUploadProgress.onUploadProgress(values[0][0], values[0][1]);
    }
}
