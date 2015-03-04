package ru.kamisempai.ninepatchbuildutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
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

    private ArrayList<StretchSegmentFloat> mStretchX = new ArrayList<>();
    private ArrayList<StretchSegmentFloat> mStretchY = new ArrayList<>();
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

    public NinePatchBuilder addStretchSegmentX(float a, float b) {
        combine(mStretchX, new StretchSegmentFloat(a, b));
        return this;
    }

    public NinePatchBuilder addStretchSegmentY(float a, float b) {
        combine(mStretchY, new StretchSegmentFloat(a, b));
        return this;
    }

    // TODO Add removeStretchArea methods

    public NinePatchBuilder setPadding(float left, float top, float right, float bottom) {
         return setPadding(new RectF(left, top, right, bottom));
    }
    
    public NinePatchBuilder setPadding(RectF padding) {
        mPadding = padding;
        return this;
    }

    public NinePatchBuilder setBitmap(int resId) {
        resetImage();
        mBitmapResId = resId;
        return this;
    }

    public NinePatchBuilder setBitmap(Bitmap bitmap) {
        resetImage();
        mBitmap = bitmap;
        return this;
    }

    public NinePatchBuilder setDrawable(int drawableResId, int imageWidth, int imageHeight) {
        resetImage();
        mDrawableResId = drawableResId;
        mDrawableWidth = imageWidth;
        mDrawableHeight = imageHeight;
        return this;
    }

    public NinePatchBuilder setName(String srcName) {
        mSrcName = srcName;
        return this;
    }

    // TODO Add get methods

    public Drawable build() throws IllegalStateException {
        ensureParams();
        Drawable drawable = null;
        if (mBitmap != null)
            drawable = buildFromBitmap(mBitmap);
        if (mBitmapResId != 0)
            drawable = buildFromBitmap(BitmapFactory.decodeResource(mResources, mBitmapResId));
        if (mDrawableResId != 0)
            drawable = buildFromDrawable(mDrawableResId, mDrawableWidth, mDrawableHeight);

        if (drawable == null)
            throw new IllegalStateException("Hm. This is should not happen.");
        
        return drawable;
    }

    public void reset() {
        mStretchX.clear();
        mStretchY.clear();
        mPadding = null;
        mSrcName = null;
        resetImage();
    }

    private Drawable buildFromBitmap(Bitmap bitmap) {
        return new NinePatchDrawable(mResources,
                bitmap,
                getChunkByteArray(bitmap),
                getPaddingRect(bitmap.getWidth(), bitmap.getHeight()),
                mSrcName);
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

    private void resetImage() {
        mBitmap = null;
        mBitmapResId = 0;
        mDrawableResId = 0;
    }

    private void ensureParams() throws IllegalStateException {
        if (mBitmap == null && mBitmapResId == 0 && mDrawableResId == 0)
            throw new IllegalStateException("Please, set image via setBitmap or setDrawable before calling build methods.");
    }

    private void combine(ArrayList<StretchSegmentFloat> segmentList, StretchSegmentFloat segment) {
        // TODO Combine crossed segments
        segmentList.add(segment);
    }

    private byte[] getChunkByteArray(Bitmap bitmap) {
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        ArrayList<StretchSegmentInt> segmentsX = toInt(mStretchX, imageWidth);
        ArrayList<StretchSegmentInt> segmentsY = toInt(mStretchY, imageHeight);
        byte regionsCount = getRegionsCount(segmentsX, segmentsY, imageWidth, imageHeight);

        ByteBuffer buffer = ByteBuffer.allocate(
                        BYTES_COUNT_WITHOUT_STRETCH_AND_COLORS
                        + segmentsX.size() * StretchSegmentInt.BYTES_PER_ITEM
                        + segmentsY.size() * StretchSegmentInt.BYTES_PER_ITEM
                        + regionsCount * BYTES_PER_INT).order(ByteOrder.nativeOrder());

        // Should be 0x01
        buffer.put((byte) 0x01);
        // Stretch x size
        buffer.put((byte) (segmentsX.size() * StretchSegmentInt.VALUES_COUNT));
        // Stretch y size
        buffer.put((byte) (segmentsY.size() * StretchSegmentInt.VALUES_COUNT));
        // Color size
        buffer.put(regionsCount);

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
        for (StretchSegmentInt segment: segmentsX) {
            buffer.putInt(segment.getA());
            buffer.putInt(segment.getB());
        }
        for (StretchSegmentInt segment: segmentsY) {
            buffer.putInt(segment.getA());
            buffer.putInt(segment.getB());
        }

        // TODO If region has no any visible pixel, color should be TRANSPARENT_COLOR
        for (byte i = 0; i < regionsCount; i++)
            buffer.putInt(NO_COLOR);

        return buffer.array();
    }

    private Rect getPaddingRect(int imageWidth, int imageHeight) {
        if (mPadding != null)
            return new Rect((int) (imageWidth * mPadding.left),
                            (int) (imageHeight * mPadding.top),
                            (int) (imageWidth * mPadding.right),
                            (int) (imageHeight * mPadding.bottom));
        return null;
    }

    private static ArrayList<StretchSegmentInt> toInt(ArrayList<StretchSegmentFloat> segments, int size) {
        ArrayList<StretchSegmentInt> result = new ArrayList<>(segments.size());
        for (StretchSegmentFloat segmentFloat: segments) {
            int a = (int) (segmentFloat.getA() * size);
            int b = (int) (segmentFloat.getB() * size);
            // TODO Combine crossed sectors
            result.add(new StretchSegmentInt(a, b));
        }
        return result;
    }

    private byte getRegionsCount(ArrayList<StretchSegmentInt> segmentsX, ArrayList<StretchSegmentInt> segmentsY, int imageWidth, int imageHeight) {
        return (byte) (getSegmentsCount(segmentsX, imageWidth) * getSegmentsCount(segmentsY, imageHeight));
    }

    private static byte getSegmentsCount(ArrayList<StretchSegmentInt> segments, int size) {
        if (segments.size() == 0)
            return 1;
        byte regionsCount = (byte) (segments.size() * 2);
        if (segments.get(0).getA() > 0)
            regionsCount++;
        if (segments.get(segments.size() - 1).getB() == size)
            regionsCount--;

        return regionsCount;
    }


    private class StretchSegmentFloat {
        private float a;
        private float b;

        public StretchSegmentFloat(float a, float b) {
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

    private static final class StretchSegmentInt {
        public static final byte VALUES_COUNT = 2;
        public static final byte BYTES_PER_ITEM = BYTES_PER_INT * VALUES_COUNT;

        private int a;
        private int b;

        public StretchSegmentInt(int a, int b) {
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
