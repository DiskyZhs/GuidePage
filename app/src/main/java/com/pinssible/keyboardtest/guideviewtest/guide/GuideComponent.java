package com.pinssible.keyboardtest.guideviewtest.guide;

import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 相对与某个TargetView的指导View
 * Created by ZhangHaoSong on 2017/7/20.
 */

public class GuideComponent {
    /**
     * 用于显示捆绑的View
     */
    protected View attachedView;

    /**
     * 添加到的蒙版View
     */
    protected GuideMaskView maskView;

    /**
     * {@link ComponentDirection}
     */
    protected ComponentDirection componentDirection;

    /**
     * 距离Target，x轴方向距离
     */
    protected int xOffset;

    /**
     * 距离Target,y轴方向距离
     */
    protected int yOffset;


    /**
     * 距离TargetView 的方向
     */
    public enum ComponentDirection {
        LEFT_TOP, //左上
        LEFT_BOTTOM,  //左下
        RIGHT_TOP, //右上
        RIGHT_BOTTOM  //右下
    }

    public GuideComponent(View attachedView, GuideMaskView maskView, ComponentDirection componentDirection, int xOffset, int yOffset) {
        this.attachedView = attachedView;
        this.maskView = maskView;
        this.componentDirection = componentDirection;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public View getAttachedView() {
        return attachedView;
    }

    public GuideMaskView getMaskView() {
        return maskView;
    }

    public ComponentDirection getComponentDirection() {
        return componentDirection;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    /**
     * 建造者
     */
    public static class Builder {
        /**
         * 用于显示捆绑的View
         */
        private View attachedView;

        /**
         * 添加到的蒙版View
         */
        private GuideMaskView maskView;

        /**
         * {@link ComponentDirection}
         */
        private ComponentDirection componentDirection;

        /**
         * 距离Target，x轴方向距离
         */
        private int xOffset;

        /**
         * 距离Target,y轴方向距离
         */
        private int yOffset;


        public Builder(View attachedView) {
            this.attachedView = attachedView;
            componentDirection = ComponentDirection.LEFT_TOP;
            xOffset = 0;
            yOffset = 0;
        }

        public void setMaskView(GuideMaskView maskView) {
            this.maskView = maskView;
        }

        /**
         * @param componentDirection {@link ComponentDirection}
         */
        public Builder setComponentDirection(ComponentDirection componentDirection) {
            this.componentDirection = componentDirection;
            return this;
        }

        /**
         * @param xOffset {@link #xOffset}
         */
        public Builder setXOffset(int xOffset) {
            this.xOffset = xOffset;
            return this;
        }


        /**
         * @param yOffset {@link #yOffset}
         */
        public Builder setYOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public GuideComponent build() {
            return new GuideComponent(attachedView, maskView, componentDirection, xOffset, yOffset);
        }
    }


    /**
     * 将Component显示在蒙版上面
     *
     * @param maskView 蒙版View
     */
    public void showOnMaskView(GuideMaskView maskView) {
        this.maskView = maskView;
        maskView.getTargetView().getViewTreeObserver().addOnGlobalLayoutListener(new TargetViewLayoutListener(this, maskView));
        maskView.getTargetView().invalidate(); //刷新
    }

    /**
     * 将Component显示在蒙版上面
     */
    public void show() {
        if (maskView != null) {
            maskView.getTargetView().getViewTreeObserver().addOnGlobalLayoutListener(new TargetViewLayoutListener(this, maskView));
            maskView.getTargetView().invalidate(); //刷新
        }
    }

    /**
     * 将Component移除
     */
    public void dismiss() {
        if (maskView != null) {
            maskView.removeComponent(this);
        }
    }

    private static class TargetViewLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        private GuideMaskView view;
        private GuideComponent component;

        public TargetViewLayoutListener(GuideComponent component, GuideMaskView maskView) {
            this.view = maskView;
            this.component = component;
        }

        @Override
        public void onGlobalLayout() {
            //重新初始化
            view.reset();
            //添加AttachedView
            view.addComponent(component);
            //Container添加蒙版
            view.getMaskRootView().addView(view);
            //蒙版最高层显示
            view.bringToFront();
            view.getTargetView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
