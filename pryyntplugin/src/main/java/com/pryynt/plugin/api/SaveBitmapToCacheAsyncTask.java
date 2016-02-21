package com.pryynt.plugin.api;

import java.io.File;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class SaveBitmapToCacheAsyncTask extends AsyncTask<Void, Void, File> {

	private Bitmap bitmap;
	private File cacheDir;
	private SaveBitmapToCacheAsyncTaskListener listener;

	public SaveBitmapToCacheAsyncTask(Bitmap bitmap, File cacheDir, SaveBitmapToCacheAsyncTaskListener listener) {
		this.bitmap = bitmap;
		this.cacheDir = cacheDir;
		this.listener = listener;
	}

	@Override
	protected File doInBackground(Void... params) {
		try {									
			return Utils.saveBitmapToCache(bitmap, cacheDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(File result) {
		super.onPostExecute(result);
		listener.onCompleted(result);		
	}
}