package com.kisstools.imageloader.cache;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import android.text.TextUtils;

import com.kisstools.utils.FileUtil;
import com.kisstools.utils.LogUtil;
import com.kisstools.utils.MediaUtil;
import com.kisstools.utils.StringUtil;

public class DiskImageCache extends BaseCache<String> {

	public static final String TAG = "DiskImageCache";

	private static final long DEFAULT_MAX = 536870912; // 512MB

	private String cacheDir;
	private LinkedHashMap<String, Long> cacheMap;
	private long totalSize;

	public DiskImageCache() {
		totalSize = 0;
		cacheDir = MediaUtil.getFileDir("image");
		cacheMap = new LinkedHashMap<String, Long>(0, 0.75f, true);
		setMaxSize(DEFAULT_MAX);

		if (StringUtil.isEmpty(cacheDir)) {
			LogUtil.e(TAG, "no image cache dir!");
			return;
		}

		if (FileUtil.isFile(cacheDir)) {
			LogUtil.e(TAG, "cache dir is file??");
			return;
		}

		if (!FileUtil.exists(cacheDir)) {
			FileUtil.mkdirs(cacheDir);
		}

		File file = new File(cacheDir);
		File[] children = file.listFiles();
		if (children == null || children.length == 0) {
			return;
		}

		for (File child : children) {
			totalSize += child.length();
			long lastModify = child.lastModified();
			cacheMap.put(child.getName(), lastModify);
		}
	}

	@Override
	public String get(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		String absPath = cacheDir + "/" + key;
		if (FileUtil.exists(absPath)) {
			Long currentTime = System.currentTimeMillis();
			File file = new File(absPath);
			file.setLastModified(currentTime);
			cacheMap.put(key, currentTime);
			return absPath;
		} else {
			return null;
		}
	}

	@Override
	public void set(String key, String imagePath) {
		String absPath = cacheDir + "/" + key;
		if (StringUtil.isEmpty(key) || TextUtils.isEmpty(imagePath)) {
			return;
		}

		if (imagePath.equals(absPath)) {
			return;
		}

		if (FileUtil.exists(absPath)) {
			LogUtil.d(TAG, "delete old image " + key);
			FileUtil.delete(absPath);
		}

		FileUtil.move(imagePath, absPath);
		File file = new File(absPath);
		totalSize += file.length();
		Long currentTime = System.currentTimeMillis();
		file.setLastModified(currentTime);
		cacheMap.put(key, currentTime);

		trim(maxSize);
	}

	@Override
	public boolean contains(String key) {
		String absPath = cacheDir + "/" + key;
		if (!FileUtil.exists(absPath)) {
			return false;
		}

		return cacheMap.containsKey(key);
	}

	@Override
	public String remove(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}
		String absPath = cacheDir + "/" + key;
		if (!FileUtil.exists(absPath)) {
			return null;
		}

		totalSize -= FileUtil.size(absPath);

		FileUtil.delete(absPath);
		cacheMap.remove(key);
		return null;
	}

	@Override
	public long getSize() {
		long totalSize = 0;
		synchronized (this) {
			File file = new File(cacheDir);
			File[] children = file.listFiles();

			if (children == null || children.length == 0) {
				return totalSize;
			}
			for (File child : children) {
				totalSize += child.length();
			}
		}
		return totalSize;
	}

	@Override
	public int getCount() {
		int count = 0;
		synchronized (this) {
			count = FileUtil.childCount(cacheDir);
		}
		return count;
	}

	@Override
	protected void trim(long maxSize) {
		while (true) {
			synchronized (this) {
				if (totalSize <= maxSize || cacheMap.isEmpty()) {
					break;
				}

				Map.Entry<String, Long> entry = cacheMap.entrySet().iterator()
						.next();
				String key = entry.getKey();
				String absPath = cacheDir + key;
				LogUtil.d(TAG, "trim key " + key);
				totalSize -= FileUtil.size(absPath);
				cacheMap.remove(key);
				FileUtil.delete(absPath);
			}
		}
	}

	public String getCacheFolder() {
		return cacheDir;
	}
}
