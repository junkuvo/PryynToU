package com.pryynt.plugin.api;

class ImageParams {
	private int mWidth;
	private int mHeight;
	private int mDpi;

	public ImageParams(int width, int height, int dpi) {
		mWidth = width;
		mHeight = height;
		mDpi = dpi;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public int getDpi() {
		return mDpi;
	}
}
