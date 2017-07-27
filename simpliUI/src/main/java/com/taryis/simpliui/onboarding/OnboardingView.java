package com.taryis.simpliui.onboarding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.taryis.simpliui.BuildConfig;
import com.taryis.simpliui.R;
import com.taryis.simpliui.vector.VectorParser;

import java.text.ParseException;

import static com.taryis.simpliui.onboarding.OnboardingView.BubblePosition.ABOVE_NO_CARAT;
import static com.taryis.simpliui.onboarding.OnboardingView.BubblePosition.BELOW;
import static com.taryis.simpliui.onboarding.OnboardingView.BubblePosition.BELOW_NO_CARAT;

/**
 * Created by Mukund on 16-07-2017.
 */
public class OnboardingView extends CoordinatorLayout implements View.OnClickListener {

    private static final long ANIMATION_DURATION = 150;

    private Bitmap background;
    private View mView;
    private int value;
    private int mPosition;
    private String mMessage;
    private LayoutInflater mInflater;
    private OnClickCallbackListener listener;
    private int mPadding;
    private String mPath;
    private View mParent;
    private View root;

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

    /**
     * @param path          To provide vector path for create focus.
     * @param focusableView View, which needs to be focused for on-boarding tooltip.
     * @param parent        If the ficusable view is not directly on an activity, need to provided it's parent reference which is full screen.
     * @param position      BubblePosition should it be above or below the focusable view.
     * @param message       Information to be displayed on the tooltip.
     * @param padding       any padding required for the ficusable view.
     */
    public void focusForViewWithPath(String path, View focusableView, View parent, int position, String message, int padding) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        mPath = path;

