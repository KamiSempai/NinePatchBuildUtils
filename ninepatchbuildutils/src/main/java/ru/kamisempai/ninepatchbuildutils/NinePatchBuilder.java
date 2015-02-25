package ru.kamisempai.ninepatchbuildutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Builder class for NinePatch objects.
 * <br><br>
 * Created by Shurygin Denis on 2015-02-06.
 */
public class NinePatchBuilder {
    public static final String TAG = NinePatchBuilder.class.getSimpleName();
    // 4 bytes to define array sizes, 8 bytes to skip,
    // 4*4 bytes to define padding, 4 bytes to skip.
    // Total 32 bytes
    private static final byte BYTES_COUNT_WITHOUT_STRETCH_AND_COLORS = 32;
    private static final byte BYTES_PER_INT = 4;

    // The 9 patch segment is not a solid color.
    private static final int NO_COLOR = 0x00000001;

    // The 9 patch segment is completely transparent.
    private static final int TRANSPARENT_COLOR = 0x00000000;

    private Resources mResources;

    private ArrayList<StretchAreaFloat> mStretchX = new ArrayList<>();
    private ArrayList<StretchAreaFloat> mStretchY = new ArrayList<>();
    private RectF mPadding;

    private Bitmap mBitmap;
    private int mBitmapResId;
    private int mDrawableResId;
    private int mDrawableWidth;
    private int mDrawableHeight;

    private String mSrcName;

    public NinePatchBuilder(Resources resources) {
        mResources = resources;
    }

    public void addStretchAreaX(float a, float b) {
        combine(mStretchX, new StretchAreaFloat(a, b));
    }

    public void addStretchAreaY(float a, float b) {
        combine(mStretchY, new StretchAreaFloat(a, b));
    }

    // TODO Add removeStretchArea methods

    public void setPadding(RectF padding) {
        mPadding = padding;
    }

    public void setBitmap(int resId) {
        resetImage();
        mBitmapResId = resId;
    }

    public void setBitmap(Bitmap bitmap) {
        resetImage();
        mBitmap = bitmap;
    }

    public void setDrawable(int drawableResId, int imageWidth, int imageHeight) {
        resetImage();
        mDrawableResId = drawableResId;
        mDrawableWidth = imageWidth;
        mDrawableHeight = imageHeight;
    }

    public void setName(String srcName) {
        mSrcName = srcName;
    }

    // TODO Add get methods

    public Drawable build() throws IllegalStateException {
        ensureParams();
        if (mBitmap != null)
            return buildFromBitmap(mBitmap);
        if (mBitmapResId != 0)
            return buildFromBitmap(BitmapFactory.decodeResource(mResources, mBitmapResId));
        if (mDrawableResId != 0)
            return buildFromDrawable(mDrawableResId, mDrawableWidth, mDrawableHeight);

        throw new IllegalStateException("Hm. This is should not happen.");
    }

    public void reset() {
        mStretchX.clear();
        mStretchY.clear();
        mPadding = null;
        mSrcName = null;
        resetImage();
    }

    private Drawable buildFromBitmap(Bitmap bitmap) {
        return new NinePatchDrawable(mResources, buildNinePatch(bitmap));
    }

