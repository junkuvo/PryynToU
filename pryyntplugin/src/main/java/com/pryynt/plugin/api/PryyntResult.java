package com.pryynt.plugin.api;

final public class PryyntResult {
	
	public static final int P_RESULT_SUCCESS = 0;
	public static final int P_RESULT_FAILED = 1;
	public static final int P_RESULT_FAILED_APPLICATION_KEY_NOT_SET = 2;
	public static final int P_RESULT_FAILED_PREFERRED_LANGUAGE_NOT_SET = 3;
	public static final int P_RESULT_FAILED_PREFERRED_CURRENCY_NOT_SET = 4;
	public static final int P_RESULT_FAILED_SESSION_IS_NOT_STARTED = 5;
	public static final int P_RESULT_FAILED_NO_IMAGES_LOADED = 6;
	public static final int P_RESULT_SUCCESS_IMAGE_MATCH_BY_CHECKSUM = 7;
	public static final int P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION = 8;
	public static final int P_RESULT_FAILED_BASE_URL_NOT_SET = 9;
	
	private int mResultCode;
	private String mErrorMessage;
	
	public static PryyntResult resultSuccess() {
		return new PryyntResult(P_RESULT_SUCCESS, null);
	}
	
	public PryyntResult (int resultCode, String errorMessage)
	{
		mResultCode = resultCode;
		mErrorMessage = errorMessage; 
	}
	
	public int getResultCode() {
		return mResultCode;
	}
	
	public String getErrorMessage() {
		return mErrorMessage;
	}
}
