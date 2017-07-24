package com.pinssible.keyboardtest.guideviewtest.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * 自定义GuideView
 */
public class GuideMaskView extends RelativeLayout {
    private String TAG = "GuideMaskView";
    private RectF targetViewRectF = new RectF(0, 0, 0, 0);

    //计算出自己的宽高以及位置
    private Rect maskRect = new Rect();

    //透明
    private Bitmap mEraserBitmap;
    private Canvas mEraserCanvas;
    private Paint mEraserPaint; //抠出显示Paint

    /**
     * 被填上蒙版的根View
     */
    protected ViewGroup rootView;
    /**
     * 用来显示的不被遮盖的View
     */
    protected View targetView;


    //可配置属性
    protected int maskColor = 0x66000000;//默认蒙层颜色
    protected float targetRadius = 5;  //圆角矩形显示时的Radius
    protected float targetPadding = 1; //圆角矩形(圆形/椭圆形)显示时的边距Padding
    protected float roundRadius = 0; //圆形显示时候的半径
    protected float ovalXRadius = 0; //椭圆形时x半径
    protected float ovalYRadius = 0; //椭圆形时y半径
    protected TargetShape targetShape = TargetShape.Round_Rectangle; //TargetView显示的形状
    protected boolean canComponentCoverTarget = true; //添加的Component是否可以覆盖住TargetView


    /**
     * 显示的TargetView的形状
     */
    public enum TargetShape {
        Round_Rectangle,//圆角矩形
        Round,//圆形
        Oval //椭圆
    }


    public GuideMaskView(Context context, View targetView, ViewGroup rootView) {
        super(context);
        this.rootView = rootView;
        this.targetView = targetView;
        init(targetView, rootView);
    }


    /**
     * 重新设置rootView与targetView绘制区域
     */
    public void reset() {
        init(targetView, rootView);
    }

    private void init(View targetView, ViewGroup rootView) {
        // mask以及Target的位置以及距离
        rootView.getHitRect(maskRect);
        Log.e(TAG, "maskRect = " + maskRect.toString());
        if (targetView != null) {
            Rect rect = new Rect();
            targetView.getHitRect(rect);
            targetViewRectF = new RectF(rect);
            Log.e(TAG, "targetViewRectF = " + targetViewRectF.toString());
        }


        setWillNotDraw(false);

        if (maskRect.width() > 0) { //蒙层绘制完毕
            //设置Bitmap（蒙层bitmap）
            mEraserBitmap = Bitmap.createBitmap(maskRect.width(), maskRect.height(), Bitmap.Config.ARGB_8888);
            mEraserCanvas = new Canvas(mEraserBitmap);

            //设置抠出显示Paint
            mEraserPaint = new Paint();
            mEraserPaint.setColor(0xFFFFFFFF);
            mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); //将目标区域擦除
            mEraserPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(maskRect.width(), maskRect.height());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (canComponentCoverTarget) {   //优先设置属性是否遮盖住
            drawMask(canvas);
            //最后再去绘制其他View
            super.onDraw(canvas);
        } else {
            //优先绘制其他View
            super.onDraw(canvas);
            drawMask(canvas);
        }
    }

