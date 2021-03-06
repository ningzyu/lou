package com.lyloou.lou.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.lyloou.lou.app.LouApplication;

public class LouActivity extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();
    protected Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Log.d(TAG, "--> onCreate: There you are ==> " + TAG);
        LouApplication.addActivity(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // 如果是TaskRoot，那么移到后台而不是退出程序；
        // if (event.getAction() == KeyEvent.KEYCODE_BACK && this.isTaskRoot()) {
        //    moveTaskToBack(true);
        // }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "--> onDestroy: There you are ==> " + TAG);
        LouApplication.removeActivity(this);
    }
}
                                                  