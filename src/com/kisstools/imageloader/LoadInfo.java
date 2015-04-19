/**
 * @author dawson dong
 */

package com.kisstools.imageloader;

import java.util.concurrent.locks.ReentrantLock;

import com.kisstools.imageloader.view.ViewPack;

public class LoadInfo {

	public final String key;

	public final ViewPack view;

	public final String path;

	public int total;

	public int current;

	final ReentrantLock pathLock;

	public ImageLoader loader;

	public LoadInfo(String path, String key, ViewPack vp, ReentrantLock lock) {
		this.path = path;
		this.key = key;
		this.view = vp;
		this.pathLock = lock;
	}
}
