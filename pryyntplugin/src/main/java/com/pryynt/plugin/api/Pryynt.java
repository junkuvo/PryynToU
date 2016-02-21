package com.pryynt.plugin.api;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

final public class Pryynt {

	private static volatile Pryynt instance;

	private static Pryynt getInstance() {
		Pryynt localInstance = instance;
		if (localInstance == null) {
			synchronized (Pryynt.class) {
				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new Pryynt();
				}
			}
		}
		return localInstance;
	}

	private String mApplicationKey = null;
	private String mPreferredLanguage = null;
	private String mPreferredCurrency = null;
	private String mPryyntBaseUrl = null;

	private int mNumOfUploadedImages = 0;

	private Pryynt() {
		mPreferredLanguage = Locale.getDefault().getISO3Language();
	}

	// Public methods

	/**
	 * Sets application key
	 */
	public static void setApplicationKey(String applicationKey) {
		getInstance().mApplicationKey = applicationKey;
	}

	/**
	 * Sets PRYYNT base URL
	 */
	public static void setPryyntBaseUrl(String baseUrl) {
		getInstance().mPryyntBaseUrl = baseUrl;
	}

	/**
	 * Sets preferred language
	 */
	public static void setPreferredLanguage(String language) {
		getInstance().mPreferredLanguage = language;
	}

	/**
	 * Sets preferred currency
	 */
	public static void setPreferredCurrency(String currency) {
		getInstance().mPreferredCurrency = currency;
	}

	/**
	 * Returns true if session with PRYYNT API is started
	 */
	public static boolean isSessionStarted() {
		if (!isAndroidVersionCorrect()) {
			return false;
		}
		return RestClient.getInstance().isSessionStarted();
	}

	/**
	 * Starts new or restarts session with PRYYNT API
	 */
	public static void startSession(final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		if (getInstance().mApplicationKey == null || getInstance().mApplicationKey.isEmpty()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_APPLICATION_KEY_NOT_SET, null));
			return;
		}
		if (getInstance().mPryyntBaseUrl == null || getInstance().mPryyntBaseUrl.isEmpty()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_BASE_URL_NOT_SET, null));
			return;
		}
		if (getInstance().mPreferredLanguage == null || getInstance().mPreferredLanguage.isEmpty()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_PREFERRED_LANGUAGE_NOT_SET, null));
			return;
		}
		if (getInstance().mPreferredCurrency == null || getInstance().mPreferredCurrency.isEmpty()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_PREFERRED_CURRENCY_NOT_SET, null));
			return;
		}

		getInstance().mNumOfUploadedImages = 0;

		RestClient.getInstance().setApiBaseUrl(getInstance().mPryyntBaseUrl);
		RestClient.getInstance().resetSession();

		RestClient.getInstance().sessionStart(getInstance().mApplicationKey, getInstance().mPreferredLanguage,
				getInstance().mPreferredCurrency, new RestRequestListener<GenericResponse>() {
					@Override
					public void onSuccess(int statusCode, GenericResponse response) {
						callbackListener.onSuccess(PryyntResult.resultSuccess());
					}

					@Override
					public void onFailure(int statusCode, GenericResponse response) {
						callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
								.getErrorMessage()));
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						callbackListener.onProgress(bytesWritten, totalSize);
					}

					@Override
					public void onError(Exception e) {
						callbackListener.onError(e);
					}
				});
	}

	/**
	 * Uploads bitmap to PRYYNT server. Calls internally validation. If image
	 * has insufficient quality - it will not be uploaded
	 */
	public static void uploadImage(final Bitmap bitmap, Context context, final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		SaveBitmapToCacheAsyncTask saveBitmapToCacheAsyncTask = new SaveBitmapToCacheAsyncTask(bitmap,
				context.getCacheDir(), new SaveBitmapToCacheAsyncTaskListener() {
					@Override
					public void onCompleted(final File imageFile) {
						final PryyntCallbackListener operationCallback = new PryyntCallbackListener() {

							@Override
							public void onSuccess(PryyntResult result) {
								imageFile.delete();
								callbackListener.onSuccess(result);
							}

							@Override
							public void onProgress(int bytesWritten, int totalSize) {
								callbackListener.onProgress(bytesWritten, totalSize);
							}

							@Override
							public void onFailure(PryyntResult result) {
								imageFile.delete();
								callbackListener.onFailure(result);
							}

							@Override
							public void onError(Exception e) {
								imageFile.delete();
								callbackListener.onError(e);
							}
						};

						if (!isSessionStarted()) {
							startSession(new PryyntCallbackListener() {

								@Override
								public void onSuccess(PryyntResult result) {
									uploadImageWithValidationInternal(imageFile, operationCallback);
								}

								@Override
								public void onFailure(PryyntResult result) {
									operationCallback.onFailure(result);
								}

								@Override
								public void onProgress(int bytesWritten, int totalSize) {
								}

								@Override
								public void onError(Exception e) {
									operationCallback.onError(e);
								}
							});
							return;
						}
						uploadImageWithValidationInternal(imageFile, operationCallback);
					}
				});
		saveBitmapToCacheAsyncTask.execute(null, null);
	}

	/**
	 * Uploads bitmap to PRYYNT server. Calls internally validation. If image
	 * has insufficient quality - it will not be uploaded
	 */
	public static void uploadImage(String filePath, final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		final File imageFile = new File(filePath);

		if (!isSessionStarted()) {
			startSession(new PryyntCallbackListener() {

				@Override
				public void onSuccess(PryyntResult result) {
					uploadImageWithValidationInternal(imageFile, callbackListener);
				}

				@Override
				public void onFailure(PryyntResult result) {
					callbackListener.onFailure(result);
				}

				@Override
				public void onProgress(int bytesWritten, int totalSize) {
				}

				@Override
				public void onError(Exception e) {
					callbackListener.onError(e);
				}
			});
			return;
		}

		uploadImageWithValidationInternal(imageFile, callbackListener);
	}

	/**
	 * Sends specified URL to PRYYNT server, where image will be downloaded. If
	 * image has insufficient quality - it will not be added
	 */
	public static void uploadImageByUrl(final String imageUrl, final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		if (!isSessionStarted()) {
			startSession(new PryyntCallbackListener() {

				@Override
				public void onSuccess(PryyntResult result) {
					uploadImageByUrlInternal(imageUrl, callbackListener);
				}

				@Override
				public void onFailure(PryyntResult result) {
					callbackListener.onFailure(result);
				}

				@Override
				public void onProgress(int bytesWritten, int totalSize) {
				}

				@Override
				public void onError(Exception e) {
					callbackListener.onError(e);
				}
			});
			return;
		}
		uploadImageByUrlInternal(imageUrl, callbackListener);
	}

	/**
	 * Validates image quality and checks if image is already uploaded by its
	 * checksum
	 */
	public static void validateImageQuality(Bitmap bitmap, Context context,
			final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		SaveBitmapToCacheAsyncTask saveBitmapToCacheAsyncTask = new SaveBitmapToCacheAsyncTask(bitmap,
				context.getCacheDir(), new SaveBitmapToCacheAsyncTaskListener() {
					@Override
					public void onCompleted(final File imageFile) {

						FileInfoLoadAsyncTask fileInfoLoadAsyncTask = new FileInfoLoadAsyncTask(imageFile,
								new FileInfoLoadAsyncTaskListener() {
									@Override
									public void onCompleted(final String checksum, final ImageParams imageParams) {

										imageFile.delete();

										if (!isSessionStarted()) {
											startSession(new PryyntCallbackListener() {

												@Override
												public void onSuccess(PryyntResult result) {
													validateImageQualityInternal(checksum, imageParams.getWidth(),
															imageParams.getHeight(), imageParams.getDpi(),
															callbackListener);
												}

												@Override
												public void onFailure(PryyntResult result) {
													callbackListener.onFailure(result);
												}

												@Override
												public void onProgress(int bytesWritten, int totalSize) {
												}

												@Override
												public void onError(Exception e) {
													callbackListener.onError(e);
												}
											});
											return;
										}
										validateImageQualityInternal(checksum, imageParams.getWidth(),
												imageParams.getHeight(), imageParams.getDpi(), callbackListener);

									}
								});
						fileInfoLoadAsyncTask.execute(null, null);

					}
				});
		saveBitmapToCacheAsyncTask.execute(null, null);
	}

	/**
	 * Validates image quality and checks if image is already uploaded by its
	 * checksum
	 */
	public static void validateImageQuality(String filePath, final PryyntCallbackListener callbackListener) {
		if (!isAndroidVersionCorrect()) {
			callbackListener
					.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null));
			return;
		}

		File imageFile = new File(filePath);

		FileInfoLoadAsyncTask fileInfoLoadAsyncTask = new FileInfoLoadAsyncTask(imageFile,
				new FileInfoLoadAsyncTaskListener() {
					@Override
					public void onCompleted(final String checksum, final ImageParams imageParams) {

						if (!isSessionStarted()) {
							startSession(new PryyntCallbackListener() {

								@Override
								public void onSuccess(PryyntResult result) {
									validateImageQualityInternal(checksum, imageParams.getWidth(),
											imageParams.getHeight(), imageParams.getDpi(), callbackListener);
								}

								@Override
								public void onFailure(PryyntResult result) {
									callbackListener.onFailure(result);
								}

								@Override
								public void onProgress(int bytesWritten, int totalSize) {
								}

								@Override
								public void onError(Exception e) {
									callbackListener.onError(e);
								}
							});
							return;
						}

						validateImageQualityInternal(checksum, imageParams.getWidth(), imageParams.getHeight(),
								imageParams.getDpi(), callbackListener);
						System.gc();
					}
				});
		fileInfoLoadAsyncTask.execute(null, null);
	}

	/**
	 * Opens activity with PRYYNT web site initialized by the current session
	 */
	public static PryyntResult openPryyntPopup(Activity activity) {
		if (!isAndroidVersionCorrect()) {
			return new PryyntResult(PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION, null);
		}

		if (!RestClient.getInstance().isSessionStarted()) {
			return new PryyntResult(PryyntResult.P_RESULT_FAILED_SESSION_IS_NOT_STARTED, null);
		}
		if (getInstance().mNumOfUploadedImages == 0) {
			return new PryyntResult(PryyntResult.P_RESULT_FAILED_NO_IMAGES_LOADED, null);
		}

		Intent startPryyntIntent = new Intent(activity, PryyntAppActivity.class);
		startPryyntIntent.putExtra("BASE_URL", getInstance().mPryyntBaseUrl);
		activity.startActivity(startPryyntIntent);
		return PryyntResult.resultSuccess();
	}

	// Private methods

	private static void uploadImageWithValidationInternal(final File imageFile,
			final PryyntCallbackListener callbackListener) {
		if (!RestClient.getInstance().isSessionStarted()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_SESSION_IS_NOT_STARTED, null));
			return;
		}

		FileInfoLoadAsyncTask fileInfoLoadAsyncTask = new FileInfoLoadAsyncTask(imageFile,
				new FileInfoLoadAsyncTaskListener() {
					@Override
					public void onCompleted(final String checksum, ImageParams imageParams) {
						validateImageQualityInternal(checksum, imageParams.getWidth(), imageParams.getHeight(),
								imageParams.getDpi(), new PryyntCallbackListener() {

									@Override
									public void onSuccess(PryyntResult result) {
										if (result.getResultCode() == PryyntResult.P_RESULT_SUCCESS) {
											uploadImageFileInternal(imageFile, callbackListener);
										} else if (result.getResultCode() == PryyntResult.P_RESULT_SUCCESS_IMAGE_MATCH_BY_CHECKSUM) {
											uploadImageChecksumInternal(checksum, callbackListener);
										}
									}

									@Override
									public void onFailure(PryyntResult result) {
										callbackListener.onFailure(result);
									}

									@Override
									public void onProgress(int bytesWritten, int totalSize) {
									}

									@Override
									public void onError(Exception e) {
										callbackListener.onError(e);
									}
								});
					}
				});
		fileInfoLoadAsyncTask.execute(null, null);
	}

	private static void uploadImageByUrlInternal(String imageUrl, final PryyntCallbackListener callbackListener) {
		if (!RestClient.getInstance().isSessionStarted()) {
			callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED_SESSION_IS_NOT_STARTED, null));
			return;
		}

		RestClient.getInstance().uploadImage(null, imageUrl, null, new RestRequestListener<GenericResponse>() {

			@Override
			public void onSuccess(int statusCode, GenericResponse response) {
				if (response.getStatusCode().intValue() == 0) {
					getInstance().mNumOfUploadedImages++;
					callbackListener.onSuccess(PryyntResult.resultSuccess());
				} else {
					callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
							.getErrorMessage()));
				}
			}

			@Override
			public void onFailure(int statusCode, GenericResponse response) {
				callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response.getErrorMessage()));
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				callbackListener.onProgress(bytesWritten, totalSize);
			}

			@Override
			public void onError(Exception e) {
				callbackListener.onError(e);
			}
		});
	}

	private static void validateImageQualityInternal(String checksum, int width, int height, int dpi,
			final PryyntCallbackListener callbackListener) {

		RestClient.getInstance().validateImageQuality(checksum, width, height, dpi,
				new RestRequestListener<GenericResponse>() {

					@Override
					public void onSuccess(int statusCode, GenericResponse response) {
						if (response.getData() != null
								&& response.getData().equalsIgnoreCase(GenericResponse.R_RESULT_SUCCESS)) {
							callbackListener.onSuccess(PryyntResult.resultSuccess());
						} else if (response.getData() != null
								&& response.getData().equalsIgnoreCase(GenericResponse.R_MEDIA_MATCH_BY_CHECKSUM)) {
							callbackListener.onSuccess(new PryyntResult(
									PryyntResult.P_RESULT_SUCCESS_IMAGE_MATCH_BY_CHECKSUM, response.getErrorMessage()));
						} else {
							callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
									.getErrorMessage()));
						}
					}

					@Override
					public void onFailure(int statusCode, GenericResponse response) {
						callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
								.getErrorMessage()));
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						callbackListener.onProgress(bytesWritten, totalSize);
					}

					@Override
					public void onError(Exception e) {
						callbackListener.onError(e);
					}
				});
	}

	private static void uploadImageFileInternal(File imageFile, final PryyntCallbackListener callbackListener) {
		RestClient.getInstance().uploadImage(null, null, imageFile, new RestRequestListener<GenericResponse>() {

			@Override
			public void onSuccess(int statusCode, GenericResponse response) {
				if (response.getStatusCode().intValue() == 0) {
					getInstance().mNumOfUploadedImages++;
					callbackListener.onSuccess(PryyntResult.resultSuccess());
				} else {
					callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
							.getErrorMessage()));
				}
			}

			@Override
			public void onFailure(int statusCode, GenericResponse response) {
				callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response.getErrorMessage()));
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				callbackListener.onProgress(bytesWritten, totalSize);
			}

			@Override
			public void onError(Exception e) {
				callbackListener.onError(e);
			}
		});
	}

	private static void uploadImageChecksumInternal(String checksum, final PryyntCallbackListener callbackListener) {
		RestClient.getInstance().uploadImage(checksum, null, null, new RestRequestListener<GenericResponse>() {

			@Override
			public void onSuccess(int statusCode, GenericResponse response) {
				if (response.getStatusCode().intValue() == 0) {
					getInstance().mNumOfUploadedImages++;
					callbackListener.onSuccess(PryyntResult.resultSuccess());
				} else {
					callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response
							.getErrorMessage()));
				}
			}

			@Override
			public void onFailure(int statusCode, GenericResponse response) {
				callbackListener.onFailure(new PryyntResult(PryyntResult.P_RESULT_FAILED, response.getErrorMessage()));
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				callbackListener.onProgress(bytesWritten, totalSize);
			}

			@Override
			public void onError(Exception e) {
				callbackListener.onError(e);
			}
		});
	}

	private static boolean isAndroidVersionCorrect() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
}
