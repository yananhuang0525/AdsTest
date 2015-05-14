package com.hyn.adstest;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;

import android.content.Context;
import android.widget.ImageView;

/**
 * Volley框架是用图片设置
 *
 * @author hyn
 */
public class DownLoadImage {
    public DownLoadImage() {
    }

    /**
     * 设计图片下载
     *
     * @param context
     * @param imageView
     * @param path
     * @param queue
     * @param imageCache
     */
    public static void downLoadImage(Context context, ImageView imageView,
                                     String path, RequestQueue queue, ImageCache imageCache) {
        ImageLoader imageLoader = new ImageLoader(queue, imageCache);
        ImageListener imageListener = ImageLoader.getImageListener(imageView,
                R.drawable.allshare, R.drawable.allshare);
        imageLoader.get(path, imageListener);
    }

}
