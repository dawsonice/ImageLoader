/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.io.FileOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;

import com.kisstools.imageloader.conf.ContentLoader;
import com.kisstools.imageloader.conf.FileLoader;
import com.kisstools.imageloader.conf.Loader;
import com.kisstools.imageloader.conf.NetLoader;
import com.kisstools.utils.CloseUtil;
import com.kisstools.utils.FileUtil;
import com.kisstools.utils.LogUtil;
import com.kisstools.utils.MediaUtil;
import com.kisstools.utils.SystemUtil;

public class LoaderImpl implements Runnable {

	public static final String TAG = "LoaderTask";

	public LoaderListener listener;

	public LoaderConfig config;

	public LoaderInfo loadInfo;

	@Override
	public void run() {
		LogUtil.d(TAG, "loader task: " + loadInfo.path);

		loadInfo.pathLock.lock();
		try {
			if (loadInfo.invalid()) {
				LogUtil.e(TAG, "view already collected");
				return;
			}

			if (listener != null && !loadInfo.invalid()) {
				listener.onStart(loadInfo.path);
			}

			Bitmap bitmap = load();

			if (listener != null && !loadInfo.invalid()) {
				listener.onFinish(loadInfo.path, bitmap);
			}

		} catch (Throwable t) {

		} finally {
			loadInfo.pathLock.unlock();
			ImageView imageView = loadInfo.view.getImageView();
			if (imageView != null) {
				loadInfo.loader.cancel(imageView);
			} else {
				loadInfo.loader.cancel(loadInfo.path);
			}
		}
	}

	private Bitmap load() {
		long loadBegin = System.currentTimeMillis();
		checkPause("start");

		Bitmap bitmap = config.memCache.get(loadInfo.key);
		if (bitmap != null) {
			LogUtil.d(TAG, "load image from memory");
			if (!loadInfo.invalid()) {
				ImageView imageView = loadInfo.view.getImageView();
				onImage(bitmap, imageView);
			}
			return bitmap;
		}

		InputStream inputStream = null;
		// save input content to cache folder if need
		boolean cacheData = false;

		if (config.diskCache.contains(loadInfo.key)) {
			LogUtil.d(TAG, "load image from disk cache");
			String absPath = config.diskCache.get(loadInfo.key);
			inputStream = FileUtil.getStream(absPath);
		} else if (loadInfo.path.startsWith("http")
				|| loadInfo.path.startsWith("https")) {
			LogUtil.d(TAG, "load image from net");
			Loader loader = new NetLoader();
			inputStream = loader.load(loadInfo.path);
			cacheData = true;
		} else if (loadInfo.path.startsWith("file")
				|| loadInfo.path.startsWith("/")) {
			LogUtil.d(TAG, "load image from disk");
			FileLoader loader = new FileLoader();
			inputStream = loader.load(loadInfo.path);
			cacheData = true;
		} else if (loadInfo.path.startsWith("content")) {
			LogUtil.d(TAG, "load image from content");
			ContentLoader loader = new ContentLoader();
			inputStream = loader.load(loadInfo.path);
			cacheData = true;
		}

		if (inputStream == null) {
			LogUtil.w(TAG, "failed to get input stream");
			return bitmap;
		}

		try {
			loadInfo.total = inputStream.available();
			LogUtil.d(TAG, "input stream available " + loadInfo.total);
		} catch (Exception e) {

		}

		if (cacheData) {
			LogUtil.d(TAG, "save input stream to cache file");
			String cachePath = MediaUtil.getFileDir("cache") + "/"
					+ System.currentTimeMillis();
			if (!saveCache(cachePath, inputStream)) {
				LogUtil.d(TAG, "load invalid on save input stream");
				FileUtil.delete(cachePath);
				return bitmap;
			}

			LogUtil.d(TAG, "set image to disk cache folder");
			config.diskCache.set(loadInfo.key, cachePath);
			String absPath = config.diskCache.get(loadInfo.key);
			inputStream = FileUtil.getStream(absPath);
		}

		Options options = null;
		int width = loadInfo.view.getWidth();
		int height = loadInfo.view.getHeight();
		if (width > 0 && height > 0) {
			options = new Options();
			options.outWidth = width;
			options.outHeight = height;
		}

		LogUtil.d(TAG, "decode width " + width + " height " + height);
		checkPause("decode");
		if (loadInfo.invalid()) {
			LogUtil.d(TAG, "load invalid before decode. " + loadInfo.path);
			return bitmap;
		}
		long decode = System.currentTimeMillis();
		bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		CloseUtil.close(inputStream);
		long delta = System.currentTimeMillis() - decode;
		LogUtil.d(TAG, "decode image delta " + delta);

		if (bitmap == null) {
			// remove local invalid image cache
			config.diskCache.remove(loadInfo.key);

			LogUtil.e(TAG, "failed to decode bitmap");
			return bitmap;
		}

		config.memCache.set(loadInfo.key, bitmap);
		if (!loadInfo.invalid()) {
			ImageView imageView = loadInfo.view.getImageView();
			onImage(bitmap, imageView);
		} else {
			LogUtil.d(TAG, "view collected on decoded");
		}

		delta = System.currentTimeMillis() - loadBegin;
		LogUtil.d(TAG, "load image delta " + delta + " for " + loadInfo.path);
		return bitmap;
	}

	private boolean saveCache(String absPath, InputStream ips) {
		if (!FileUtil.create(absPath, true)) {
			return false;
		}

		FileOutputStream fos = null;
		boolean interupted = false;
		try {
			fos = new FileOutputStream(absPath);
			byte buffer[] = new byte[10240];
			boolean hasMore = true;
			while (hasMore) {
				checkPause("write");

				int count = ips.read(buffer);
				hasMore = count > 0 && !loadInfo.invalid();
				if (loadInfo.invalid()) {
					// stop loading content
					interupted = true;
				}

				if (!hasMore) {
					break;
				}

				fos.write(buffer, 0, count);
				loadInfo.current += count;

				if (listener != null) {
					listener.onProgress(absPath, loadInfo.current,
							loadInfo.total);
				}
			}
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
			interupted = true;
		} finally {
			CloseUtil.close(fos);
			CloseUtil.close(ips);
		}
		return !interupted;
	}

	private void onImage(final Bitmap bitmap, final ImageView imageView) {
		if (bitmap == null || imageView == null) {
			return;
		}

		SystemUtil.runOnMain(new Runnable() {

			@Override
			public void run() {
				imageView.setImageBitmap(bitmap);
			}

		});
	}

	private void checkPause(String place) {
		if (!config.paused.get()) {
			return;
		}

		synchronized (config.pauseLock) {
			try {
				LogUtil.d(TAG, "paused at place " + place);
				config.pauseLock.wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
