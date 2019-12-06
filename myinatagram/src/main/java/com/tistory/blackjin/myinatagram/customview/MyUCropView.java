package com.tistory.blackjin.myinatagram.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;

public class MyUCropView extends FrameLayout {

    private final OverlayView mViewOverlay;
    private GestureCropImageView mGestureCropImageView;

    public MyUCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyUCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true);
        mGestureCropImageView = findViewById(R.id.image_view_crop);
        mViewOverlay = findViewById(R.id.view_overlay);

        //크롭시 표시되는 안내선 없애기
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
        //mViewOverlay.processStyledAttributes(a);
        //mGestureCropImageView.processStyledAttributes(a);
        //a.recycle();

        //크롭 시 패딩 간역 없애기
        mViewOverlay.setPadding(0, 0, 0, 0);
        setListenersToViews();
    }

    private void setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(mViewOverlay::setTargetAspectRatio);
        mViewOverlay.setOverlayViewChangeListener(cropRect -> mGestureCropImageView.setCropRect(cropRect));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @NonNull
    public GestureCropImageView getCropImageView() {
        return mGestureCropImageView;
    }

    @NonNull
    public OverlayView getOverlayView() {
        return mViewOverlay;
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    public void resetCropImageView() {
        removeView(mGestureCropImageView);
        mGestureCropImageView = new GestureCropImageView(getContext());
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }

    /**
     * Method for change gestureCropImageView
     */
    public void changeCropImageView(GestureCropImageView cropImageView) {
        removeView(mGestureCropImageView);
        mGestureCropImageView = cropImageView;
        setListenersToViews();
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect());
        addView(mGestureCropImageView, 0);
    }
}