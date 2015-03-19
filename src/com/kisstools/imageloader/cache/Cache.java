package com.kisstools.imageloader.cache;

public interface Cache<T> {

	public abstract T get(String key);

	public abstract void set(String key, T value);

	public abstract boolean contains(String key);

	public abstract T remove(String key);

	public abstract void setMaxSize(long maxSize);

	public abstract long getMaxSize();

	public abstract long getSize();

	public abstract int getCount();

	public abstract void clear();
}
