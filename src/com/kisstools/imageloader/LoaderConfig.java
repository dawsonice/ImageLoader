/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;

import com.kisstools.imageloader.cache.Cache;
import com.kisstools.imageloader.cache.DiskImageCache;
import com.kisstools.imageloader.cache.MemImageCache;
import com.kisstools.imageloader.conf.FileNamer;
import com.kisstools.imageloader.conf.Namer;
import com.kisstools.thread.KissExecutor;

public class LoaderConfig {

	public Namer namer;

	public Cache<Bitmap> memCache;

	public Cache<String> diskCache;

	public Executor executor;

	public final AtomicBoolean paused;
	public final Object pauseLock;

	public LoaderConfig() {
		this.paused = new AtomicBoolean(false);
		this.pauseLock = new Object();
	}

	public LoaderConfig build() {
		int priority = Thread.NORM_PRIORITY;
		executor = KissExecutor.createExecutor(4, priority);
		memCache = new MemImageCache();
		diskCache = new DiskImageCache();
		this.namer = new FileNamer();
		return this;
	}
}
