package ru.kamisempai.ninepatchbuildutils.example;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Shurygin Denis on 2015-02-20.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle(this.getClass().getSimpleName());
    }
}
