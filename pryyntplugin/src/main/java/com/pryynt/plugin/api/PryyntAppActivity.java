package com.pryynt.plugin.api;

import java.io.File;

import com.pryynt.plugin.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PryyntAppActivity extends Activity {

	private final String LAST_URL = "LAST_URL";
	
	private WebView mWebView;
	private View mRoot;	
	private String mLastUrl;
	private String mBaseUrl;
	
	private static final int FILECHOOSER_RESULTCODE   = 2888;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pryynt_app);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mRoot = getWindow().getDecorView().findViewById(android.R.id.content);
		mWebView = (WebView) mRoot.findViewById(R.id.web_view);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setUseWideViewPort(false);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient() {
            
            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){  
                
                // Update message
                mUploadMessage = uploadMsg;
                 
                try{    
                 
                    // Create AndroidExampleFolder at sdcard                
                    File imageStorageDir = new File(
                                           Environment.getExternalStoragePublicDirectory(
                                           Environment.DIRECTORY_PICTURES)
                                           , "AndroidExampleFolder");
                                            
                    if (!imageStorageDir.exists()) {
                        // Create AndroidExampleFolder at sdcard
                        imageStorageDir.mkdirs();
                    }
                     
                    // Create camera captured image file path and name 
                    File file = new File(
                                    imageStorageDir + File.separator + "IMG_"
                                    + String.valueOf(System.currentTimeMillis()) 
                                    + ".jpg");
                                     
                    mCapturedImageURI = Uri.fromFile(file); 
                     
                    // Camera capture image intent
                    final Intent captureIntent = new Intent(
                                                  android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                   
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                    
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT); 
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                     
                    // Create file chooser intent
                    Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                     
                    // Set camera intent to file chooser 
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                                           , new Parcelable[] { captureIntent });
                     
                    // On select image call onActivityResult method of activity
                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                     
                  }
                 catch(Exception e){
                     Toast.makeText(getBaseContext(), "Exception:"+e, 
                                Toast.LENGTH_LONG).show();
                 }
                 
            }
             
            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }
             
            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg, 
                                       String acceptType, 
                                       String capture) {
                                        
                openFileChooser(uploadMsg, acceptType);
            }
 
 
 
            // The webPage has 2 filechoosers and will send a 
            // console message informing what action to perform, 
            // taking a photo or updating the file
             
            public boolean onConsoleMessage(ConsoleMessage cm) {  
                   
                onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
                return true;
            }
             
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                //Log.d("androidruntime", "Show console messages, Used for debugging: " + message);
                 
            }
        });   // End setWebChromeClient
		
		
		if(savedInstanceState != null) {
			mLastUrl = savedInstanceState.getString(LAST_URL);
		}
		
		mBaseUrl = getIntent().getStringExtra("BASE_URL");
				
		if (mLastUrl == null) {
			String 	openPryyntPageScript = "<html>" +
					"<body>" +
					"<script>" +
					"document.addEventListener(\"DOMContentLoaded\", function(event) {" +
					"var url = '" + mBaseUrl + "mobile/init';" +
					"var session_id = '" + RestClient.getInstance().getAPISessionId() + "';" +
					"var android_version = '" + Build.VERSION.RELEASE + "';" +
					"var form = document.createElement(\"form\");" +
					"document.body.appendChild(form);" +
					"form.method = \"POST\";" +
					"form.action = url;" +
					"var element1 = document.createElement(\"input\");" +
					"element1.value=session_id;" +
					"element1.name=\"session_id\";" +
					"element1.type= 'hidden';" +
					"form.appendChild(element1);" +
					"var element2 = document.createElement(\"input\");" +
					"element2.value=android_version;" +
					"element2.name=\"android_version\";" +
					"element2.type= 'hidden';" +
					"form.appendChild(element2);" +
					"form.submit();" +
					"});" +
					"</script>" +
					"</body>" +
					"</html>";
			mWebView.loadData(openPryyntPageScript, "text/html", "UTF-8");
		} else {
			mWebView.loadUrl(mLastUrl);
		}
	}
	
	// Return here when file selected from camera or from SDcard
    
    @Override 
    protected void onActivityResult(int requestCode, int resultCode,  
                                       Intent intent) { 
         
     if(requestCode==FILECHOOSER_RESULTCODE)  
     {  
        
            if (null == this.mUploadMessage) {
                return;
 
            }
 
           Uri result=null;
            
           try{
                if (resultCode != RESULT_OK) {
                     
                    result = null;
                     
                } else {
                     
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData(); 
                } 
            }
            catch(Exception e)
            {
                Toast.makeText(getApplicationContext(), "activity :"+e,
                 Toast.LENGTH_LONG).show();
            }
             
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
     }
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		mLastUrl = mWebView.getUrl();
		outState.putString(LAST_URL, mLastUrl);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		onBackPressed();
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}