package com.example.nancy.swipelayout.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 侧滑删除的自定义控件（类似QQ那种）
 * Created by Nancy on 2016/5/20.
 */
public class SwipeLayout extends ViewGroup {

    private View mContentView;
    private View mDeleteView;
    private int mDeleteWidth;
    private ViewDragHelper mDragHelper;
    private int mContentAndDeleteHeight;

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
                    /**
                     *  是否捕获touch事件
                     * @param child 被触摸的View
                     * @param pointerId 第二个是touch的ID
                     * @return true则捕获该事件
                     */
                    @Override
                    public boolean tryCaptureView(View child, int pointerId) {

                        //如果点击的是这两个就捕获
                        return child == mContentView || child == mDeleteView;
                    }


                    /**
                     *    水平移动的回调（垂直同理）
                     * @param child   触摸的View
                     * @param left child  ！！将要！！移动到的左侧坐标
                     * @param dx 移动增量，相对上一次move（不是第一次的down）右边为正
                     * @return 返回移动到的位置
                     */
                    @Override
                    public int clampViewPositionHorizontal(View child, int left, int dx) {
                        //todo 关键所在：在水平移动的时候禁止父控件拦截事件，此处的含义是，当移动侧滑删除空间的时候，ListView不能
                        //todo 响应上下滑动事件。不加这条的话，基本无法拉出侧滑，原因是侧滑的时候很容易有垂直分量上的移动，
                        //todo 从而导致ListView抢先拦截事件，进而导致侧滑删除失败(ListView是父控件，所以优先响应拦截事件)
                        if (dx != 0) requestDisallowInterceptTouchEvent(true);

                        //防止越界
                        //如果点击了内容
                        if (child == mContentView) {
                            if (left > 0) {
                                left = 0;
                            } else if (left < -mDeleteWidth) {
                                left = -mDeleteWidth;
                            }

                        } else {//如果点击了deleteView
                            if (left < mContentView.getMeasuredWidth() - mDeleteWidth) {
                                left = mContentView.getMeasuredWidth() - mDeleteWidth;
                            } else if (left > mContentView.getMeasuredWidth()) {
                                left = mContentView.getMeasuredWidth();
                            }
                        }


                        return left;

                    }

