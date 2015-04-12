package com.kisstools.imageloader.cache;

public abstract class BaseCache<T> implements Cache<T> {

	protected long maxSize;

	protected abstract void trim(long maxSize);

	@Override
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
		trim(maxSize);
	}

	@Override
	public long getMaxSize() {
		return this.maxSize;
	}

	public void clear() {
		trim(0);
	}
}