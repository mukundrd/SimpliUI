package com.trayis.simpliui.onboarding;

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
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trayis.simpliui.R;
import com.trayis.simpliui.vector.VectorParser;

import java.text.ParseException;

import static com.trayis.simpliui.onboarding.OnboardingView.BubblePosition.ABOVE_NO_CARAT;
import static com.trayis.simpliui.onboarding.OnboardingView.BubblePosition.BELOW;
import static com.trayis.simpliui.onboarding.OnboardingView.BubblePosition.BELOW_NO_CARAT;

/**
 * Created by Mukund on 16-07-2017.
 */
public class OnboardingView extends CoordinatorLayout implements View.OnClickListener {

    private static final String TAG = "OnboardingView";

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
    private boolean mCarved;

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
     * @param parent        If the focusable view is not directly on an activity, need to provided it's parent reference which is full screen.
     * @param position      @{Link BubblePosition} should it be above or below the focusable view.
     * @param message       Information to be displayed on the tooltip.
     * @param padding       Any padding required for the ficusable view.
     */
    public void focusForView(String path, View focusableView, View parent, int position, String message, int padding) {
        focusForView(path, focusableView, parent, position, message, padding, false);
    }

    /**
     * @param path          To provide vector path for create focus.
     * @param focusableView View, which needs to be focused for on-boarding tooltip.
     * @param parent        If the focusable view is not directly on an activity, need to provided it's parent reference which is full screen.
     * @param position      @{Link BubblePosition} should it be above or below the focusable view.
     * @param message       Information to be displayed on the tooltip.
     * @param padding       Any padding required for the ficusable view.
     * @param force         Force focusing of view even it's already focused
     */
    public void focusForView(String path, View focusableView, View parent, int position, String message, int padding, boolean force) {
        if ((focusableView == null || (mView != null && mView == focusableView)) && !force) {
            return;
        }

        mPath = path;
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

                    if (width == 0 || height == 0) {
                        return;
                    }

                    background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    background.eraseColor(ContextCompat.getColor(getContext(), R.color.defaultOnboardingScrim));
                }

                if (mView != null) {
                    carveForView();
                    mCarved = true;
                    if (getVisibility() != VISIBLE) {
                        setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    private void carveForView() {
        Canvas canvas = new Canvas(background);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Resources resources = getResources();

        int viewWidth = mView.getWidth();
        int viewHeight = mView.getHeight();

        int viewLeft = getRelativeLeft(mView);
        int viewTop = getRelativeTop(mView);
        int viewRight = viewLeft + viewWidth;
        int viewBottom = viewTop + viewHeight;

        if (TextUtils.isEmpty(mPath)) {
            canvas.drawRect(viewLeft, viewTop, viewRight, viewBottom, paint);
        } else {
            try {
                Path path = new VectorParser().parsePath(mPath);

                Matrix scaleMatrix = new Matrix();
                RectF rectF1 = new RectF();
                path.computeBounds(rectF1, true);
                scaleMatrix.setScale(viewWidth / rectF1.width(), viewHeight / rectF1.height(), rectF1.centerX(), rectF1.centerY());
                path.transform(scaleMatrix);

                RectF rectF2 = new RectF();
                path.computeBounds(rectF2, true);

                float offW = rectF2.width() - rectF1.width();
                float offH = rectF2.height() - rectF1.height();
                path.offset(viewLeft + offW / 2, viewTop + offH / 2);

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
                Log.e(TAG, e.getMessage(), e);
            }
        }

        setBackground(new BitmapDrawable(resources, background));

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
        root.findViewById(R.id.btn_got_it).setOnClickListener(this);
        root.findViewById(R.id.never_show_link).setOnClickListener(this);
        root.findViewById(R.id.message_holder).setOnClickListener(this);

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
                    offset += mPadding;
                } else {
                    arrow = findViewById(R.id.up_arrow);
                }
                cParams.topMargin = viewBottom + offset;
            } else {
                findViewById(R.id.up_arrow).setVisibility(GONE);
                if (mPosition == ABOVE_NO_CARAT) {
                    findViewById(R.id.down_arrow).setVisibility(GONE);
                    offset += mPadding;
                } else {
                    arrow = findViewById(R.id.down_arrow);
                }
                cParams.topMargin = (int) ((viewTop + 2.5 * offset) - (offset + root.getHeight()));
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

    private int getRelativeLeft(View myView) {
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
    }

    public void setOnClickCallbackListener(OnClickCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.btn_got_it) {
            if (listener != null) {
                listener.onClickNext(mView);
            }

        } else if (i == R.id.never_show_link) {
            if (listener != null) {
                listener.onClickNeverShow();
            }

        }
    }

    public boolean isCarved() {
        return mCarved;
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
