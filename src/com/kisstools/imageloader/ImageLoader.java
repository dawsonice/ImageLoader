package com.kisstools.imageloader;

import java.io.File;
import java.util.HashMap;

import me.dawson.kisstools.utils.BitmapUtil;
import me.dawson.kisstools.utils.ImageSize;
import me.dawson.kisstools.utils.LogUtil;
import me.dawson.kisstools.utils.StringUtil;
import android.graphics.Bitmap;

import com.kisstools.imageloader.cache.Cache;
import com.kisstools.imageloader.cache.LruDiskCache;
import com.kisstools.imageloader.cache.LruMemoryCache;
import com.kisstools.imageloader.view.ImageViewRef;

public class ImageLoader {
	public static final String TAG = "ImageLoader";

	private volatile static ImageLoader instance;

	public static ImageLoader getInstance() {
		synchronized (ImageLoader.class) {
			if (instance == null) {
				if (instance == null) {
					instance = new ImageLoader();
				}
			}
		}
		return instance;
	}

	private Cache<Bitmap> memoryCache;
	private Cache<File> diskCache;
	private HashMap<String, ImageViewRef> loadTasks;

	protected ImageLoader() {
	}

	public void init() {
		memoryCache = new LruMemoryCache();
		diskCache = new LruDiskCache();
		loadTasks = new HashMap<String, ImageViewRef>();
	}

	public void destroy() {
	}

	public boolean hasImage(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			return false;
		}

		if (memoryCache.contains(filePath)) {
			return true;
		}

		if (diskCache.contains(filePath)) {
			return true;
		}

		return false;
	}

	public Bitmap getImage(String filePath) {
		return getImage(filePath, null);
	}

	public Bitmap getImage(String filePath, ImageSize size) {
		if (StringUtil.isEmpty(filePath)) {
			return null;
		}

		if (!hasImage(filePath)) {
			LogUtil.w(TAG, "image not cached!");
			return null;
		}

		String cachePath = getCachePath(filePath);
		return BitmapUtil.getImage(cachePath, size);
	}

	public void loadImage(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			LogUtil.w(TAG, "invalid filePath");
			return;
		}

		if (hasImage(filePath)) {
			LogUtil.d(TAG, "image already cached");
			return;
		}
	}

	public void cancelLoad(String filePath) {
		if (StringUtil.isEmpty(filePath)) {
			LogUtil.w(TAG, "invalid filePath");
			return;
		}

		loadTasks.remove(filePath);
	}

	private String getCachePath(String filePath) {
		String localPath = null;
		if (diskCache instanceof LruDiskCache) {
			String cacheFolder = ((LruDiskCache) diskCache).getCacheFolder();
			localPath = cacheFolder + "/" + filePath;
		}
		return localPath;
	}

}