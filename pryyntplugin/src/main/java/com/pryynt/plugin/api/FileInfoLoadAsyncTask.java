package com.pryynt.plugin.api;

import java.io.File;
import android.os.AsyncTask;

public class FileInfoLoadAsyncTask extends AsyncTask<Void, Void, FileInfo> {

	private File imageFile;
	private FileInfoLoadAsyncTaskListener listener;

	public FileInfoLoadAsyncTask(File imageFile, FileInfoLoadAsyncTaskListener listener) {
		this.imageFile = imageFile;
		this.listener = listener;
	}

	@Override
	protected FileInfo doInBackground(Void... params) {
		try {			
			FileInfo fileInfo = new FileInfo();
			fileInfo.checksum = Utils.getMd5Checksum(imageFile);
			fileInfo.imageParams = Utils.getImageParams(imageFile);			
			return fileInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(FileInfo result) {
		super.onPostExecute(result);
		listener.onCompleted(result.checksum, result.imageParams);		
	}
}
