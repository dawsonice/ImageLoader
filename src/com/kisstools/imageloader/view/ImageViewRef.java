package com.kisstools.imageloader.view;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import me.dawson.kisstools.utils.LogUtil;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageViewRef {
	protected Reference<ImageViewEx> imageViewRef;
	private String identity;

	public ImageViewRef(ImageViewEx iv) {
		this(iv, null);
	}

	public ImageViewRef(ImageViewEx iv, String id) {
		imageViewRef = new WeakReference<ImageViewEx>(iv);
		identity = id;
	}

	public String getIdentity() {
		return identity;
	}

	public int getWidth() {
		ImageView imageView = imageViewRef.get();
		if (imageView != null) {
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
		return 0;
	}

	public int getHeight() {
		ImageView imageView = imageViewRef.get();
		if (imageView != null) {
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
		return 0;
	}

	public ImageViewEx getWrappedView() {
		return imageViewRef.get();
	}

	public boolean isCollected() {
		return imageViewRef.get() == null;
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
