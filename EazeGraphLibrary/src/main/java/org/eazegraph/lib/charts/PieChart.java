/**
 *
 *   Copyright (C) 2014 Paul Cech
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/**
 * Important:
 *
 * This PieChart partially uses google provided code from their developer website (code sample):
 *
 *      http://developer.android.com/training/custom-views/create-view.html
 *
 * Mainly it's the code which handles the touch and rotation/animation handling. I did not logically modified
 * the code, I only copied and used the bits I needed and renamed the variables.
 * Another function which I extracted from the code sample is the "vectorToScalarScroll(...)" - function.
 * This can be found in the "Utils" - class.
 *
 * That's why I include the Apache License part from the sample:
 *
 * *************************************************************************************************
 *
 *  Copyright (C) 2012 The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.eazegraph.lib.charts;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import org.eazegraph.lib.R;
import org.eazegraph.lib.communication.IOnItemFocusChangedListener;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.utils.Utils;

public class PieChart extends BaseChart {


    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public PieChart(Context context) {
        super(context);

        mUseInnerPadding     = DEF_USE_INNER_PADDING;
        mInnerPadding        = DEF_INNER_PADDING;
        mInnerPaddingOutline = DEF_INNER_PADDING_OUTLINE;
        mHighlightStrength   = DEF_HIGHLIGHT_STRENGTH;
        mUsePieRotation      = DEF_USE_PIE_ROTATION;
        mAutoCenterInSlice   = DEF_AUTO_CENTER;
        mDrawValueInPie      = DEF_DRAW_VALUE_IN_PIE;
        mValueTextSize       = Utils.dpToPx(DEF_VALUE_TEXT_SIZE);
        mValueTextColor      = DEF_VALUE_TEXT_COLOR;
        mUseCustomInnerValue = DEF_USE_CUSTOM_INNER_VALUE;
        mOpenClockwise       = DEF_OPEN_CLOCKWISE;

        initializeGraph();
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p/>
     * <p/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #View(android.content.Context, android.util.AttributeSet, int)
     */
    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PieChart,
                0, 0
        );

        try {

            mUseInnerPadding     = a.getBoolean(R.styleable.PieChart_egUseInnerPadding,     DEF_USE_INNER_PADDING);
            mInnerPadding        = a.getFloat(R.styleable.PieChart_egInnerPadding,          DEF_INNER_PADDING);
            mInnerPaddingOutline = a.getFloat(R.styleable.PieChart_egInnerPaddingOutline,   DEF_INNER_PADDING_OUTLINE);
            mHighlightStrength   = a.getFloat(R.styleable.PieChart_egHighlightStrength,     DEF_HIGHLIGHT_STRENGTH);
            mUsePieRotation      = a.getBoolean(R.styleable.PieChart_egUsePieRotation,      DEF_USE_PIE_ROTATION);
            mAutoCenterInSlice   = a.getBoolean(R.styleable.PieChart_egAutoCenter,          DEF_AUTO_CENTER);
            mDrawValueInPie      = a.getBoolean(R.styleable.PieChart_egDrawValueInPie,      DEF_DRAW_VALUE_IN_PIE);
            mValueTextSize       = a.getDimension(R.styleable.PieChart_egValueTextSize,     Utils.dpToPx(DEF_VALUE_TEXT_SIZE));
            mValueTextColor      = a.getColor(R.styleable.PieChart_egValueTextColor,        DEF_VALUE_TEXT_COLOR);
            mUseCustomInnerValue = a.getBoolean(R.styleable.PieChart_egUseCustomInnerValue, DEF_USE_CUSTOM_INNER_VALUE);
            mOpenClockwise       = a.getBoolean(R.styleable.PieChart_egOpenClockwise,       DEF_OPEN_CLOCKWISE);

        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }

        initializeGraph();
    }

    public void setOnItemFocusChangedListener(IOnItemFocusChangedListener _listener) {
        mListener = _listener;
    }

    public boolean isUseInnerPadding() {
        return mUseInnerPadding;
    }

    public void setUseInnerPadding(boolean _useInnerPadding) {
        mUseInnerPadding = _useInnerPadding;
        onDataChanged();
    }

    public float getInnerPadding() {
        return mInnerPadding;
    }

    public void setInnerPadding(float _innerPadding) {
        mInnerPadding = _innerPadding;
        onDataChanged();
    }

    public float getInnerPaddingOutline() {
        return mInnerPaddingOutline;
    }

    public void setInnerPaddingOutline(float _innerPaddingOutline) {
        mInnerPaddingOutline = _innerPaddingOutline;
        onDataChanged();
    }

    public float getHighlightStrength() {
        return mHighlightStrength;
    }

    public void setHighlightStrength(float _highlightStrength) {
        mHighlightStrength = _highlightStrength;
        for (PieModel model : mPieData) {
            highlightSlice(model);
        };
        invalidate();
    }

    public boolean isAutoCenterInSlice() {
        return mAutoCenterInSlice;
    }

    public void setAutoCenterInSlice(boolean _autoCenterInSlice) {
        mAutoCenterInSlice = _autoCenterInSlice;
    }

    public boolean isUsePieRotation() {
        return mUsePieRotation;
    }

    public void setUsePieRotation(boolean _usePieRotation) {
        mUsePieRotation = _usePieRotation;
    }

    public boolean isDrawValueInPie() {
        return mDrawValueInPie;
    }

    public void setDrawValueInPie(boolean _drawValueInPie) {
        mDrawValueInPie = _drawValueInPie;
        invalidate();
    }

    public float getValueTextSize() {
        return mValueTextSize;
    }

    public void setValueTextSize(float _valueTextSize) {
        mValueTextSize = Utils.dpToPx(_valueTextSize);
        invalidate();
    }

    public int getValueTextColor() {
        return mValueTextColor;
    }

    public void setValueTextColor(int _valueTextColor) {
        mValueTextColor = _valueTextColor;
    }

    /**
     * Returns the index of the currently selected data item.
     *
     * @return The zero-based index of the currently selected data item.
     */
    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * Set the currently selected item. Calling this function will set the current selection
     * and rotate the pie to bring it into view.
     *
     * @param currentItem The zero-based index of the item to select.
     */
    public void setCurrentItem(int currentItem) {
        setCurrentItem(currentItem, true);
    }

    /**
     * Set the current item by index. Optionally, scroll the current item into view. This version
     * is for internal use--the scrollIntoView option is always true for external callers.
     *
     * @param currentItem    The index of the current item.
     * @param scrollIntoView True if the pie should rotate until the current item is centered.
     *                       False otherwise. If this parameter is false, the pie rotation
     *                       will not change.
     */
    private void setCurrentItem(int currentItem, boolean scrollIntoView) {
        mCurrentItem = currentItem;
        if (mListener != null) {
            mListener.onItemFocusChanged(currentItem);
        }
        if (scrollIntoView) {
            centerOnCurrentItem();
        }
        invalidate();
    }

    /**
     * Returns the current rotation of the pie graphic.
     *
     * @return The current pie rotation, in degrees.
     */
    public int getPieRotation() {
        return mPieRotation;
    }

    /**
     * Set the current rotation of the pie graphic. Setting this value may change
     * the current item.
     *
     * @param rotation The current pie rotation, in degrees.
     */
    public void setPieRotation(int rotation) {
        mPieRotation = (rotation % 360 + 360) % 360;
        mGraph.rotateTo(mPieRotation);

        calcCurrentItem();
    }

    public void addPieSlice(PieModel _Slice) {
        highlightSlice(_Slice);
        mPieData.add(_Slice);
        mTotalValue += _Slice.getValue();
        onDataChanged();
    }

    public String getInnerValueString() {
        return mInnerValueString;
    }

    public void setInnerValueString(String _innerValueString) {
        mInnerValueString = _innerValueString;
        mValueView.invalidate();
    }

    public void clearChart() {
        mPieData.clear();
        mTotalValue = 0;
        onDataChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the GestureDetector interpret this event
        boolean result = false;

        if(mUsePieRotation) {
            result = mDetector.onTouchEvent(event);

            // If the GestureDetector doesn't want this event, do some custom processing.
            // This code just tries to detect when the user is done scrolling by looking
            // for ACTION_UP events.
            if (!result) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // User is done scrolling, it's now safe to do things like autocenter
                    stopScrolling();
                    result = true;
                }
            }
        }


        return result;
    }


    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth  = w;
        mHeight = h;

        mGraph.layout(0, 0, mWidth, (int) (mHeight - mLegendHeight));
        mGraph.setPivot(mGraphBounds.centerX(), mGraphBounds.centerY());

        mValueView.layout(0, 0, mWidth, (int) (mHeight - mLegendHeight));

        mLegend.layout(0, (int) (mHeight - mLegendHeight), mWidth, mHeight);

        onDataChanged();
    }

    @Override
    protected void initializeGraph() {
        mPieData = new ArrayList<PieModel>();

        mTotalValue = 0;

        mGraphPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLegendPaint.setTextSize(mLegendTextSize);
        mLegendPaint.setColor(DEF_LEGEND_COLOR);
        mLegendPaint.setStyle(Paint.Style.FILL);

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setTextSize(mValueTextSize);
        mValuePaint.setColor(mValueTextColor);
        mValuePaint.setStyle(Paint.Style.FILL);

        mGraph = new Graph(getContext());
        mGraph.rotateTo(mPieRotation);
        setLayerToSW(mGraph);
        addView(mGraph);

        mValueView = new InnerValueView(getContext());
        addView(mValueView);

        mLegend = new Legend(getContext());
        addView(mLegend);

        mRevealAnimator = ValueAnimator.ofFloat(0, 1);
        mRevealAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRevealValue = animation.getAnimatedFraction();
                invalidate();
            }
        });
        mRevealAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mStartedAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if(mUsePieRotation) {
            // Set up an animator to animate the PieRotation property. This is used to
            // correct the pie's orientation after the user lets go of it.
            mAutoCenterAnimator = ObjectAnimator.ofInt(PieChart.this, "PieRotation", 0);

            // Create a Scroller to handle the fling gesture.
            mScroller = new Scroller(getContext(), null, true);

            // The scroller doesn't have any built-in animation functions--it just supplies
            // values when we ask it to. So we have to have a way to call it every frame
            // until the fling ends. This code (ab)uses a ValueAnimator object to generate
            // a callback on every animation frame. We don't use the animated value at all.
            mScrollAnimator = ValueAnimator.ofFloat(0, 1);
            mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    tickScrollAnimation();
                }
            });

            // Create a gesture detector to handle onTouch messages
            mDetector = new GestureDetector(PieChart.this.getContext(), new GestureListener());

            // Turn off long press--this control doesn't use it, and if long press is enabled,
            // you can't scroll for a bit, pause, then scroll some more (the pause is interpreted
            // as a long press, apparently)
            mDetector.setIsLongpressEnabled(false);
        }

        if(this.isInEditMode()) {
            addPieSlice(new PieModel("Frühstück", 15, Color.parseColor("#FE6DA8")));
            addPieSlice(new PieModel("Mittagessen", 25, Color.parseColor("#56B7F1")));
            addPieSlice(new PieModel("Abendessen", 35, Color.parseColor("#CDA67F")));
            addPieSlice(new PieModel("Snack", 25, Color.parseColor("#FED70E")));
        }
    }

    @Override
    protected void onDataChanged() {
        super.onDataChanged();

        int currentAngle = 0;
        int index = 0;
        int size = mPieData.size();

        for (PieModel model : mPieData) {
            int endAngle = (int) (currentAngle + model.getValue() * 360.f / mTotalValue);
            if(index == size-1) {
                endAngle = 360;
            }

            model.setStartAngle(currentAngle);
            model.setEndAngle(endAngle);
            currentAngle = model.getEndAngle();
            index++;
        }
        calcCurrentItem();
        onScrollFinished();
    }

    private void highlightSlice(PieModel _Slice) {
        // Calculate the highlight color. Saturate at 0xff to make sure that high values
        // don't result in aliasing.
        int color = _Slice.getColor();
        _Slice.setHighlightedColor(Color.argb(
                0xff,
                Math.min((int) (mHighlightStrength * (float) Color.red(color)), 0xff),
                Math.min((int) (mHighlightStrength * (float) Color.green(color)), 0xff),
                Math.min((int) (mHighlightStrength * (float) Color.blue(color)), 0xff)
        ));
    }

    /**
     * Calculate which pie slice is under the pointer, and set the current item
     * field accordingly.
     */
    private void calcCurrentItem() {
        int pointerAngle;

        // calculate the correct pointer angle, depending on clockwise drawing or not
        if(mOpenClockwise) {
            pointerAngle = (mIndicatorAngle + 360 - mPieRotation) % 360;
        }
        else {
            pointerAngle = (mIndicatorAngle + 180 + mPieRotation) % 360;
        }

        for (int i = 0; i < mPieData.size(); ++i) {
            PieModel model = mPieData.get(i);
            if (model.getStartAngle() <= pointerAngle && pointerAngle <= model.getEndAngle()) {
                if (i != mCurrentItem) {
                    setCurrentItem(i, false);
                }
                break;
            }
        }
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setPieRotation(mScroller.getCurrY());
        } else {
            mScrollAnimator.cancel();
            onScrollFinished();
        }
    }

    private void setLayerToSW(View v) {
        if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void setLayerToHW(View v) {
        if (!v.isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * Force a stop to all pie motion. Called when the user taps during a fling.
     */
    private void stopScrolling() {
        mScroller.forceFinished(true);
        mAutoCenterAnimator.cancel();

        onScrollFinished();
    }

    /**
     * Called when the user finishes a scroll action.
     */
    private void onScrollFinished() {
        if (mAutoCenterInSlice) {
            centerOnCurrentItem();
        }
    }

    /**
     * Kicks off an animation that will result in the pointer being centered in the
     * pie slice of the currently selected item.
     */
    private void centerOnCurrentItem() {
        if(!mPieData.isEmpty()) {
            PieModel current = mPieData.get(getCurrentItem());
            int targetAngle;

            if(mOpenClockwise) {
                targetAngle = (mIndicatorAngle - current.getStartAngle()) - ((current.getEndAngle() - current.getStartAngle()) / 2);
                if (targetAngle < 0 && mPieRotation > 0) targetAngle += 360;
            }
            else {
                targetAngle = current.getStartAngle() + (current.getEndAngle() - current.getStartAngle()) / 2;
                targetAngle += mIndicatorAngle;
                if (targetAngle > 270 && mPieRotation < 90) targetAngle -= 360;
            }

            mAutoCenterAnimator.setIntValues(targetAngle);
            mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
        }
    }

    private RectF getGraphBounds() {
        return mGraphBounds;
    }

    @Override
    protected int getDataSize() {
        return mPieData.size();
    }

    //##############################################################################################
    // Graph
    //##############################################################################################
    private class Graph extends View {
        /**
         * Simple constructor to use when creating a view from code.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         */
        private Graph(Context context) {
            super(context);
        }

        /**
         * Implement this to do your drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (!mPieData.isEmpty()) {
                for (PieModel model : mPieData) {
                    mGraphPaint.setColor(model.getColor());

                    // TODO: put calculation in the animation onUpdate method and provide an animated value
                    float startAngle;
                    if(mOpenClockwise) {
                        startAngle = model.getStartAngle() * mRevealValue;
                    }
                    else {
                        startAngle = 360 - model.getEndAngle() * mRevealValue;
                    }

                    float sweepAngle = (model.getEndAngle() - model.getStartAngle()) * mRevealValue;
                    canvas.drawArc(mGraphBounds,
                            startAngle,
                            sweepAngle,
                            true, mGraphPaint);

                    // Draw the highlighted inner edges if an InnerPadding is selected
                    if (mUseInnerPadding) {
                        mGraphPaint.setColor(model.getHighlightedColor());

                        canvas.drawArc(mInnerBounds,
                                startAngle,
                                sweepAngle,
                                true, mGraphPaint);
                    }
                }

                // Draw inner white circle
                if (mUseInnerPadding) {
                    mGraphPaint.setColor(0xFFFFFFFF);

                    if(mOpenClockwise) {
                        canvas.drawArc(mInnerOutlineBounds,
                                0,
                                (360 * mRevealValue),
                                true,
                                mGraphPaint);
                    }
                    else {
                        canvas.drawArc(mInnerOutlineBounds,
                                0,
                                (360 * -mRevealValue),
                                true,
                                mGraphPaint);
                    }
                }
            }
            else {
                // No Data available
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            float newWidth = w - mLeftPadding - mRightPadding;
            float newHeight = h - mTopPadding - mBottomPadding;
            // Figure out how big we can make the pie.
            mPieDiameter = Math.min(newWidth, newHeight);
            mPieRadius = mPieDiameter / 2.f;
            // calculate the left and right space to be center aligned
            float centeredValueWidth  = (w - mPieDiameter) / 2f;
            float centeredValueHeight = (h - mPieDiameter) / 2f;
            mGraphBounds = new RectF(
                    0.0f,
                    0.0f,
                    mPieDiameter,
                    mPieDiameter);
            mGraphBounds.offsetTo(centeredValueWidth, centeredValueHeight);

            mCalculatedInnerPadding         = (mPieRadius / 100) * mInnerPadding;
            mCalculatedInnerPaddingOutline  = (mPieRadius / 100) * mInnerPaddingOutline;

            mInnerBounds = new RectF(
                mGraphBounds.centerX() - mCalculatedInnerPadding - mCalculatedInnerPaddingOutline,
                mGraphBounds.centerY() - mCalculatedInnerPadding - mCalculatedInnerPaddingOutline,
                mGraphBounds.centerX() + mCalculatedInnerPadding + mCalculatedInnerPaddingOutline,
                mGraphBounds.centerY() + mCalculatedInnerPadding + mCalculatedInnerPaddingOutline);

            mInnerOutlineBounds = new RectF(
                    mGraphBounds.centerX() - mCalculatedInnerPadding,
                    mGraphBounds.centerY() - mCalculatedInnerPadding,
                    mGraphBounds.centerX() + mCalculatedInnerPadding,
                    mGraphBounds.centerY() + mCalculatedInnerPadding);

            mGraphWidth  = w;
            mGraphHeight = h;

        }

        public void rotateTo(float pieRotation) {
            mRotation = pieRotation;
            setRotation(pieRotation);
        }

        public void setPivot(float x, float y) {
            mPivot.x = x;
            mPivot.y = y;
            setPivotX(x);
            setPivotY(y);
        }

        private float   mRotation = 0;
        private PointF  mPivot    = new PointF();
    }

    private class InnerValueView extends View {

        /**
         * Simple constructor to use when creating a view from code.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         */
        public InnerValueView(Context context) {
            super(context);
        }

        /**
         * Implement this to do your drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(!mPieData.isEmpty() && mDrawValueInPie) {
                PieModel model = mPieData.get(mCurrentItem);

                // center text in view
                // TODO: put boundary calculation out of the onDraw method
                if(!mUseCustomInnerValue) {
                    mInnerValueString = model.getValue()+"";
                }

                mValuePaint.getTextBounds(mInnerValueString, 0, mInnerValueString.length(), mValueTextBounds);
                canvas.drawText(
                        mInnerValueString,
                        mInnerBounds.centerX() - (mValueTextBounds.width() / 2),
                        mInnerBounds.centerY() + (mValueTextBounds.height() / 2),
                        mValuePaint
                );
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

        }

        private Rect    mValueTextBounds = new Rect();

    }

    //##############################################################################################
    // Legend
    //##############################################################################################
    private class Legend extends View {
        /**
         * Simple constructor to use when creating a view from code.
         *
         * @param context The Context the view is running in, through which it can
         *                access the current theme, resources, etc.
         */
        private Legend(Context context) {
            super(context);
        }

        /**
         * Implement this to do your drawing.
         *
         * @param canvas the canvas on which the background will be drawn
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawPath(mTriangle, mLegendPaint);

            if(!mPieData.isEmpty()) {
                PieModel model = mPieData.get(mCurrentItem);

                // center text in view
                // TODO: move the boundary calculation out of onDraw
                mLegendPaint.getTextBounds(model.getLegendLabel(), 0, model.getLegendLabel().length(), mTextBounds);
                float height = calculateMaxTextHeight(mLegendPaint);
                canvas.drawText(
                        model.getLegendLabel(),
                        (mLegendWidth / 2) - (mTextBounds.width() / 2),
                        mIndicatorSize * 2 + mIndicatorBottomMargin + height,
                        mLegendPaint
                );
            }
            else {
                String str = "No Data available";
                mLegendPaint.getTextBounds(str, 0, str.length(), mTextBounds);
                float height = calculateMaxTextHeight(mLegendPaint);
                canvas.drawText(
                        str,
                        (mLegendWidth / 2) - (mTextBounds.width() / 2),
                        mIndicatorSize * 2 + mIndicatorBottomMargin + height,
                        mLegendPaint
                );
            }
        }

        /**
         * This is called during layout when the size of this view has changed. If
         * you were just added to the view hierarchy, you're called with the old
         * values of 0.
         *
         * @param w    Current width of this view.
         * @param h    Current height of this view.
         * @param oldw Old width of this view.
         * @param oldh Old height of this view.
         */
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mTriangle = new Path();
            mTriangle.moveTo((w / 2) - mIndicatorSize, mIndicatorSize*2);
            mTriangle.lineTo((w / 2) + mIndicatorSize, mIndicatorSize*2);
            mTriangle.lineTo(w / 2, 0);
            mTriangle.lineTo((w / 2) - mIndicatorSize, mIndicatorSize*2);

            mLegendWidth  = w;
            mLegendHeight = h;
        }

        private float mIndicatorSize = Utils.dpToPx(8);
        private float mIndicatorBottomMargin = Utils.dpToPx(4);
        private Path  mTriangle;
        private Rect  mTextBounds = new Rect();
    }

    /**
     * Extends {@link android.view.GestureDetector.SimpleOnGestureListener} to provide custom gesture
     * processing.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Set the pie rotation directly.
            float scrollTheta = Utils.vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.getX() - getGraphBounds().centerX(),
                    e2.getY() - getGraphBounds().centerY());
            setPieRotation(mPieRotation - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Set up the Scroller for a fling
            float scrollTheta = Utils.vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - getGraphBounds().centerX(),
                    e2.getY() - getGraphBounds().centerY());
            mScroller.fling(
                    0,
                    mPieRotation,
                    0,
                    (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            // Start the animator and tell it to animate for the expected duration of the fling.
            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // The user is interacting with the pie, so we want to turn on acceleration
            // so that the interaction is smooth.
            if (isAnimationRunning()) {
                stopScrolling();
            }
            return true;
        }
    }

    private boolean isAnimationRunning() {
        return !mScroller.isFinished() || mAutoCenterAnimator.isRunning();
    }


    //##############################################################################################
    // Variables
    //##############################################################################################

    private static final String LOG_TAG = PieChart.class.getSimpleName();

    public static final float   DEF_INNER_PADDING           = 65.f;
    public static final float   DEF_INNER_PADDING_OUTLINE   = 5.f;
    public static final boolean DEF_USE_INNER_PADDING       = true;
    public static final float   DEF_HIGHLIGHT_STRENGTH      = 1.15f;
    public static final boolean DEF_USE_PIE_ROTATION        = true;
    public static final boolean DEF_AUTO_CENTER             = true;
    public static final boolean DEF_DRAW_VALUE_IN_PIE       = true;
    public static final float   DEF_VALUE_TEXT_SIZE         = 14.f;
    public static final int     DEF_VALUE_TEXT_COLOR        = 0xFF898989;
    public static final boolean DEF_USE_CUSTOM_INNER_VALUE  = false;
    public static final boolean DEF_OPEN_CLOCKWISE          = true;

    /**
     * The initial fling velocity is divided by this amount.
     */
    public static final int     FLING_VELOCITY_DOWNSCALE = 4;

    public static final int     AUTOCENTER_ANIM_DURATION = 250;

    private List<PieModel>      mPieData;

    private Paint               mGraphPaint;
    private Paint               mLegendPaint;
    private Paint               mValuePaint;

    private Graph               mGraph;
    private InnerValueView      mValueView;
    private Legend              mLegend;

    private RectF               mGraphBounds;
    private RectF               mInnerBounds;
    private RectF               mInnerOutlineBounds;

    private float               mPieDiameter;
    private float               mPieRadius;
    private float               mTotalValue;
    private String              mInnerValueString = "";

    private boolean             mUseInnerPadding;
    private float               mInnerPadding;
    private float               mInnerPaddingOutline;
    private float               mHighlightStrength;
    private boolean             mAutoCenterInSlice;
    private boolean             mUsePieRotation;
    private boolean             mDrawValueInPie;
    private float               mValueTextSize;
    private int                 mValueTextColor;
    private boolean             mUseCustomInnerValue;
    private boolean             mOpenClockwise;

    private float               mCalculatedInnerPadding;
    private float               mCalculatedInnerPaddingOutline;

    private int                 mPieRotation;
    // Indicator is located at the bottom
    private int                 mIndicatorAngle = 90;
    private int                 mCurrentItem = 0;

    private ObjectAnimator      mAutoCenterAnimator;
    private Scroller            mScroller;
    private ValueAnimator       mScrollAnimator;
    private GestureDetector     mDetector;

    private IOnItemFocusChangedListener mListener;

}
