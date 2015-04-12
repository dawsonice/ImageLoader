package com.kisstools.imageloader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.kisstools.imageloader.ImageLoader;
import com.kisstools.utils.LogUtil;

public class ImageViewEx extends ImageView {
	public static final String TAG = "AsyncImageView";

	private String imageId;
	private boolean imageLoaded;

	public ImageViewEx(Context context) {
		this(context, null);
	}

	public ImageViewEx(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoaded = false;
	}

	public void setImageId(String id) {
		if (TextUtils.isEmpty(id)) {
			LogUtil.d(TAG, "invalid id");
			return;
		}

		if (id.equals(imageId)) {
			return;
		}

		imageId = id;
		imageLoaded = false;
		this.setImageBitmap(null);
	}

	public int getViewId() {
		return this.hashCode();
	}

	public String getImageId() {
		return this.imageId;
	}

	// in absolute list view , item invisible
	@Override
	public void onFinishTemporaryDetach() {
		super.onFinishTemporaryDetach();
		LogUtil.d(TAG, "onFinishTemporaryDetach");
		if (!imageLoaded) {
			// ImageLoader.getInstance().loadImage(this, imageId);
		}
	}

	@Override
	public void onStartTemporaryDetach() {
		super.onStartTemporaryDetach();
		LogUtil.d(TAG, "onStartTemporaryDetach");
		if (!imageLoaded) {
			ImageLoader.getInstance().cancel(imageId);
		}
	}

	// view's or its ancestor's visibility changed
	@Override
	public void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);

		boolean visible = visibility == View.VISIBLE;
		LogUtil.d(TAG, "onVisibilityChanged visibility " + visible);

		if (visible) {
			// increase priority of load image
			if (!imageLoaded) {
				// ImageLoader.getInstance().loadImage(this, imageId);
			}
		} else {
			// decrease image
			if (!imageLoaded) {
				ImageLoader.getInstance().cancel(imageId);
			}
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		LogUtil.d(TAG, "onAttachedToWindow " + imageId);
		if (!imageLoaded) {
			// ImageLoader.getInstance().loadImage(this, imageId);
		}
	}

	// view detached from window, will destroy later
	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		LogUtil.d(TAG, "onDetachedFromWindow " + imageId);
		if (!imageLoaded) {
			ImageLoader.getInstance().cancel(imageId);
		}
	}

	public void onImageLoad(Bitmap bitmap) {
		if (bitmap != null) {
			setImageBitmap(bitmap);
			imageLoaded = true;
		}
	}
}
