package com.pryynt.plugin.api;

import java.io.File;
import java.io.UnsupportedEncodingException;

import com.loopj.android.http.*;

import org.apache.http.Header;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

class RestClient {

	private static volatile RestClient instance;

	private RestClient() {
	}

	public static RestClient getInstance() {
		RestClient localInstance = instance;
		if (localInstance == null) {
			synchronized (RestClient.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new RestClient();
				}
			}
		}
		return localInstance;
	}

	private String mApiBaseUrl = null;
	//private static final String BASE_URL = "http://192.168.1.67:8080/v1/";

	private String mSessionId = null;

	public void setApiBaseUrl(String apiBaseUrl) {
		mApiBaseUrl = apiBaseUrl + "v1/";
	}
	
	public boolean isSessionStarted() {
		return mSessionId != null;
	}

	public void resetSession() {
		mSessionId = null;
	}

	public String getAPISessionId () {
		return mSessionId; 
	}

	private String getAbsoluteUrl(String relativeUrl) {
		String url = mApiBaseUrl + relativeUrl;

		return url;
	}

	public void get(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(10000);
		client.get(getAbsoluteUrl(url), requestParams, responseHandler);
	}

	public void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler)
			throws UnsupportedEncodingException {
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(300000);
		client.post(getAbsoluteUrl(url), requestParams, responseHandler);
	}

	public void sessionStart(String applicationKey, String language, String currency,
			final RestRequestListener<GenericResponse> listener) {
		try {
			RequestParams requestParams = new RequestParams();
			requestParams.put("application_key", applicationKey);
			requestParams.put("language", language);
			requestParams.put("currency", currency);
			requestParams.put("platform", "android");

			get("common/session-start", requestParams, new BaseJsonHttpResponseHandler<GenericResponse>() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, GenericResponse response) {

					if (response != null && statusCode == 200) {
						mSessionId = response.getData();
						listener.onSuccess(statusCode, response);
					} else {
						listener.onFailure(statusCode, response);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData,
						GenericResponse response) {
					if (response == null) {
						response = new GenericResponse();
					}
					listener.onFailure(statusCode, response);
				}

				@Override
				protected GenericResponse parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
					return new ObjectMapper().readValues(new JsonFactory().createJsonParser(rawJsonData),
							GenericResponse.class).next();
				}
			});
		} catch (Exception e) {
			listener.onError(e);
		}
	}

	public void validateImageQuality(String checksum, int widthInPx, int heightInPx, int dpi,
			final RestRequestListener<GenericResponse> listener) {
		try {
			RequestParams requestParams = new RequestParams();			
			
			requestParams.put("dpi", Integer.toString(dpi));
			requestParams.put("height", Integer.toString(heightInPx));
			requestParams.put("width", Integer.toString(widthInPx));
			requestParams.put("checksum", checksum);
			requestParams.put("session_id", mSessionId);

			post("common/validate-media", requestParams, new BaseJsonHttpResponseHandler<GenericResponse>() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, GenericResponse response) {

					if (response != null && statusCode == 200) {
						listener.onSuccess(statusCode, response);
					} else {
						listener.onFailure(statusCode, response);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData,
						GenericResponse response) {
					if (response == null) {
						response = new GenericResponse();
					}
					listener.onFailure(statusCode, response);
				}

				@Override
				protected GenericResponse parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
					return new ObjectMapper().readValues(new JsonFactory().createJsonParser(rawJsonData),
							GenericResponse.class).next();
				}
			});
		} catch (Exception e) {
			listener.onError(e);
		}
	}

	public void uploadImage(String checksum, String imageUrl, File imageFile,
			final RestRequestListener<GenericResponse> listener) {
		try {
			RequestParams requestParams = new RequestParams();

			requestParams.put("session_id", mSessionId);
			if (checksum != null) {
				requestParams.put("checksum", checksum);
			} else if (imageUrl != null) {
				requestParams.put("url", imageUrl);
			} else if (imageFile != null) {
				requestParams.put("file", imageFile);
				requestParams.setAutoCloseInputStreams(true);
			}

			post("common/upload-media", requestParams, new BaseJsonHttpResponseHandler<GenericResponse>() {

				@Override
				public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, GenericResponse response) {

					if (response != null && statusCode == 200) {
						listener.onSuccess(statusCode, response);
					} else {
						listener.onFailure(statusCode, response);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData,
						GenericResponse response) {
					if (response == null) {
						response = new GenericResponse();
					}
					listener.onFailure(statusCode, response);
				}
				
				@Override
				public void onProgress(int bytesWritten, int totalSize) {
					listener.onProgress(bytesWritten, totalSize);
				}

				@Override
				protected GenericResponse parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
					UploadImageResult uploadImageResult = new ObjectMapper().readValues(new JsonFactory().createJsonParser(rawJsonData),
							UploadImageResult.class).next();
					GenericResponse genericResponse = new GenericResponse();
					genericResponse.setStatusCode(uploadImageResult.getStatusCode());
					genericResponse.setErrorMessage(uploadImageResult.getErrorMessage());
					return genericResponse;
				}
			});
		} catch (Exception e) {
			listener.onError(e);
		}
	}
}
