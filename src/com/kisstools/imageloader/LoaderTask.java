/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.kisstools.imageloader.conf.Loader;
import com.kisstools.imageloader.conf.NetLoader;
import com.kisstools.imageloader.view.ViewPack;
import com.kisstools.utils.BitmapUtil;
import com.kisstools.utils.CloseUtil;
import com.kisstools.utils.LogUtil;
import com.kisstools.utils.SystemUtil;
import com.kisstools.utils.UrlUtil;

public class LoaderTask implements Runnable {

	public static final String TAG = "LoaderTask";

	public String key;

	public String path;

	public ViewPack view;

	public LoaderProperty loaderProperty;

	@Override
	public void run() {
		LogUtil.d(TAG, "loader task: " + path);
		if (TextUtils.isEmpty(path)) {
			return;
		}

		if (loaderProperty.diskCache.contains(key)) {
			LogUtil.d(TAG, "load image from disk cache");
			Bitmap bitmap = loaderProperty.diskCache.get(key);
			onLoad(bitmap);
		} else if (path.startsWith("http") || path.startsWith("https")) {
			LogUtil.d(TAG, "load image from net");
			Loader loader = new NetLoader();
			InputStream is = loader.load(path);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			CloseUtil.close(is);
			if (bitmap != null) {
				loaderProperty.diskCache.set(key, bitmap);
				onLoad(bitmap);
			}
		} else if (path.startsWith("file") || path.startsWith("/")) {
			LogUtil.d(TAG, "load image from disk");
			Uri uri = UrlUtil.parse(path);
			String absPath = uri.getPath();
			Bitmap bitmap = BitmapUtil.getImage(absPath);
			if (bitmap != null) {
				loaderProperty.diskCache.set(key, bitmap);
				onLoad(bitmap);
			}
		} else if (path.startsWith("content")) {
			LogUtil.d(TAG, "load image from content");
		}

	}

	private void onLoad(final Bitmap bitmap) {
		loaderProperty.memCache.set(key, bitmap);

		SystemUtil.runOnMain(new Runnable() {

			@Override
			public void run() {
				ImageView imageView = view.getPackView();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}

		});
	}
}