                    /**
                     * 位置改变时回调
                     * @param changedView 哪个View移动了
                     * @param left 移动后的left值
                     * @param top  移动后的top值
                     * @param dx   相对上次的水平移动增量
                     * @param dy   相对上次的垂直移动增量
                     */
                    @Override
                    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                        if (changedView == mContentView) {
                            //如果mContentView  让mDeleteView也一起移动
                            int l = left + mContentView.getMeasuredWidth();
                            mDeleteView.layout(l, 0, l + mDeleteWidth, mDeleteView.getMeasuredHeight());

                        } else {
                            //如果mDeleteView  让mContentView也一起移动
                            mContentView.layout(left - mContentView.getMeasuredWidth(), 0, left, mContentView.getMeasuredHeight());
                        }
                    }

                    /**
                     * 松开手(up事件)时回调
                     * @param releasedChild 被松开的View
                     * @param xvel x速率速率
                     * @param yvel y速率
                     */
                    @Override
                    public void onViewReleased(View releasedChild, float xvel, float yvel) {
                        float x = mContentView.getX();
                        System.out.println("SwipeLayout.onViewReleased");
                        if (x < -mDeleteWidth / 2) {
                            //如果deleteView露出超过一半 显示deleteView
//                            mContentView.layout(-mDeleteWidth, 0, -mDeleteWidth + mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());

//                         todo  数据模拟，没有实际效果。内部还是封装的Scroller，所以还是需要重写computeScroll方法
                            mDragHelper.smoothSlideViewTo(mContentView, -mDeleteWidth, 0);
//
//                            mDeleteView.layout(mContentView.getMeasuredWidth() - mDeleteWidth, 0,
//                                    mContentView.getMeasuredWidth(), mDeleteView.getMeasuredHeight());

                            mDragHelper.smoothSlideViewTo(mDeleteView, mContentView.getMeasuredWidth() - mDeleteWidth, 0);


                        } else {
                            //显示contentView
//                            mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
                            mDragHelper.smoothSlideViewTo(mContentView, 0, 0);

//                            mDeleteView.layout(mContentView.getMeasuredWidth(), 0,
//                                    mContentView.getMeasuredWidth() + mDeleteView.getMeasuredWidth(), mDeleteView.getMeasuredHeight());

                            mDragHelper.smoothSlideViewTo(mDeleteView, mContentView.getMeasuredWidth(), 0);


                        }


//                        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
//                        invalidate();//刷新:draw-->onDraw-->computeScroll


                        //todo invalidate() ,postInvalidate();和ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);都可以
                        //这里是Main线程
                        invalidate();     //这句不调用就无法启动刷新
                    }
                }

        );
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
//            invalidate(); 下面这个条不行，不会继续移动！

        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentView = getChildAt(0);
        mDeleteView = getChildAt(1);

        LayoutParams params = mDeleteView.getLayoutParams();
        mDeleteWidth = params.width;

        //两个孩子的高度（是一样的）
        mContentAndDeleteHeight = params.height;


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //由ViewDragHelper来处理touch事件
        mDragHelper.processTouchEvent(event);

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    /**
     * 经过代码测试就知道，当我们设置width或height为fill_parent时，容器在布局时调用子view的 measure方法传入的模式是EXACTLY。因为子view会占据剩余容器的空间，所以它大小是确定的。而当设置为wrap_content时，容器 传进去的是AT_MOST, 表示子view的大小最多是多少，这样子view会根据这个上限来设置自己的尺寸。当子view的大小设置为精确值时，容器传入的是EXACTLY, 而MeasureSpec的UNSPECIFIED模式目前还没有发现在什么情况下使用。
     * 　　View的onMeasure方法默认行为是当模式为UNSPECIFIED时，设置尺寸为mMinWidth(通常为0)或者背景drawable的最小尺寸，当模式为EXACTLY或者AT_MOST时，尺寸设置为传入的MeasureSpec的大小。
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        System.out.println("A"+MeasureSpec.getSize(widthMeasureSpec));
//        System.out.println("B"+MeasureSpec.getSize(heightMeasureSpec));
//        //unspiec :0
//        // at_most :- wrap_content
//        //  exa:+ match_parent  80dp
//        System.out.println("AX"+MeasureSpec.getMode(widthMeasureSpec));
//        System.out.println("BX"+MeasureSpec.getMode(heightMeasureSpec));


        //测量孩子
        int childrenHeightSpec = MeasureSpec.makeMeasureSpec(mContentAndDeleteHeight, MeasureSpec.EXACTLY);

        //测量内容
        mContentView.measure(widthMeasureSpec, childrenHeightSpec);
        //测量删除
        int deleteWidth = MeasureSpec.makeMeasureSpec(mDeleteWidth, MeasureSpec.EXACTLY);
        mDeleteView.measure(deleteWidth, childrenHeightSpec);


        //todo 用孩子的高来设定自己的高，很关键！没有这句话，在listView中就高度不正常.另外高度为wrap_content的时候，传递进来的widthMeasureSpec是0（应该是父类无法计算本控件的高度，因为高度由绘制方法决定的）
        heightMeasureSpec = childrenHeightSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        System.out.println("C" + getMeasuredWidth());
//        System.out.println("D" + getMeasuredHeight());

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //给孩子布局
        System.out.println("SwipeLayout.onLayout");
        mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());


        mDeleteView.layout(mContentView.getMeasuredWidth(), 0,
                mContentView.getMeasuredWidth() + mDeleteView.getMeasuredWidth(), mDeleteView.getMeasuredHeight());


    }

    public void close(){
        mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());


        mDeleteView.layout(mContentView.getMeasuredWidth(), 0,
                mContentView.getMeasuredWidth() + mDeleteView.getMeasuredWidth(), mDeleteView.getMeasuredHeight());

    }


}

