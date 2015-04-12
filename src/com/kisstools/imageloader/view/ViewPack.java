package com.kisstools.imageloader.view;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kisstools.utils.LogUtil;

public class ViewPack {
	protected Reference<ImageView> viewPack;

	public ViewPack(ImageView iv) {
		viewPack = new WeakReference<ImageView>(iv);
	}

	public int getId() {
		View view = viewPack.get();
		return view == null ? super.hashCode() : view.hashCode();
	}

	public int getWidth() {
		ImageView imageView = viewPack.get();
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
		ImageView imageView = viewPack.get();
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

	public ImageView getPackView() {
		return viewPack.get();
	}

	public boolean isCollected() {
		return viewPack.get() == null;
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
