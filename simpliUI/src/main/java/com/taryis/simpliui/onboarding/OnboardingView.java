package com.taryis.simpliui.onboarding;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.taryis.simpliui.R;

/**
 * Created by Mukund on 16-07-2017.
 */
public class OnboardingView extends CoordinatorLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private Bitmap background;
    private View mView;
    private int value;
    private int mPosition;
    private String mMessage;
    private LayoutInflater mInflater;
    private View mMessageView;

    public OnboardingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OnboardingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        value = attrs.getAttributeResourceValue(R.styleable.OnboardingView_overlay, 0);
        if (value == 0) {
            value = attrs.getAttributeResourceValue(R.styleable.OnboardingView_overlayDrawable, 0);
        }
        mInflater = LayoutInflater.from(context);
    }

    public void focusForView(View focusableView, int position, String message) {
        background = null;
        mView = focusableView;
        mPosition = position == 1 ? 1 : 0;
        mMessage = message;
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * Callback method to be invoked when the global layout state or the visibility of views
     * within the view tree changes
     */
    @Override
    public void onGlobalLayout() {

        if (background == null) {
            if (value > 0) {
                background = BitmapFactory.decodeResource(getResources(), value);
            }

            if (background == null) {
                background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                background.eraseColor(ContextCompat.getColor(getContext(), R.color.degaultOnboardingOverlay));
            }

            if (mView != null) {
                carveForView(mView);
            }
        }
    }

    private void carveForView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        canvas.drawBitmap(background, 0, 0, paint);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        canvas.drawRect(mView.getLeft(), mView.getTop(), mView.getRight(), mView.getBottom(), paint);

        Resources resources = getResources();
        setBackground(new BitmapDrawable(resources, bitmap));

        if (mMessageView == null) {
            mMessageView = mInflater.inflate(R.layout.onboarding_view, this, false);
        } else {
            removeView(mMessageView);
        }

        CoordinatorLayout.LayoutParams cParams = (CoordinatorLayout.LayoutParams) mMessageView.getLayoutParams();
        View arrow;
        if (mPosition == 0) {
            mMessageView.findViewById(R.id.down_arrow).setVisibility(GONE);
            cParams.topMargin = mView.getBottom() + 5;
            arrow = mMessageView.findViewById(R.id.up_arrow);
        } else {
            mMessageView.findViewById(R.id.up_arrow).setVisibility(GONE);
            cParams.topMargin = mView.getTop() - 5;
            arrow = mMessageView.findViewById(R.id.down_arrow);
        }
        mMessageView.setLayoutParams(cParams);

        RelativeLayout.LayoutParams rParams = (RelativeLayout.LayoutParams) arrow.getLayoutParams();
        rParams.leftMargin = mView.getLeft() + mView.getWidth() / 2 - resources.getDimensionPixelOffset(R.dimen.onboarding_arrow_width) / 2;
        arrow.setLayoutParams(rParams);

        if (!TextUtils.isEmpty(mMessage)) {
            TextView message = mMessageView.findViewById(R.id.onboard_message);
            message.setText(mMessage);
            message.setVisibility(VISIBLE);
        }

        addView(mMessageView);
    }
}
