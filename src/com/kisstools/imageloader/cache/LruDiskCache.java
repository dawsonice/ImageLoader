package com.kisstools.imageloader.cache;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import me.dawson.kisstools.utils.FileUtil;
import me.dawson.kisstools.utils.LogUtil;
import me.dawson.kisstools.utils.MediaUtil;
import me.dawson.kisstools.utils.StringUtil;

public class LruDiskCache extends BaseCache<File> {
	public static final String TAG = "LruDiskCache";

	private static final long DEFAULT_MAX = 536870912; // 512MB

	private String cacheDir;
	private LinkedHashMap<String, Long> cacheMap;
	private long totalSize;

	public LruDiskCache() {
		totalSize = 0;
		cacheDir = MediaUtil.getAppDir() + "/image/";
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
	public File get(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		String absPath = cacheDir + key;
		if (!FileUtil.exists(absPath)) {
			return null;
		} else {
			File file = new File(absPath);
			Long currentTime = System.currentTimeMillis();
			file.setLastModified(currentTime);
			cacheMap.put(key, currentTime);
			return file;
		}
	}

	@Override
	public void set(String key, File file) {
		if (StringUtil.isEmpty(key) || !FileUtil.exists(file)) {
			return;
		}

		String fileName = file.getName();
		String absPath = file.getAbsolutePath();

		boolean child = FileUtil.childOf(absPath, cacheDir);
		if (!child) {
			LogUtil.d(TAG, "file not under cache dir");
			String dstPath = cacheDir + "/" + fileName;
			FileUtil.move(absPath, dstPath);
			file = new File(dstPath);
		}

		totalSize += file.length();

		Long currentTime = System.currentTimeMillis();
		file.setLastModified(currentTime);
		cacheMap.put(key, currentTime);

		trimToSize(maxSize);
	}

	@Override
	public boolean contains(String key) {
		return cacheMap.containsKey(key);
	}

	@Override
	public File remove(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}
		String absPath = cacheDir + key;
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
	protected void trimToSize(long maxSize) {
		while (true) {
			synchronized (this) {
				if (totalSize <= maxSize || cacheMap.isEmpty()) {
					break;
				}

				Map.Entry<String, Long> entry = cacheMap.entrySet().iterator()
						.next();
				String key = entry.getKey();
				String absPath = cacheDir + key;
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
