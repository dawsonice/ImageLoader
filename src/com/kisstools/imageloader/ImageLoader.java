package com.kisstools.imageloader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.kisstools.imageloader.view.ViewPack;
import com.kisstools.utils.LogUtil;
import com.kisstools.utils.StringUtil;

public class ImageLoader {
	public static final String TAG = "ImageLoader";

	private volatile static ImageLoader instance;

	public static ImageLoader getInstance() {
		synchronized (ImageLoader.class) {
			if (instance == null) {
				instance = new ImageLoader();
			}
		}
		return instance;
	}

	private LoaderProperty property;
	private Map<Integer, ViewPack> loaderTasks;
	private LoaderProperty loaderProperty;

	private final AtomicBoolean paused = new AtomicBoolean(false);
	private final Object pauseLock = new Object();

	protected ImageLoader() {
		if (loaderProperty == null) {
			loaderProperty = new LoaderProperty();
		}
		property = loaderProperty.build();
		loaderTasks = new HashMap<Integer, ViewPack>();
	}

	public void resume() {

	}

	public void pause() {

	}

	public void destroy() {
	}

	public boolean hasImage(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			return false;
		}
		String key = loaderProperty.namer.create(filePath);
		if (loaderProperty.memCache.contains(key)) {
			return true;
		}

		if (loaderProperty.diskCache.contains(key)) {
			return true;
		}

		return false;
	}

	public void load(String path) {
		load(null, path);
	}

	public void load(ImageView imageView, String path) {
		if (StringUtil.isEmpty(path)) {
			return;
		}

		String key = loaderProperty.namer.create(path);
		Bitmap cached = loaderProperty.memCache.get(key);
		if (cached != null) {
			LogUtil.d(TAG, "load image from memory");
			imageView.setImageBitmap(cached);
			return;
		}

		ViewPack vp = new ViewPack(imageView);

		LoaderTask task = new LoaderTask();
		task.key = key;
		task.path = path;
		task.view = vp;
		task.loaderProperty = loaderProperty;
		loaderProperty.executor.execute(task);
	}

	public boolean cancel(ImageView imageView) {
		return false;
	}

	public boolean cancel(String path) {
		if (StringUtil.isEmpty(path)) {
			LogUtil.w(TAG, "invalid path");
			return false;
		}

		return loaderTasks.remove(path) != null;
	}

}