    private void drawMask(Canvas canvas) {
        //绘制蒙层
        mEraserBitmap.eraseColor(Color.TRANSPARENT);
        mEraserCanvas.drawColor(maskColor);

        //擦除目标
        switch (targetShape) {
            case Round_Rectangle:
                RectF rectF = new RectF(targetViewRectF.left - targetPadding, targetViewRectF.top - targetPadding, targetViewRectF.right + targetPadding, targetViewRectF.bottom + targetPadding);
                mEraserCanvas.drawRoundRect(rectF, targetRadius, targetRadius, mEraserPaint);
                break;
            case Round:
                if (roundRadius > 0) {
                    mEraserCanvas.drawCircle(targetViewRectF.centerX(), targetViewRectF.centerY(), roundRadius, mEraserPaint);
                } else {
                    //计算斜边以及Padding
                    float radius = ((float) Math.sqrt(targetViewRectF.height() * targetViewRectF.height() + targetViewRectF.width() * targetViewRectF.width()) / 2) + targetPadding;
                    mEraserCanvas.drawCircle(targetViewRectF.centerX(), targetViewRectF.centerY(), radius, mEraserPaint);
                }
                break;
            case Oval:
                if (ovalXRadius > 0 && ovalYRadius > 0) {
                    RectF ovalF1 = new RectF(targetViewRectF.centerX() - ovalXRadius, targetViewRectF.centerY() - ovalYRadius, targetViewRectF.centerX() + ovalXRadius, targetViewRectF.centerY() + ovalYRadius);
                    mEraserCanvas.drawOval(ovalF1, mEraserPaint);
                } else {
                    RectF ovalF1 = new RectF(targetViewRectF.left - targetPadding, targetViewRectF.top - targetPadding, targetViewRectF.right + targetPadding, targetViewRectF.bottom + targetPadding);
                    mEraserCanvas.drawOval(ovalF1, mEraserPaint);
                }
                break;
        }

        //将目标绘制在View上面
        canvas.drawBitmap(mEraserBitmap, 0, 0, null);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            clearFocus();
            mEraserCanvas.setBitmap(null);
            mEraserBitmap = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加绑定控件
     */
    public void addComponent(GuideComponent component) {
        //设置Component位置
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (component.attachedView.getLayoutParams() != null) {
            lp = (RelativeLayout.LayoutParams) component.attachedView.getLayoutParams();
        }
        lp.topMargin = 0;
        lp.leftMargin = 0;
        switch (component.componentDirection) {
            case LEFT_TOP:
                lp.topMargin = (int) targetViewRectF.top - component.yOffset;
                lp.leftMargin = (int) targetViewRectF.left - component.xOffset;
                break;
            case RIGHT_TOP:
                lp.topMargin = (int) targetViewRectF.top - component.yOffset;
                lp.leftMargin = (int) targetViewRectF.left + component.xOffset;
                break;
            case LEFT_BOTTOM:
                lp.topMargin = (int) targetViewRectF.top + component.yOffset;
                lp.leftMargin = (int) targetViewRectF.left - component.xOffset;
                break;
            case RIGHT_BOTTOM:
                lp.topMargin = (int) targetViewRectF.top + component.yOffset;
                lp.leftMargin = (int) targetViewRectF.left + component.xOffset;
                break;
            default:
                break;
        }

        Log.e(TAG, "addComponent topMargin = " + lp.topMargin);
        Log.e(TAG, "addComponent leftMargin = " + lp.leftMargin);
        component.attachedView.setLayoutParams(lp);
        addView(component.attachedView);
        requestLayout();
        invalidate();
    }


    /**
     * @return {@link #rootView}
     */
    public ViewGroup getMaskRootView() {
        return rootView;
    }

    /**
     * @return @link #targetView}
     */
    public View getTargetView() {
        return targetView;
    }

    /**
     * 移除添加的Component
     */
    public void removeComponent(GuideComponent component) {
        this.removeView(component.attachedView);
    }

    public float getTargetRadius() {
        return targetRadius;
    }

    public void setTargetRadius(float targetRadius) {
        this.targetRadius = targetRadius;
    }

    public float getTargetPadding() {
        return targetPadding;
    }

    public void setTargetPadding(float targetPadding) {
        this.targetPadding = targetPadding;
    }

    public float getRoundRadius() {
        return roundRadius;
    }

    public void setRoundRadius(float roundRadius) {
        this.roundRadius = roundRadius;
    }

    public float getOvalXRadius() {
        return ovalXRadius;
    }

    public void setOvalXRadius(float ovalXRadius) {
        this.ovalXRadius = ovalXRadius;
    }

    public float getOvalYRadius() {
        return ovalYRadius;
    }

    public void setOvalYRadius(float ovalYRadius) {
        this.ovalYRadius = ovalYRadius;
    }

    public TargetShape getTargetShape() {
        return targetShape;
    }

    public void setTargetShape(TargetShape targetShape) {
        this.targetShape = targetShape;
    }

    public boolean isCanComponentCoverTarget() {
        return canComponentCoverTarget;
    }

    public void setCanComponentCoverTarget(boolean canComponentCoverTarget) {
        this.canComponentCoverTarget = canComponentCoverTarget;
    }

    /************************************************************************ builder  ************************************************************************************************/
    public static class Builder {
        /**
         * 默认蒙层颜色
         */
        private int maskColor = 0x66000000;
        /**
         * 圆角矩形显示时的Radius
         */
        private float targetRadius = 5;
        /**
         * 圆角矩形(圆形/椭圆形)显示时的边距Padding
         */
        private float targetPadding = 1;
        /**
         * 圆形显示时候的半径
         */
        private float roundRadius = 0;
        /**
         * 椭圆形时x半径
         */
        private float ovalXRadius = 0;
        /**
         * 椭圆形时y半径
         */
        private float ovalYRadius = 0;
        /**
         * TargetView显示的形状
         */
        private TargetShape targetShape = TargetShape.Round_Rectangle;
        /**
         * 添加的Component是否可以覆盖住TargetView
         */
        private boolean canComponentCoverTarget = true;

        /**
         * 被填上蒙版的根View
         */
        private ViewGroup rootView;
        /**
         * 用来显示的不被遮盖的View
         */
        private View targetView;

        private Context context;

        public Builder(Context context, ViewGroup rootView, View targetView) {
            this.context = context;
            this.rootView = rootView;
            this.targetView = targetView;
        }

        public Builder setMaskColor(int maskColor) {
            this.maskColor = maskColor;
            return this;
        }

        public Builder setTargetRadius(float targetRadius) {
            this.targetRadius = targetRadius;
            return this;
        }

        public Builder setTargetPadding(float targetPadding) {
            this.targetPadding = targetPadding;
            return this;
        }

        public Builder setRoundRadius(float roundRadius) {
            this.roundRadius = roundRadius;
            return this;
        }

        public Builder setOvalXRadius(float ovalXRadius) {
            this.ovalXRadius = ovalXRadius;
            return this;
        }

        public Builder setOvalYRadius(float ovalYRadius) {
            this.ovalYRadius = ovalYRadius;
            return this;
        }

        public Builder setTargetShape(TargetShape targetShape) {
            this.targetShape = targetShape;
            return this;
        }

        public Builder setCanComponentCoverTarget(boolean canComponentCoverTarget) {
            this.canComponentCoverTarget = canComponentCoverTarget;
            return this;
        }

        public Builder setRootView(ViewGroup rootView) {
            this.rootView = rootView;
            return this;
        }

        public Builder setTargetView(View targetView) {
            this.targetView = targetView;
            return this;
        }

        public GuideMaskView build() {
            GuideMaskView maskView = new GuideMaskView(context, targetView, rootView);
            maskView.maskColor = maskColor;
            maskView.targetRadius = targetRadius;
            maskView.targetPadding = targetPadding;
            maskView.roundRadius = roundRadius;
            maskView.ovalXRadius = ovalXRadius;
            maskView.ovalYRadius = ovalYRadius;
            maskView.targetShape = targetShape;
            maskView.canComponentCoverTarget = canComponentCoverTarget;
            return maskView;
        }
    }
}
