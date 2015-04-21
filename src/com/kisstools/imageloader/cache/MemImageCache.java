package com.kisstools.imageloader.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;

import com.kisstools.utils.BitmapUtil;
import com.kisstools.utils.LogUtil;
import com.kisstools.utils.StringUtil;
import com.kisstools.utils.SystemUtil;

public class MemImageCache extends BaseCache<MemImage> {

	public static final String TAG = "MemImageCache";

	private LinkedHashMap<String, MemImage> cacheMap;
	private long totalSize;

	public MemImageCache() {
		maxSize = SystemUtil.getMaxMemory() / 4;
		cacheMap = new LinkedHashMap<String, MemImage>(0, 0.75f, true);
		totalSize = 0;
	}

	@Override
	public void set(String key, MemImage value) {
		if (StringUtil.isEmpty(key) || value == null) {
			return;
		}

		// remove old cache content
		remove(key);

		synchronized (this) {
			cacheMap.put(key, value);
			Bitmap bitmap = value.bitmap;
			totalSize += BitmapUtil.getImageBytes(bitmap);
		}

		// trim total size to max limit
		trim(maxSize);
	}

	@Override
	public MemImage get(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		if (!cacheMap.containsKey(key)) {
			return null;
		}

		return cacheMap.get(key);
	}

	@Override
	public MemImage remove(String key) {
		if (StringUtil.isEmpty(key)) {
			return null;
		}

		if (!cacheMap.containsKey(key)) {
			return null;
		}
		synchronized (this) {
			MemImage memImage = cacheMap.remove(key);
			Bitmap bitmap = memImage.bitmap;
			totalSize -= BitmapUtil.getImageBytes(bitmap);
			return memImage;
		}
	}

	@Override
	public boolean contains(String key) {
		return cacheMap.containsKey(key);
	}

	@Override
	public long getSize() {
		return totalSize;
	}

	@Override
	public int getCount() {
		int count = 0;
		synchronized (this) {
			count = cacheMap.size();
		}
		return count;
	}

	@Override
	public void clear() {
		trim(0);
	}

	protected void trim(long maxSize) {
		while (true) {
			synchronized (this) {
				if (totalSize <= maxSize || cacheMap.isEmpty()) {
					break;
				}

				Map.Entry<String, MemImage> toEvict = cacheMap.entrySet()
						.iterator().next();
				String key = toEvict.getKey();
				Bitmap bitmap = toEvict.getValue().bitmap;
				LogUtil.d(TAG, "trim key " + key);
				cacheMap.remove(key);
				totalSize -= BitmapUtil.getImageBytes(bitmap);
			}
		}
	}
}
