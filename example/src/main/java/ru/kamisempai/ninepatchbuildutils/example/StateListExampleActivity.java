package ru.kamisempai.ninepatchbuildutils.example;

import android.content.res.Resources;
import android.os.Bundle;

import ru.kamisempai.ninepatchbuildutils.NinePatchBuilder;
import ru.kamisempai.ninepatchbuildutils.NinePatchInflater;

/**
 * Created by Shurygin Denis on 2015-02-20.
 */
public class StateListExampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_state_example);

        Resources resources = getResources();

        NinePatchBuilder ninePathBuilder = new NinePatchBuilder(resources);
        ninePathBuilder.addStretchAreaX(0.65f, 0.66f);
        ninePathBuilder.addStretchAreaY(0.45f, 0.46f);
        ninePathBuilder.setDrawable(R.drawable.vector_state_list,
                (int) resources.getDimension(R.dimen.vector_border_width),
                (int) resources.getDimension(R.dimen.vector_border_height));

        findViewById(R.id.builder).setBackground(ninePathBuilder.build());

        findViewById(R.id.inflater).setBackground(NinePatchInflater.inflate(resources, R.xml.vector_state_list_nine_patch));
    }
}
