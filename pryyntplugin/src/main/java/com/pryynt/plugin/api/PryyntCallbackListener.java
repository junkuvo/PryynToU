package com.pryynt.plugin.api;

public interface PryyntCallbackListener {
	public void onSuccess(PryyntResult result);

	public void onFailure(PryyntResult result);
	
	public void onProgress(int bytesWritten, int totalSize);

	public void onError(Exception e);
}
