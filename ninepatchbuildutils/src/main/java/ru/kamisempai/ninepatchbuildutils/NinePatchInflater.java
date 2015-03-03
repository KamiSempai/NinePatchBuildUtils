package ru.kamisempai.ninepatchbuildutils;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Shurygin Denis on 07.02.2015.
 */
public class NinePatchInflater {
    public static final String TAG = NinePatchInflater.class.getSimpleName();

    private static SparseArray<WeakReference<Drawable.ConstantState>> sCache = new SparseArray<>();

    public static Drawable inflate(Resources resources, int resId){
        Drawable drawable = getCachedDrawable(resources, resId);
        if (drawable == null) try {
            final XmlPullParser parser = resources.getXml(resId);
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.START_TAG &&
                    type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            if ("nine-patch-plus".equals(parser.getName())) {
                drawable = inflate(resources, parser, attrs);
            }
            else
                drawable = Drawable.createFromXmlInner(resources, parser, attrs);
            cacheDrawable(drawable, resId);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    private static Drawable inflate(Resources resources, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        NinePatchBuilder builder = new NinePatchBuilder(resources);

        final TypedArray a = obtainAttributes(resources, null, attrs, R.styleable.NinePatchPlusDrawable);

        int srcResId = a.getResourceId(R.styleable.NinePatchPlusDrawable_src, 0);

        if (srcResId == 0) {
            throw new XmlPullParserException(a.getPositionDescription() +
                    ": <nine-patch> requires a valid src attribute");
        }

        int width = (int) a.getDimension(R.styleable.NinePatchPlusDrawable_width, 0);
        int height = (int) a.getDimension(R.styleable.NinePatchPlusDrawable_height, 0);

        builder.setDrawable(srcResId, width, height);

        float[] stretchX = split(a.getString(R.styleable.NinePatchPlusDrawable_stretchX));
        for (int i = 0; i < stretchX.length / 2; i++)
            builder.addStretchSegmentX(stretchX[i * 2], stretchX[i * 2 + 1]);

        float[] stretchY = split(a.getString(R.styleable.NinePatchPlusDrawable_stretchY));
        for (int i = 0; i < stretchY.length / 2; i++)
            builder.addStretchSegmentY(stretchY[i * 2], stretchY[i * 2 + 1]);

        a.recycle();

        return builder.build();
    }

    private static Drawable getCachedDrawable(Resources resources, int id) {
        WeakReference<Drawable.ConstantState> reference = sCache.get(id, null);
        if (reference != null) {
            Drawable.ConstantState constantState = reference.get();
            if (constantState != null)
                return constantState.newDrawable(resources);
            else
                sCache.delete(id);
        }
        return null;
    }

    private static void cacheDrawable(Drawable drawable, int id) {
        // TODO Support themes
        sCache.put(id, new WeakReference<Drawable.ConstantState>(drawable.getConstantState()));
    }

    private static TypedArray obtainAttributes(
            Resources res, Resources.Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    private static float[] split(String string) {
        if (string == null)
            return new float[0];
        String[] strings = string.split(",");
        float[] result = new float[strings.length];
        for (int i = 0; i < strings.length; i++)
            result[i] = Float.parseFloat(strings[i]);
        return result;
    }
}
