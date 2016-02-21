package apps.junkuvo.localimageviewer;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {

    Bitmap mImage;
    float mCornerRadius = 100f;

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;
    }


    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray tArray =
                context.obtainStyledAttributes(
                        attrs,
                        R.styleable.RoundImageView
                );

        mCornerRadius = tArray.getFloat(R.styleable.RoundImageView_cornerRadius,mCornerRadius);
    }


    @Override
    public void setImageDrawable(Drawable drawable){
        Bitmap image =  ((BitmapDrawable) drawable).getBitmap();
        setRoundBitmap(image);
    }

    @Override
    public void setImageResource(int resId){

        Bitmap image = BitmapFactory.decodeResource(getResources(), resId);

        setRoundBitmap(image);
    }

    @Override
    public void setImageBitmap(Bitmap src){
        setRoundBitmap(src);
    }

    private void setRoundBitmap(Bitmap image){
        int width  = image.getWidth();
        int height = image.getHeight();

        Bitmap clipArea = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(clipArea);
        c.drawARGB(0,0,0,0);
        c.drawRoundRect(new RectF(0, 0, width, height), mCornerRadius, mCornerRadius, new Paint(Paint.ANTI_ALIAS_FLAG));

        if(mImage != null){
            mImage.recycle();
            mImage = null;
        }

        mImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(mImage);
        Paint paint = new Paint();
        canvas.drawBitmap(clipArea, 0, 0, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image, new Rect(0, 0, width, height), new Rect(0, 0, width, height), paint);
        clipArea.recycle();
        clipArea = null;
        super.setImageDrawable(new BitmapDrawable(getContext().getResources(), mImage));

    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();

        super.setImageDrawable(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        } else {
            setBackgroundDrawable(null);
        }

        destroyDrawingCache();

        mImage.recycle();
        mImage = null;
    }
}