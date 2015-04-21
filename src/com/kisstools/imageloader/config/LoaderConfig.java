/**
 * @author dawson dong
 */

package com.kisstools.imageloader.config;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kisstools.imageloader.cache.Cache;
import com.kisstools.imageloader.cache.DiskImageCache;
import com.kisstools.imageloader.cache.MemImage;
import com.kisstools.imageloader.cache.MemImageCache;
import com.kisstools.imageloader.namer.FileNamer;
import com.kisstools.imageloader.namer.Namer;
import com.kisstools.thread.KissExecutor;

public class LoaderConfig {

	public Namer namer;

	public Cache<MemImage> memCache;

	public Cache<String> diskCache;

	public Executor remoteExecutor;

	public Executor nativeExecutor;

	public final AtomicBoolean paused;
	public final Object pauseLock;

	public LoaderConfig() {
		this.paused = new AtomicBoolean(false);
		this.pauseLock = new Object();
	}

	public LoaderConfig build() {
		int priority = Thread.NORM_PRIORITY;
		remoteExecutor = KissExecutor.createExecutor(3, priority);
		nativeExecutor = KissExecutor.createExecutor(2, priority);
		memCache = new MemImageCache();
		diskCache = new DiskImageCache();
		this.namer = new FileNamer();
		return this;
	}
}
