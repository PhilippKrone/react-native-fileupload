package com.yoloci.fileupload;

import java.io.File;
import java.net.URL;
import java.util.*;

import com.facebook.react.bridge.ReadableMap;

/**
 * Created by kys on 7/21/2016.
 */
public class UploadParams {
    public interface OnTaskCompleted {
        void onTaskCompleted(UploadResult res);
    }

    public interface OnUploadBegin {
        void onUploadBegin();
    }

    public interface OnUploadProgress {
        void onUploadProgress(int contentLength, int bytesWritten);
    }

    public ReadableMap options;
    public OnTaskCompleted onTaskCompleted;
    public OnUploadBegin onUploadBegin;
    public OnUploadProgress onUploadProgress;
}
