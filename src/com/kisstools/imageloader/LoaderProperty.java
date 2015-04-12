/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.util.concurrent.Executor;

import android.graphics.Bitmap;

import com.kisstools.imageloader.cache.Cache;
import com.kisstools.imageloader.cache.DiskImageCache;
import com.kisstools.imageloader.cache.MemImageCache;
import com.kisstools.imageloader.conf.FileNamer;
import com.kisstools.imageloader.conf.Namer;
import com.kisstools.thread.KissExecutor;

public class LoaderProperty {

	public Namer namer;

	public Cache<Bitmap> memCache;

	public Cache<Bitmap> diskCache;

	public Executor executor;

	public LoaderProperty build() {
		int priority = Thread.NORM_PRIORITY;
		executor = KissExecutor.createExecutor(4, priority);
		memCache = new MemImageCache();
		diskCache = new DiskImageCache();
		this.namer = new FileNamer();
		return this;
	}
}
