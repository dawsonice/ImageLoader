package com.kisstools.imageloader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
	// path as the string, view as the pack
	private Map<Integer, ViewPack> loadingView;
	private Map<String, ReentrantLock> pathLocks;

	private LoaderProperty loaderProperty;

	private final AtomicBoolean paused = new AtomicBoolean(false);
	private final Object pauseLock = new Object();

	protected ImageLoader() {
		if (loaderProperty == null) {
			loaderProperty = new LoaderProperty();
		}

		property = loaderProperty.build();
		loadingView = Collections
				.synchronizedMap(new HashMap<Integer, ViewPack>());
		pathLocks = new WeakHashMap<String, ReentrantLock>();
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

	public void load(String path, LoaderListener listener) {
		load(null, path, listener);
	}

	public void load(ImageView imageView, String path) {
		load(imageView, path, null);
	}

	public void load(ImageView imageView, String path, LoaderListener listener) {
		if (StringUtil.isEmpty(path)) {
			return;
		}

		imageView.setImageBitmap(null);
		String key = loaderProperty.namer.create(path);

		ViewPack vp = new ViewPack(imageView, path);

		if (loadingView.containsKey(vp.getId())) {
			ViewPack old = loadingView.remove(vp.getId());
			String oldPath = old.getPath();
			LogUtil.d(TAG, "collect old view " + oldPath);
			old.collect();
		}

		loadingView.put(vp.getId(), vp);

		LoaderTask task = new LoaderTask();
		task.loaderProperty = loaderProperty;
		ReentrantLock lock = getLock(path);
		LoadInfo loadInfo = new LoadInfo(path, key, vp, lock);
		loadInfo.loader = this;
		task.listener = listener;
		task.loadInfo = loadInfo;
		loaderProperty.executor.execute(task);
	}

	private ReentrantLock getLock(String path) {
		ReentrantLock lock = pathLocks.get(path);
		if (lock == null) {
			lock = new ReentrantLock();
			pathLocks.put(path, lock);
		}
		return lock;
	}

	public boolean cancel(ImageView imageView) {
		ViewPack viewPack = new ViewPack(imageView, null);
		viewPack = loadingView.remove(viewPack.getId());
		if (viewPack != null) {
			loadingView.remove(viewPack.getId());
			String path = viewPack.getPath();
			LogUtil.d(TAG, "cancel load " + path);
			viewPack.collect();
		}
		return viewPack != null;
	}

	public boolean cancel(String path) {
		if (StringUtil.isEmpty(path)) {
			LogUtil.w(TAG, "invalid path");
			return false;
		}

		boolean removed = false;

		Iterator<Integer> iterator = loadingView.keySet().iterator();
		while (iterator.hasNext()) {
			int vid = iterator.next();
			ViewPack vp = loadingView.get(vid);
			if (path.equals(vp.getPath())) {
				iterator.remove();
				LogUtil.d(TAG, "cancel load " + path);
				vp.collect();
				removed = true;
			}
		}

		return removed;
	}

}