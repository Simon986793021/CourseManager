package com.wind.coursemanager.application;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.wind.coursemanager.model.Course;

/**
 * Created by Simon on 2017/5/24.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AVObject.registerSubclass(Course.class);
        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"r1sfMn5TebgurDvrxN7O2Ukk-gzGzoHsz","7P8Gv1aorMHWEgk0IWLaS98J");
        AVOSCloud.setDebugLogEnabled(true);
    }
}