        focusForView(focusableView, parent, position, message, padding);
    }

    /**
     * @param focusableView View, which needs to be focused for on-boarding tooltip.
     * @param parent        If the ficusable view is not directly on an activity, need to provided it's parent reference which is full screen.
     * @param position      BubblePosition should it be above or below the focusable view.
     * @param message       Information to be displayed on the tooltip.
     * @param padding       any padding required for the ficusable view.
     */
    public void focusForView(View focusableView, View parent, int position, String message, int padding) {
        if (focusableView == null || (mView != null && mView == focusableView)) {
            return;
        }

        background = null;
        mView = focusableView;
        mParent = parent;
        mPosition = position;
        mMessage = message;
        mPadding = padding;

        if (mPosition < BELOW || mPosition > ABOVE_NO_CARAT) {
            mPosition = BELOW;
        }

        post(() -> {
            if (background == null) {
                if (value > 0) {
                    background = BitmapFactory.decodeResource(getResources(), value);
                }

                if (background == null) {
                    int width = getWidth();
                    int height = getHeight();
                    if (width == 0 || height == 0) {
                        View parentView = (View) getParent();
                        if (width == 0) {
                            width = parentView.getWidth();
                        }
                        if (height == 0) {
                            height = parentView.getHeight();
                        }
                    }
                    background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    background.eraseColor(ContextCompat.getColor(getContext(), R.color.defaultOnboardingScrim));
                }

                if (mView != null) {
                    carveForView();
                    if (getVisibility() != VISIBLE) {
                        setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    private void carveForView() {
        Bitmap bitmap = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        canvas.drawBitmap(background, 0, 0, paint);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Resources resources = getResources();

        int viewWidth = mView.getWidth();
        int viewHeight = mView.getHeight();

        int[] location = new int[2];
        //mView.getLocationOnScreen(location);
        mView.getLocationInWindow(location);
        int viewLeft = location[0];
        int viewTop = location[1];
        int viewRight = viewLeft + viewWidth;
        int viewBottom = viewTop + viewHeight;

        if (BuildConfig.DEBUG) {
            Log.v("OnboardingView", String.format("specs (%1$d, %2$d, %3$d, %4$d)", viewLeft, viewTop, viewRight, viewBottom));
        }

        if (TextUtils.isEmpty(mPath)) {
            canvas.drawRect(viewLeft, viewTop, viewRight, viewBottom, paint);
        } else {
            try {
                Path path = new VectorParser().parsePath(mPath);

                Matrix scaleMatrix = new Matrix();
                RectF rectF1 = new RectF();
                path.computeBounds(rectF1, true);
                scaleMatrix.setScale((viewWidth)/rectF1.width(), (viewHeight)/rectF1.height(), rectF1.centerX(), rectF1.centerY());
                path.transform(scaleMatrix);

                RectF rectF2 = new RectF();
                path.computeBounds(rectF2, true);

                float offW = rectF2.width() - rectF1.width();
                float offH = rectF2.height() - rectF1.height();
                path.offset(viewLeft + offW/2, viewTop + offH/2);

                canvas.drawPath(path, paint);

                if (mPadding > 0) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    if (TextUtils.isEmpty(mPath)) {
                        paint.setStrokeCap(Paint.Cap.SQUARE);
                    } else {
                        paint.setStrokeCap(Paint.Cap.ROUND);
                    }
                    paint.setStrokeWidth(mPadding);
                }

                canvas.drawPath(path, paint);

            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("OnboardingView", e.getMessage(), e);
            }
        }

        setBackground(new BitmapDrawable(resources, bitmap));

        if (root != null) {
            View oldView = root;
            oldView.animate().alpha(0).setDuration(ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    removeView(oldView);
                }
            }).start();
        }

        root = LayoutInflater.from(getContext()).inflate(R.layout.onboarding_view, this, false);
        setOnClickListener(this);

        if (!TextUtils.isEmpty(mMessage)) {
            TextView message = root.findViewById(R.id.onboard_message);
            message.setText(mMessage);
            message.setVisibility(VISIBLE);
        }

        post(() -> {
            LayoutParams cParams = (LayoutParams) root.getLayoutParams();
            View arrow = null;
            int offset = resources.getDimensionPixelOffset(R.dimen.size_10dp);
            if (mPosition == BELOW || mPosition == BELOW_NO_CARAT) {
                findViewById(R.id.down_arrow).setVisibility(GONE);
                cParams.topMargin = viewBottom + offset;
                if (mPosition == BELOW_NO_CARAT) {
                    findViewById(R.id.up_arrow).setVisibility(GONE);
                } else {
                    arrow = findViewById(R.id.up_arrow);
                }
            } else {
                findViewById(R.id.up_arrow).setVisibility(GONE);
                cParams.topMargin = (int) ((viewTop + 2.5 * offset) - (offset + root.getHeight()));
                if (mPosition == ABOVE_NO_CARAT) {
                    findViewById(R.id.down_arrow).setVisibility(GONE);
                } else {
                    arrow = findViewById(R.id.down_arrow);
                }
            }
            root.setLayoutParams(cParams);

            if (arrow != null) {
                RelativeLayout.LayoutParams rParams = (RelativeLayout.LayoutParams) arrow.getLayoutParams();
                rParams.leftMargin = viewLeft + mView.getWidth() / 2 - resources.getDimensionPixelOffset(R.dimen.size_50dp) / 2 - getPaddingLeft();
                arrow.setLayoutParams(rParams);
            }

            root.setVisibility(VISIBLE);
            root.animate().alpha(1).setDuration(ANIMATION_DURATION).start();
        });

        addView(root);
    }

    /*private int getRelativeLeft(View myView) {
        View parent = (View) myView.getParent();
        if (parent == mParent || parent.getId() == Window.ID_ANDROID_CONTENT)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft(parent);
    }

    private int getRelativeTop(View myView) {
        View parent = (View) myView.getParent();
        if (parent.getId() == Window.ID_ANDROID_CONTENT)
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }*/

    public void setOnClickCallbackListener(OnClickCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case R.id.btn_got_it:
                if (listener != null) {
                    listener.onClickNext(mView);
                }
                break;
            case R.id.never_show_link:
                if (listener != null) {
                    listener.onClickNeverShow();
                }
                break;
        }*/
    }

    public interface OnClickCallbackListener {
        void onClickNext(View view);

        void onClickNeverShow();
    }

    public @interface BubblePosition {
        int BELOW = 0;
        int ABOVE = 1;
        int BELOW_NO_CARAT = 2;
        int ABOVE_NO_CARAT = 3;
    }
}
