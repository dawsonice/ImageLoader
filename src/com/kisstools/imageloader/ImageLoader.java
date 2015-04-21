package com.kisstools.imageloader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
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

	private LoaderConfig config;

	// path as the string, view as the pack
	private Map<Integer, ViewPack> loadingView;

	private Map<String, ReentrantLock> pathLocks;

	@SuppressLint("UseSparseArrays")
	protected ImageLoader() {
		if (config == null) {
			config = new LoaderConfig();
		}

		config = config.build();
		loadingView = Collections
				.synchronizedMap(new HashMap<Integer, ViewPack>());
		pathLocks = new WeakHashMap<String, ReentrantLock>();
	}

	public void resume() {
		if (!config.paused.get()) {
			LogUtil.w(TAG, "image loader not paused!");
			return;
		}

		config.paused.set(false);
		synchronized (config.pauseLock) {
			config.pauseLock.notifyAll();
		}
	}

	public void pause() {
		if (config.paused.get()) {
			LogUtil.w(TAG, "image loader already paused!");
			return;
		}
		config.paused.set(true);
	}

	public void stop() {
		// set all task as collected
		for (int vid : loadingView.keySet()) {
			loadingView.get(vid).collect();
		}
		loadingView.clear();
		pathLocks.clear();

		// shutdown all executors.
		((ExecutorService) config.remoteExecutor).shutdownNow();
		((ExecutorService) config.nativeExecutor).shutdownNow();
	}

	public void destroy() {

	}

	public void load(String path, LoaderListener listener) {
		load(null, path, listener, false);
	}

	public void load(String path, LoaderListener listener, boolean origin) {
		load(null, path, listener, origin);
	}

	public void load(ImageView imageView, String path) {
		load(imageView, path, null, false);
	}

	public void load(ImageView imageView, String path, boolean origin) {
		load(imageView, path, null, false);
	}

	public void load(ImageView imageView, String path, LoaderListener listener) {
		load(imageView, path, null, false);
	}

	public synchronized void load(ImageView imageView, String path,
			LoaderListener listener, boolean origin) {
		if (StringUtil.isEmpty(path)) {
			return;
		}

		imageView.setImageBitmap(null);
		String key = config.namer.create(path);

		ViewPack vp = new ViewPack(imageView, path);

		if (loadingView.containsKey(vp.getId())) {
			ViewPack oldPack = loadingView.remove(vp.getId());
			String oldPath = oldPack.getPath();
			LogUtil.d(TAG, "collect old view " + oldPath);
			oldPack.collect();
		}

		loadingView.put(vp.getId(), vp);

		LoaderImpl loader = new LoaderImpl();
		ReentrantLock lock = getLock(path);
		LoaderInfo loadInfo = new LoaderInfo(path, key, vp, lock);
		loadInfo.origin = origin;
		loadInfo.loader = this;
		loader.listener = listener;
		loader.config = config;
		loader.loadInfo = loadInfo;

		boolean disk = config.diskCache.contains(key);
		boolean mem = config.memCache.contains(key);

		if (mem || disk) {
			config.nativeExecutor.execute(loader);
		} else {
			config.remoteExecutor.execute(loader);
		}
	}

	private ReentrantLock getLock(String path) {
		ReentrantLock lock = pathLocks.get(path);
		if (lock == null) {
			lock = new ReentrantLock();
			pathLocks.put(path, lock);
		}
		return lock;
	}

	public synchronized boolean cancel(ImageView imageView) {
		ViewPack viewPack = new ViewPack(imageView, null);
		viewPack = loadingView.remove(viewPack.getId());
		if (viewPack != null) {
			String path = viewPack.getPath();
			LogUtil.d(TAG, "cancel load " + path);
			viewPack.collect();
		}
		return viewPack != null;
	}

	public synchronized boolean cancel(String path) {
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