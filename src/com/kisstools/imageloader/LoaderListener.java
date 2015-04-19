/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import android.graphics.Bitmap;

public interface LoaderListener {

	public void onStart(String path);

	public void onProgress(String path, int current, int total);

	public void onFinish(String path, Bitmap bitmap);

}
