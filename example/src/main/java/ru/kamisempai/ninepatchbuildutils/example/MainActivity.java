package ru.kamisempai.ninepatchbuildutils.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Shurygin Denis on 07.02.2015.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStateListExampleClick(View view) {
        startActivity(new Intent(this, StateListExampleActivity.class));
    }

    public void onLevelListExampleClick(View view) {
        startActivity(new Intent(this, LevelListExampleActivity.class));
    }
}
