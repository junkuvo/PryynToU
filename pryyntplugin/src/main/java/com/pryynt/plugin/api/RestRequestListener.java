package com.pryynt.plugin.api;


interface RestRequestListener<T> {

    public void onSuccess(int statusCode, T response);

	public void onFailure(int statusCode, T response);
	
	public void onProgress(int bytesWritten, int totalSize);
	
	public void onError(Exception e);
}