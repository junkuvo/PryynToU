package apps.junkuvo.localimageviewer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.pryynt.plugin.api.Pryynt;
import com.pryynt.plugin.api.PryyntCallbackListener;
import com.pryynt.plugin.api.PryyntResult;
import com.software.shell.fab.ActionButton;

import io.fabric.sdk.android.Fabric;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GALLERY = 0;
    private static final int REQUEST_CAMERA = 1;
    private ProgressDialog mProgressDialog;
    private Context mContext;

    private ActionButton mOrder;
    private ActionButton mSelect;
    private ActionButton mCamera;

    private Animation mAnimationBlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();

        Fabric.with(fabric);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
        txtTitle.setTypeface(Typeface.SERIF, Typeface.ITALIC);
        TextView txtMessage = (TextView)findViewById(R.id.txtCenterMessage);
        txtMessage.setTypeface(Typeface.SERIF,Typeface.ITALIC);

        mContext = this;
        mAnimationBlink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        mCamera = (ActionButton) findViewById(R.id.fabCamera);
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCameraIntent();
            }
        });
        mCamera.setVisibility(View.INVISIBLE);

        int buttonColor = ContextCompat.getColor(mContext,R.color.fab_material_light_green_500);
        int buttonPressedColor = ContextCompat.getColor(mContext,R.color.fab_material_light_green_900);
        mSelect = (ActionButton) findViewById(R.id.fabSelect);
        mSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callGalleryIntent();
            }
        });
        mSelect.setImageResource(R.drawable.ic_image);
        mSelect.setButtonColor(buttonColor);
        mSelect.setButtonColorPressed(buttonPressedColor);
        mSelect.setAnimation(mAnimationBlink);

        mOrder = (ActionButton) findViewById(R.id.fabOrder);
        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPryyntWebSite();
            }
        });
        mOrder.setImageResource(R.drawable.ic_shopping_cart);
        buttonColor = ContextCompat.getColor(mContext,R.color.fab_material_light_blue_500);
        buttonPressedColor = ContextCompat.getColor(mContext,R.color.fab_material_light_blue_900);
        mOrder.setButtonColor(buttonColor);
        mOrder.setButtonColorPressed(buttonPressedColor);

        Pryynt.setPryyntBaseUrl("https://pryynt.me/");
        Pryynt.setApplicationKey("56bed2744974056bed274497e4");
        Pryynt.setPreferredCurrency("USD");
        Pryynt.setPreferredLanguage("EN");

        mBitmapArray = new ArrayList<Bitmap>();
        mRecyclerView = (RecyclerViewPager) findViewById(R.id.list);
        mRecyclerViewAdapter = new RecyclerViewAdapter(mContext,mBitmapArray);
        LinearLayoutManager layout = new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layout);

        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    private RecyclerViewPager mRecyclerView;
    private ArrayList<Bitmap> mBitmapArray;
    private RecyclerViewAdapter mRecyclerViewAdapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    uploadImageBitmapToPryynt(selectedImage, data);
                    (findViewById(R.id.txtCenterMessage)).setVisibility(View.GONE);
                    setImage(data);
                }
                break;
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    uploadImageFileToPryynt(selectedImage);
                    ( findViewById(R.id.txtCenterMessage)).setVisibility(View.GONE);
                    setImage(data);

                }
                break;
        }

        if(resultCode == RESULT_OK){
            mOrder.setVisibility(View.VISIBLE);
            mOrder.setAnimation(mAnimationBlink);
            mSelect.setType(ActionButton.Type.DEFAULT);

            CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams( // 親 View の LayoutParams を指定
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.END|Gravity.BOTTOM;
            int marginDp = (int)convertDPtoPX(16);
            lp.setMargins(marginDp, marginDp, marginDp, marginDp);
            mSelect.setLayoutParams(lp);
            mSelect.clearAnimation();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPryyntWebSite() {
        PryyntResult pryyntResult = Pryynt.openPryyntPopup(this);
        if (pryyntResult.getResultCode() != PryyntResult.P_RESULT_SUCCESS) {
            Toast.makeText(mContext, "Failed to open PRYYNT web site. " + getErrorReason(pryyntResult),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void callGalleryIntent(){
        // ギャラリー呼び出し
//        Intent intent = new Intent();
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);


    }

    private void callCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void setImage(Intent data){
        Bitmap bitmap = null;
        try {
            if(data.getData()==null){
                bitmap = (Bitmap)data.getExtras().get("data");
            }else{
                InputStream in = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(in);
                in.close();
//                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        imgView.setLayoutParams(layoutParams);
        // 選択した画像を表示
//        imgView.setImageBitmap(bitmap);

        // ☆Tip　Adapterを作り直してセットし直さないと、Arrayの先頭に視点が移動しない
        mBitmapArray.add(0, bitmap);
        mRecyclerViewAdapter = null;
        mRecyclerViewAdapter = new RecyclerViewAdapter(mContext,mBitmapArray);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    // camera
    private void uploadImageBitmapToPryynt(Uri imageUri, Intent data) {
        Bitmap bitmap = null;
        try {
            if(data.getData()==null){
                bitmap = (Bitmap)data.getExtras().get("data");
            }else{
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            }

//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            showProgressDialog();
            Pryynt.uploadImage(bitmap, this, new PryyntCallbackListener() {

                @Override
                public void onSuccess(PryyntResult result) {
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, "Successfully uploaded image", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(PryyntResult result) {
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, "Failed to upload image. " + getErrorReason(result), Toast.LENGTH_LONG)
                            .show();
                }

                @Override
                public void onProgress(int bytesWritten, int totalSize) {
                    mProgressDialog.setProgress((int) ((float) bytesWritten / (float) totalSize * 100.f));
                }

                @Override
                public void onError(Exception e) {
                    mProgressDialog.dismiss();
                    Toast.makeText(mContext, "Failed to upload image. Error occurred: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // image
    private void uploadImageFileToPryynt(Uri imageUri) {

        showProgressDialog();

        String filePath = getRealPathFromURI(imageUri);

        Pryynt.uploadImage(filePath, new PryyntCallbackListener() {

            @Override
            public void onSuccess(PryyntResult result) {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, "Successfully uploaded image", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(PryyntResult result) {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, "Failed to upload image. " + getErrorReason(result), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                mProgressDialog.setProgress((int) ((float) bytesWritten / (float) totalSize * 100.f));
            }

            @Override
            public void onError(Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(mContext, "Failed to upload image. Error occurred: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        });

    }

    private void uploadImageUrlToPryynt(String imageUrl) {
        Pryynt.uploadImageByUrl(imageUrl, new PryyntCallbackListener() {

            @Override
            public void onSuccess(PryyntResult result) {
                Toast.makeText(mContext, "Successfully uploaded image", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(PryyntResult result) {
                Toast.makeText(mContext, "Failed to upload image. " + getErrorReason(result), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(mContext, "Failed to upload image. Error occurred: " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();

            }
        });
    }

    @SuppressLint("NewApi")
    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("Uploading Image to Pryynt");
        mProgressDialog.setMessage("You can delete uploaded images in Pryynt anytime");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mProgressDialog.setProgressNumberFormat(null);
        }
        mProgressDialog.setProgress(0);
        mProgressDialog.setMax(100);
        mProgressDialog.show();
    }

    private String getErrorReason(PryyntResult pryyntResult) {
        String errorReason = "";
        if (pryyntResult.getResultCode() != PryyntResult.P_RESULT_SUCCESS) {

            switch (pryyntResult.getResultCode()) {
                case PryyntResult.P_RESULT_FAILED_SESSION_IS_NOT_STARTED:
                    errorReason = "Session is not started";
                    break;
                case PryyntResult.P_RESULT_FAILED_NO_IMAGES_LOADED:
                    errorReason = "No images uploaded";
                    break;
                case PryyntResult.P_RESULT_FAILED_APPLICATION_KEY_NOT_SET:
                    errorReason = "Application key is not set";
                    break;
                case PryyntResult.P_RESULT_FAILED_BASE_URL_NOT_SET:
                    errorReason = "Pryynt API base url is not set";
                    break;
                case PryyntResult.P_RESULT_FAILED_PREFERRED_CURRENCY_NOT_SET:
                    errorReason = "Preferred currency is not set";
                    break;
                case PryyntResult.P_RESULT_FAILED_PREFERRED_LANGUAGE_NOT_SET:
                    errorReason = "Preferred language is not set";
                    break;
                case PryyntResult.P_RESULT_FAILED_UNSUPPORTED_ANDROID_VERSION:
                    errorReason = "Android version is too low";
                    break;
                case PryyntResult.P_RESULT_FAILED:
                    errorReason = pryyntResult.getErrorMessage() != null ? pryyntResult.getErrorMessage() : "";
                    break;
            }
        }

        return errorReason;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private float convertDPtoPX(float dp){
        float d = mContext.getResources().getDisplayMetrics().density;
        return dp * d + 0.5f;
    }
}
