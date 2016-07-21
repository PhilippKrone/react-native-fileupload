package com.yoloci.fileupload;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import java.util.Map;

public class FileUploadModule extends ReactContextBaseJavaModule {

    @Override
    public String getName() {
        return "FileUpload";
    }

    public FileUploadModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit(eventName, params);
    }

    @ReactMethod
    public void upload(final ReadableMap options, final Callback callback) {
        try {
            UploadParams params = new UploadParams();
            params.options = options;

            params.onTaskCompleted = new UploadParams.OnTaskCompleted() {
                public void onTaskCompleted(UploadResult res) {
                    //upload success
                    if(res.exception == null) {
                        WritableMap infoMap = Arguments.createMap();
                        ////infoMap.putInt("jobId", jobId);
                        infoMap.putInt("statusCode", res.statusCode);
                        ////infoMap.putInt("bytesWritten", res.bytesWritten);
                        callback.invoke(null, infoMap);
                    }
                }
            };

            params.onUploadBegin = new UploadParams.OnUploadBegin() {
                public void onUploadBegin(int statusCode, int contentLength, Map<String, String> headers) {

                }
            };

            params.onUploadProgress = new UploadParams.OnUploadProgress() {
                public void onUploadProgress(int contentLength, int bytesWritten) {

                }
            };

            Uploader uploader = new Uploader();
            uploader.execute(params);
        } catch (Exception ex) {
            ex.printStackTrace();
            callback.invoke(makeErrorPayload(ex));
        }
    }

    private WritableMap makeErrorPayload(Exception ex) {
        WritableMap error = Arguments.createMap();
        error.putString("message", ex.getMessage());
        return error;
    }
}
