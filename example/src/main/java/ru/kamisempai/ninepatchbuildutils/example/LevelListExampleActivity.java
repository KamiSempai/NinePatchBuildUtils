package ru.kamisempai.ninepatchbuildutils.example;

import android.content.res.Resources;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.view.View;

import ru.kamisempai.ninepatchbuildutils.NinePatchBuilder;
import ru.kamisempai.ninepatchbuildutils.NinePatchInflater;

/**
 * Created by Shurygin Denis on 2015-02-20.
 */
public class LevelListExampleActivity extends BaseActivity {

    private LevelListDrawable mBuilderDrawable;
    private LevelListDrawable mInflaterDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_level_example);

        Resources resources = getResources();

        NinePatchBuilder ninePathBuilder = new NinePatchBuilder(resources);
        ninePathBuilder.addStretchSegmentX(0.65f, 0.66f);
        ninePathBuilder.addStretchSegmentY(0.45f, 0.46f);
        ninePathBuilder.setDrawable(R.drawable.vector_level_list,
                (int) resources.getDimension(R.dimen.vector_border_width),
                (int) resources.getDimension(R.dimen.vector_border_height));
        
        mBuilderDrawable = (LevelListDrawable) ninePathBuilder.build();
        findViewById(R.id.builder).setBackground(mBuilderDrawable);

        mInflaterDrawable = (LevelListDrawable) NinePatchInflater.inflate(resources, R.xml.vector_level_list_nine_patch);
        findViewById(R.id.inflater).setBackground(mInflaterDrawable);
    }
    
    public void onClickLevel(View view) {
        int level = 0;
        if (view.getId() == R.id.level_0) {
            level = 0;
        }
        else {
            level = 10;
        }
        mBuilderDrawable.setLevel(level);
        mInflaterDrawable.setLevel(level);
    }
}
