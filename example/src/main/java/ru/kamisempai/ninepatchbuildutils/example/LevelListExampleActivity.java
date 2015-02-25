package ru.kamisempai.ninepatchbuildutils.example;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import ru.kamisempai.ninepatchbuildutils.NinePatchBuilder;

/**
 * Created by Shurygin Denis on 2015-02-20.
 */
public class LevelListExampleActivity extends BaseActivity {
    
    private ImageView mImageBuilder;
    private ImageView mImageInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_level_example);

        Resources resources = getResources();

        NinePatchBuilder ninePathBuilder = new NinePatchBuilder(resources);
        ninePathBuilder.addStretchAreaX(0.65f, 0.66f);
        ninePathBuilder.addStretchAreaY(0.45f, 0.46f);
        ninePathBuilder.setDrawable(R.drawable.vector_level_list,
                (int) resources.getDimension(R.dimen.vector_border_width),
                (int) resources.getDimension(R.dimen.vector_border_height));

        mImageBuilder = (ImageView) findViewById(R.id.builder);
//        mImageBuilder.setBackground(ninePathBuilder.build());
        mImageBuilder.setBackgroundResource(R.drawable.vector_level_list);

        mImageInflater = (ImageView) findViewById(R.id.inflater);
//        mImageInflater.setBackground(NinePatchInflater.inflate(resources, R.xml.vector_state_list_nine_patch));
    }
    
    public void onClickLevel(View view) {
        int level = 0;
        if (view.getId() == R.id.level_0) {
            level = 0;
        }
        else {
            level = 10;
        }
        mImageBuilder.setImageLevel(level);
        mImageInflater.setImageLevel(level);
    }
}
