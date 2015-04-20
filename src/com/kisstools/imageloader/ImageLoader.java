package com.kisstools.imageloader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
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

	private LoaderRuntime runtime;
	// path as the string, view as the pack
	private Map<Integer, ViewPack> loadingView;
	private Map<String, ReentrantLock> pathLocks;

	protected ImageLoader() {
		if (runtime == null) {
			runtime = new LoaderRuntime();
		}

		runtime = runtime.build();
		loadingView = Collections
				.synchronizedMap(new HashMap<Integer, ViewPack>());
		pathLocks = new WeakHashMap<String, ReentrantLock>();
	}

	public void resume() {
		if (!runtime.paused.get()) {
			LogUtil.w(TAG, "image loader not paused!");
			return;
		}

		runtime.paused.set(false);
		synchronized (runtime.pauseLock) {
			runtime.pauseLock.notifyAll();
		}
	}

	public void pause() {
		if (runtime.paused.get()) {
			LogUtil.w(TAG, "image loader already paused!");
			return;
		}
		runtime.paused.set(true);
	}

	public void stop() {
		// set all task as collected
		for (int vid : loadingView.keySet()) {
			loadingView.get(vid).collect();
		}
		loadingView.clear();
		pathLocks.clear();
		// shutdown all executors.
		((ExecutorService) runtime.executor).shutdownNow();
	}

	public void destroy() {

	}

	public boolean hasImage(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			return false;
		}
		String key = runtime.namer.create(filePath);
		if (runtime.memCache.contains(key)) {
			return true;
		}

		if (runtime.diskCache.contains(key)) {
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

	public synchronized void load(ImageView imageView, String path,
			LoaderListener listener) {
		if (StringUtil.isEmpty(path)) {
			return;
		}

		imageView.setImageBitmap(null);
		String key = runtime.namer.create(path);

		ViewPack vp = new ViewPack(imageView, path);

		if (loadingView.containsKey(vp.getId())) {
			ViewPack oldPack = loadingView.remove(vp.getId());
			String oldPath = oldPack.getPath();
			LogUtil.d(TAG, "collect old view " + oldPath);
			oldPack.collect();
		}

		loadingView.put(vp.getId(), vp);

		LoaderTask task = new LoaderTask();
		ReentrantLock lock = getLock(path);
		LoadInfo loadInfo = new LoadInfo(path, key, vp, lock);
		loadInfo.loader = this;
		task.listener = listener;
		task.runtime = runtime;
		task.loadInfo = loadInfo;
		runtime.executor.execute(task);
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