package com.pryynt.plugin.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

class NetworkManager {
	
	public static boolean isNetworkAvailable(Context  context) {	
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		boolean isAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected(); 
				
		return isAvailable;
	}
}