    private Drawable buildFromDrawable(int drawableId, int imageWidth, int imageHeight) {
        Drawable drawable = mResources.getDrawable(drawableId);
        
        // There is no ned to create NinePatchDrawable from NinePatchDrawable.
        if (drawable instanceof NinePatchDrawable)
            return drawable;

        // BitmapDrawable is the special case.
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap.getWidth() == imageWidth && bitmap.getHeight() == imageHeight)
                return buildFromBitmap(bitmap);
        }
        
        // Support for DrawableContainer and heir extensions.
        if (drawable instanceof DrawableContainer) {
            final XmlPullParser parser = mResources.getXml(drawableId);
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            int type = XmlPullParser.START_DOCUMENT;
            try {
                while ((type=parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty loop
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            if (type == XmlPullParser.START_TAG) {
                Drawable result = null;
                try {
                    result = drawable.getClass().newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (result != null) {
                    try {
                        result.inflate(new ResourceWrapper(mResources), parser, attrs);
                        return result;
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        drawable.setBounds(0, 0, imageWidth, imageHeight);
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        return buildFromBitmap(bitmap);
    }

    private NinePatch buildNinePatch(Bitmap bitmap) {
        return new NinePatch(bitmap, getChunkByteArray(bitmap.getWidth(), bitmap.getHeight()), mSrcName);
    }

    private void resetImage() {
        mBitmap = null;
        mBitmapResId = 0;
        mDrawableResId = 0;
    }

    private void ensureParams() throws IllegalStateException {
        if (mBitmap == null && mBitmapResId == 0 && mDrawableResId == 0)
            throw new IllegalStateException("Please, set image via setBitmap or setDrawable before calling build methods.");
    }

    private void combine(ArrayList<StretchAreaFloat> areasList, StretchAreaFloat area) {
        // TODO Combine crossed sectors
        areasList.add(area);
    }

    private byte[] getChunkByteArray(int imageWidth, int imageHeight) {
        ArrayList<StretchAreaInt> areasX = toInt(mStretchX, imageWidth);
        ArrayList<StretchAreaInt> areasY = toInt(mStretchY, imageHeight);
        byte colorsArraySize = getColorArraySize(areasX, areasY, imageWidth, imageHeight);

        ByteBuffer buffer = ByteBuffer.allocate(
                        BYTES_COUNT_WITHOUT_STRETCH_AND_COLORS
                        + areasX.size() * StretchAreaInt.BYTES_PER_ITEM
                        + areasY.size() * StretchAreaInt.BYTES_PER_ITEM
                        + colorsArraySize * BYTES_PER_INT).order(ByteOrder.nativeOrder());

        // Should be 0x01
        buffer.put((byte)0x01);
        // Stretch x size
        buffer.put((byte) (areasX.size() * StretchAreaInt.VALUES_COUNT));
        // Stretch y size
        buffer.put((byte) (areasY.size() * StretchAreaInt.VALUES_COUNT));
        // Color size
        buffer.put(colorsArraySize);

        // Skip 8 bytes
        buffer.putInt(0);
        buffer.putInt(0);

        // Padding
        if (mPadding != null) {
            buffer.putInt((int) (mPadding.left * imageWidth));
            buffer.putInt((int) (mPadding.right * imageWidth));
            buffer.putInt((int) (mPadding.top * imageHeight));
            buffer.putInt((int) (mPadding.bottom * imageHeight));
        }
        else {
            buffer.putInt(0);
            buffer.putInt(imageWidth);
            buffer.putInt(0);
            buffer.putInt(imageHeight);
        }

        // Skip 4 bytes
        buffer.putInt(0);

        // Stretch areas
        for (StretchAreaInt area: areasX) {
            buffer.putInt(area.getA());
            buffer.putInt(area.getB());
        }
        for (StretchAreaInt area: areasY) {
            buffer.putInt(area.getA());
            buffer.putInt(area.getB());
        }
        for (byte i = 0; i < colorsArraySize; i++)
            buffer.putInt(NO_COLOR);

        return buffer.array();
    }

    private static ArrayList<StretchAreaInt> toInt(ArrayList<StretchAreaFloat> areas, int size) {
        ArrayList<StretchAreaInt> result = new ArrayList<>(areas.size());
        for (StretchAreaFloat areaFloat: areas) {
            int a = (int) (areaFloat.getA() * size);
            int b = (int) (areaFloat.getB() * size);
            // TODO Combine crossed sectors
            result.add(new StretchAreaInt(a, b));
        }
        return result;
    }

    private byte getColorArraySize(ArrayList<StretchAreaInt> areasX, ArrayList<StretchAreaInt> areasY, int imageWidth, int imageHeight) {
        return (byte) (getSectorsCount(areasX, imageWidth) * getSectorsCount(areasY, imageHeight));
    }

    private static byte getSectorsCount(ArrayList<StretchAreaInt> areas, int size) {
        if (areas.size() == 0)
            return 1;
        byte sectorsCount = (byte) (areas.size() * 2);
        if (areas.get(0).getA() > 0)
            sectorsCount++;
        if (areas.get(areas.size() - 1).getB() == size)
            sectorsCount--;

        return sectorsCount;
    }


    private class StretchAreaFloat {
        private float a;
        private float b;

        public StretchAreaFloat(float a, float b) {
            this.a = a;
            this.b = b;
        }

        public float getA() {
            return a;
        }

        public void setA(float a) {
            this.a = a;
        }

        public float getB() {
            return b;
        }

        public void setB(float b) {
            this.b = b;
        }
    }

    private static final class StretchAreaInt {
        public static final byte VALUES_COUNT = 2;
        public static final byte BYTES_PER_ITEM = BYTES_PER_INT * VALUES_COUNT;

        private int a;
        private int b;

        public StretchAreaInt(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    /**
     * Wrapper for resources that convert any drawable into NinePatchDrawable.
     */
    private class ResourceWrapper extends Resources {

        public ResourceWrapper(Resources resources) {
            super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
        }

        @Override
        public Drawable getDrawable(int id) throws NotFoundException {
            return buildFromDrawable(id, mDrawableWidth, mDrawableHeight);
        }

        @Override
        public Drawable getDrawable(int id, Theme theme) throws NotFoundException {
            return buildFromDrawable(id, mDrawableWidth, mDrawableHeight);
        }
    }
}
