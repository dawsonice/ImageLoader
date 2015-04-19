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

public class LoaderTask implements Runnable {

	public static final String TAG = "LoaderTask";

	public LoaderListener listener;

	public LoaderProperty loaderProperty;

	public LoadInfo loadInfo;

	@Override
	public void run() {
		LogUtil.d(TAG, "loader task: " + loadInfo.path);

		loadInfo.pathLock.lock();
		try {
			if (loadInfo.view.isCollected()) {
				LogUtil.e(TAG, "view already collected");
				return;
			}

			loadInfo.total = 0;
			loadInfo.current = 0;

			if (listener != null) {
				listener.onStart(loadInfo.path);
			}

			Bitmap bitmap = load();

			if (listener != null) {
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
		Bitmap bitmap = loaderProperty.memCache.get(loadInfo.key);
		ImageView imageView = loadInfo.view.getImageView();
		if (bitmap != null) {
			LogUtil.d(TAG, "load image from memory");
			if (imageView != null) {
				onImage(bitmap, imageView);
			}
			return bitmap;
		}

		InputStream inputStream = null;
		// save input content to cache folder if need
		boolean cacheInput = false;

		if (loaderProperty.diskCache.contains(loadInfo.key)) {
			LogUtil.d(TAG, "load image from disk cache");
			String absPath = loaderProperty.diskCache.get(loadInfo.key);
			inputStream = FileUtil.getStream(absPath);
		} else if (loadInfo.path.startsWith("http")
				|| loadInfo.path.startsWith("https")) {
			LogUtil.d(TAG, "load image from net");
			Loader loader = new NetLoader();
			inputStream = loader.load(loadInfo.path);
			cacheInput = true;
		} else if (loadInfo.path.startsWith("file")
				|| loadInfo.path.startsWith("/")) {
			LogUtil.d(TAG, "load image from disk");
			FileLoader loader = new FileLoader();
			inputStream = loader.load(loadInfo.path);
			cacheInput = true;
		} else if (loadInfo.path.startsWith("content")) {
			LogUtil.d(TAG, "load image from content");
			ContentLoader loader = new ContentLoader();
			inputStream = loader.load(loadInfo.path);
			cacheInput = true;
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

		if (cacheInput) {
			LogUtil.d(TAG, "save input stream to cache file");
			String cachePath = MediaUtil.getFileDir("cache") + "/"
					+ System.currentTimeMillis();
			if (!write(cachePath, inputStream)) {
				return bitmap;
			}

			LogUtil.d(TAG, "set image to disk cache folder");
			loaderProperty.diskCache.set(loadInfo.key, cachePath);
			String absPath = loaderProperty.diskCache.get(loadInfo.key);
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
		long start = System.currentTimeMillis();
		bitmap = BitmapFactory.decodeStream(inputStream, null, options);
		CloseUtil.close(inputStream);
		long delta = System.currentTimeMillis() - start;
		LogUtil.d(TAG, "decode bitmap delta " + delta);

		if (bitmap == null) {
			// remove local invalid image cache
			loaderProperty.diskCache.remove(loadInfo.key);

			LogUtil.e(TAG, "failed to decode bitmap");
			return bitmap;
		}

		loaderProperty.memCache.set(loadInfo.key, bitmap);
		imageView = loadInfo.view.getImageView();
		if (imageView != null) {
			onImage(bitmap, imageView);
		} else {
			LogUtil.d(TAG, "view collected on decoded");
		}
		return bitmap;
	}

	private boolean write(String absPath, InputStream ips) {
		boolean finishReceive = false;
		if (!FileUtil.create(absPath, true)) {
			return finishReceive;
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(absPath);
			byte buffer[] = new byte[10240];
			boolean hasMore = true;
			while (hasMore) {
				int count = ips.read(buffer);
				hasMore = count > 0 && !loadInfo.view.isCollected();
				if (!hasMore) {
					// stop loading content
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
			finishReceive = true;
		} catch (Exception e) {
		} finally {
			CloseUtil.close(fos);
			CloseUtil.close(ips);
		}
		return finishReceive;
	}

	private void onImage(final Bitmap bitmap, final ImageView imageView) {
		SystemUtil.runOnMain(new Runnable() {

			@Override
			public void run() {
				imageView.setImageBitmap(bitmap);
			}

		});
	}
}
