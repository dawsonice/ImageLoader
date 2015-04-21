/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.util.concurrent.locks.ReentrantLock;

import com.kisstools.imageloader.view.ViewPack;

public class LoaderInfo {

	public static final String TAG = "LoaderInfo";

	public final String key;

	public final ViewPack view;

	public final String path;

	public int total;

	public int current;

	final ReentrantLock pathLock;

	public ImageLoader loader;

	public boolean origin;

	private boolean invalid;

	public LoaderInfo(String path, String key, ViewPack vp, ReentrantLock lock) {
		this.path = path;
		this.key = key;
		this.view = vp;
		this.pathLock = lock;
		this.invalid = false;
		this.total = 0;
		this.current = 0;
	}

	public void invalidate() {
		this.invalid = true;
	}

	public boolean invalid() {
		return (view.collected() || invalid);
	}
}
