package com.hyn.adstest;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private ViewPager viewPager;
    private LinearLayout layout;
    private View view;
    private List<View> list;
    private ArrayList<ImageView> img_list;
    private int[] imgIds = {R.drawable.t, R.drawable.te, R.drawable.tt, R.drawable.ttt};
    private MyHander hander = new MyHander(new WeakReference<MainActivity>(this));
    private int tempNumber = 0;
    private RequestQueue queue;
    private ImageLoader.ImageCache imageCache;
    private BitmapCache bitmapCache;
    String url1 = "http://www.kongtu.com/4879759082_image.jpg";
    String url2 = "http://www.kongtu.com/4867818855_thumb.jpg";
    String url3 = "http://www.kongtu.com/?action-viewnews-itemid-9472";
    String url4 = "http://www.kongtu.com/?action-viewnews-itemid-8488";
    private String[] url = {url1, url2, url3, url4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        // 获取内存空间，可用内存空间的八分之一作为缓存
        // 获得系统动态的内存空间
        int memory = ((ActivityManager) this.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getLargeMemoryClass();
        // 获取内存空间的8分之一
        final int cache = 1024 * 1024 * memory / 8;
        final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
                cache);
        imageCache = new ImageLoader.ImageCache() {
            public void putBitmap(String url, Bitmap bitmap) {
                // 设置缓存的路径
                lruCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return lruCache.get(url);
            }
        };
        bitmapCache = new BitmapCache();
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        layout = (LinearLayout) findViewById(R.id.layoutView);
        img_list = new ArrayList<ImageView>();
        list = new ArrayList<View>();
        for (int i = 0; i < imgIds.length; i++) {
            ImageView imgView = new ImageView(this);
            View view = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(14, 14);
            layoutParams.setMargins(3, 0, 3, 0);
            view.setLayoutParams(layoutParams);
            imgView.setBackgroundResource(imgIds[i]);
//            DownLoadImage.downLoadImage(this, imgView, url[i], queue, imageCache);
            if (i == 0) {
                view.setBackgroundResource(R.drawable.dot_focused);
            } else {
                view.setBackgroundResource(R.drawable.dot_normal);
            }
            layout.addView(view);
            list.add(view);
            img_list.add(imgView);
        }
        viewPager.setAdapter(new pagerAdapter());
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {

                i %= list.size();
                if (i < 0) {
                    i = list.size() + i;
                }
                Log.i("当前页面", "" + i);
                list.get(tempNumber).setBackgroundResource(R.drawable.dot_normal);
                list.get(i).setBackgroundResource(R.drawable.dot_focused);
                tempNumber = i;
                hander.sendMessage(Message.obtain(hander,
                        MyHander.MSG_PAGE_CHANGED, tempNumber, 0));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                switch (i) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        hander.sendEmptyMessage(MyHander.MSG_KEEP_SILENT);
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        hander.sendEmptyMessageDelayed(
                                MyHander.MSG_UPDATE_IMAGE,
                                MyHander.MSG_DELAY);
                        break;
                    default:
                        break;
                }
            }
        });
        viewPager.setCurrentItem(0);
        hander.sendEmptyMessageDelayed(MyHander.MSG_UPDATE_IMAGE, MyHander.MSG_DELAY);
    }

    private class pagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 对ViewPager页号求模取出View列表中要显示的项
            position %= img_list.size();
            if (position < 0) {
                position = img_list.size() + position;
            }
            ImageView view = img_list.get(position);
            // 如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            // add listeners here if necessary
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }
    }

    private class MyHander extends Handler {
        /**
         * 请求更新显示的View。
         */
        protected static final int MSG_UPDATE_IMAGE = 1;
        /**
         * 请求暂停轮播。
         */
        protected static final int MSG_KEEP_SILENT = 2;
        /**
         * 请求恢复轮播。
         */
        protected static final int MSG_BREAK_SILENT = 3;
        /**
         * 记录最新的页号，当用户手动滑动时需要记录新页号，否则会使轮播的页面出错。
         * 例如当前如果在第一页，本来准备播放的是第二页，而这时候用户滑动到了末页，
         * 则应该播放的是第一页，如果继续按照原来的第二页播放，则逻辑上有问题。
         */
        protected static final int MSG_PAGE_CHANGED = 4;
        // 轮播间隔时间
        protected static final long MSG_DELAY = 5000;
        // 使用弱引用避免Handler泄露.这里的泛型参数可以不是Activity，也可以是Fragment等
        private WeakReference<MainActivity> weakReference;
        private int currentItem = 0;

        public MyHander(WeakReference<MainActivity> wk) {
            weakReference = wk;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            if (activity.hander.hasMessages(MSG_UPDATE_IMAGE)) {
                activity.hander.removeMessages(MSG_UPDATE_IMAGE);
            }
            switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    currentItem++;
                    activity.viewPager.setCurrentItem(currentItem);
                    activity.hander.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    break;
                case MSG_KEEP_SILENT:
                    break;
                case MSG_PAGE_CHANGED:
                    tempNumber = msg.arg1;
                    break;
                case MSG_BREAK_SILENT:
                    activity.hander.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                    break;
            }
        }
    }

}
