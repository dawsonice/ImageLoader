package com.kisstools.imageloader.cache;

public abstract class BaseCache<T> implements Cache<T> {

	protected long maxSize;

	protected abstract void trimToSize(long maxSize);

	@Override
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
		trimToSize(maxSize);
	}

	@Override
	public long getMaxSize() {
		return this.maxSize;
	}

	public void clear() {
		trimToSize(0);
	}
}