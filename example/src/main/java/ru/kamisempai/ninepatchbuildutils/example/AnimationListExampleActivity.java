package ru.kamisempai.ninepatchbuildutils.example;

import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import ru.kamisempai.ninepatchbuildutils.NinePatchBuilder;
import ru.kamisempai.ninepatchbuildutils.NinePatchInflater;

/**
 * Created by Shurygin Denis on 2015-02-26.
 */
public class AnimationListExampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animation_example);

        Resources resources = getResources();

        NinePatchBuilder ninePatchBuilder = new NinePatchBuilder(resources)
            .addStretchSegmentX(0.65f, 0.66f)
            .addStretchSegmentY(0.45f, 0.46f)
            .setDrawable(R.drawable.vector_animation,
                    (int) resources.getDimension(R.dimen.vector_border_width),
                    (int) resources.getDimension(R.dimen.vector_border_height));

        findViewById(R.id.builder).setBackground(ninePatchBuilder.build());

        findViewById(R.id.inflater).setBackground(NinePatchInflater.inflate(resources, R.xml.vector_animation_list_nine_patch));
    }
    
    public void onClick(View view) {
        Drawable background = view.getBackground();
        if (background instanceof AnimationDrawable) {
            ((AnimationDrawable) background).start();
        }
    }
}
