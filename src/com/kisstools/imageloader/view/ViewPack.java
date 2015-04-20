package com.kisstools.imageloader.view;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kisstools.utils.LogUtil;

public class ViewPack {

	protected Reference<ImageView> viewRef;
	protected String path;

	public ViewPack(ImageView iv, String path) {
		viewRef = new WeakReference<ImageView>(iv);
		this.path = path;
	}

	public int getId() {
		View view = viewRef.get();
		return view == null ? super.hashCode() : view.hashCode();
	}

	public ImageView getImageView() {
		return viewRef.get();
	}

	public String getPath() {
		return this.path;
	}

	public synchronized void collect() {
		viewRef.clear();
	}

	public synchronized boolean collected() {
		return viewRef.get() == null;
	}

	public int getWidth() {
		ImageView imageView = viewRef.get();
		if (imageView == null) {
			return 0;
		}
		final ViewGroup.LayoutParams params = imageView.getLayoutParams();
		int width = 0;
		if (params != null
				&& params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
			width = imageView.getWidth();
		}
		if (width <= 0 && params != null) {
			width = params.width;
		}
		if (width <= 0) {
			width = getFieldValue(imageView, "mMaxWidth");
		}
		return width;
	}

	public int getHeight() {
		ImageView imageView = viewRef.get();
		if (imageView == null) {
			return 0;
		}
		final ViewGroup.LayoutParams params = imageView.getLayoutParams();
		int height = 0;
		if (params != null
				&& params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
			height = imageView.getHeight();
		}
		if (height <= 0 && params != null) {
			height = params.height;
		}
		if (height <= 0) {
			height = getFieldValue(imageView, "mMaxHeight");
		}
		return height;
	}

	private static int getFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			LogUtil.e(e);
		}
		return value;
	}
}
