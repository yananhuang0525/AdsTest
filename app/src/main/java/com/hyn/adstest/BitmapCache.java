package com.hyn.adstest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * BitmapCache��������Volley��ܻ�������
 * 
 * @author Administrator
 * 
 */
@SuppressLint("NewApi")
public class BitmapCache implements ImageCache {
	private LruCache<String, Bitmap> mCache;
	public static ImageCache imageCache;
	public static RequestQueue queue;

	/**
	 * Ϊ�˲����е�˳���������ڴ��8/1��Ϊ����
	 */
	public BitmapCache() {
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int maxSize = maxMemory / 8;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}

	public static void BitmapCacheVolley(Context context) {
		queue = Volley.newRequestQueue(context);
		// ��ȡ�ڴ�ռ䣬�����ڴ�ռ�İ˷�֮һ��Ϊ����
		// ���ϵͳ��̬���ڴ�ռ�
		int memory = ((ActivityManager) context.getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE))
				.getLargeMemoryClass();
		// ��ȡ�ڴ�ռ��8��֮һ
		final int cache = 1024 * 1024 * memory / 8;
		final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
				cache);
		imageCache = new ImageCache() {
			public void putBitmap(String url, Bitmap bitmap) {
				// ���û����·��
				lruCache.put(url, bitmap);
			}

			public Bitmap getBitmap(String url) {
				return lruCache.get(url);
			}
		};
	}
}