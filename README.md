# react-native-fileupload  [![NPM version](https://img.shields.io/npm/v/react-native-fileupload.svg?style=flat-square)](https://www.npmjs.com/package/react-native-fileupload)

**Important**: iOS version created by booxood (react-native-file-upload). This repository is the continuation of https://github.com/booxood/react-native-file-upload.

* Support to upload multiple files at a time
* Support to files and fields

## Getting started

`npm install react-native-fileupload --save`

### iOS
1. In XCode, in the project navigator, right click `your project` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-fileupload` and add `FileUpload.m`
3. Run your project (`Cmd+R`)

### Android

*Note: Android support requires React Native 0.12 or later*
 
* Edit `android/settings.gradle` to look like this:

  ```
  rootProject.name = 'MyApp'

  include ':app'

  //Add the following two lines:
  include ':react-native-fileupload'
  project(':react-native-fileupload').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-fileupload/android')
  ```

* Edit `android/app/build.gradle` (note: **app** folder) to look like this: 

  ```
  apply plugin: 'com.android.application'

  android {
    ...
  }

  dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:23.0.0'
    compile 'com.facebook.react:react-native:0.12.+'

    // Add this line:
    compile project(':react-native-fileupload')
  }
  ```

* Edit your `MainActivity.java` (deep in `android/app/src/main/java/...`) to look like this:

  ```
  package com.myapp;

  // Add this line:
  import com.yoloci.fileupload.FileUploadPackage;

  import android.app.Activity;
  ....

  public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {

    private ReactInstanceManager mReactInstanceManager;
    private ReactRootView mReactRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mReactRootView = new ReactRootView(this);

      mReactInstanceManager = ReactInstanceManager.builder()
        .setApplication(getApplication())
        .setBundleAssetName("index.android.bundle")
        .setJSMainModuleName("index.android")
        .addPackage(new MainReactPackage())

        // and this line:
        .addPackage(new FileUploadPackage())

        .setUseDeveloperSupport(BuildConfig.DEBUG)
        .setInitialLifecycleState(LifecycleState.RESUMED)
        .build();

      mReactRootView.startReactApplication(mReactInstanceManager, "MyApp", null);

      setContentView(mReactRootView);
    }
    ...
  }
  ```

## Usage

All you need is to export module `var FileUpload = require('NativeModules').FileUpload;` and direct invoke `FileUpload.upload`.

```javascript
'use strict';

var React = require('react-native');
var FileUpload = require('NativeModules').FileUpload;

var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
} = React;

var FileUploadDemo = React.createClass({
  componentDidMount: function() {
    var obj = {
        uploadUrl: 'http://127.0.0.1:3000',
        method: 'POST', // default 'POST',support 'POST' and 'PUT'
        headers: {
          'Accept': 'application/json',
        },
        fields: {
            'hello': 'world',
        },
        files: [
          {
            name: 'one', // optional, if none then `filename` is used instead
            filename: 'one.w4a', // require, file name
            filepath: '/xxx/one.w4a', // require, file absoluete path
            filetype: 'audio/x-m4a', // options, if none, will get mimetype from `filepath` extension
          },
        ]
    };
    FileUpload.upload(obj, function(err, result) {
      console.log('upload:', err, result);
    })
  },
  render: function() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('FileUploadDemo', () => FileUploadDemo);
```

## License

MIT
