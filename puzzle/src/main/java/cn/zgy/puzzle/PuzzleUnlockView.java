package cn.zgy.puzzle;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
/**
* 拼图解锁view
* @author zhengy
* create at 2020/4/1 上午10:36
**/
public class PuzzleUnlockView extends View {
    /**
     * 完整的锁子
     */
    private Bitmap mBitmapLock;

    /**
     * 缺失钥匙的锁子
     *
     */
    private Bitmap mBitmapLostLock;
    /**
     * 钥匙模版
     */
    private Bitmap mBitmapKeyTemp;
    /**
     * 钥匙
     */
    private Bitmap mBitmapKey;


    //相关坐标
    private int mKeyOffset = 0;//钥匙所处锁子中的上下偏移量
    private Rect mKeyLocation;//钥匙所处位置
    private int mKeyX;//拖动时钥匙所在x坐标
    private int mKeyCenterOffset = 10;//锁芯的偏移量
    private int mDownX;//点击时选中的X坐标
    //相关状态
    private boolean isSucceeded = false;//是否成功
    private boolean isSelected = false;  //是否选中锁子
    //事件回调
    private OnLockResultListener onLockResultListener;//事件回调
    private OnLockClickListener onLockClickListener;//点击回调




    public PuzzleUnlockView(Context context) {
        this(context, null);
    }

    public PuzzleUnlockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置view大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width/4);
    }

    /**
     * 处理素材大小及记录相关坐标
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mKeyOffset = getMeasuredHeight() / 8;//设置钥匙所处Y轴坐标的偏移量
        //初始化素材
        initLockRes(this.mBitmapLock, this.mBitmapKeyTemp);
        initLock(this.mBitmapLock, this.mBitmapKeyTemp);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isSucceeded){
            canvas.drawBitmap(mBitmapLock, 0, 0, null);
        }else {
            canvas.drawBitmap(mBitmapLostLock, 0, 0, null);
            canvas.drawBitmap(mBitmapKey, mKeyX, mKeyOffset / 2, null);
        }
    }

    /**
     * 初始化锁子相关素材
     *
     * @param lock
     * @param keyTemp
     */
    private void initLockRes(Bitmap lock, Bitmap keyTemp) {
        //获取特定大小的锁子
        if(lock == null){
            Bitmap lockBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lock);
            lockBitmap = resizeBitmap(lockBitmap, getMeasuredWidth(), getMeasuredHeight());
            this.mBitmapLock = lockBitmap;
        }else {
            this.mBitmapLock = resizeBitmap(lock, getMeasuredWidth(), getMeasuredHeight());
        }
        //加载锁芯模版
        if(keyTemp == null){
            Bitmap keyTempBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lockkey);
            keyTempBitmap = resizeBitmap(keyTempBitmap, getMeasuredHeight() - mKeyOffset, getMeasuredHeight() - mKeyOffset);
            mBitmapKeyTemp = keyTempBitmap;
        }else {
            this.mBitmapKeyTemp = resizeBitmap(keyTemp, getMeasuredHeight() - this.mKeyOffset, getMeasuredHeight() - this.mKeyOffset);
        }
    }

    /**
     * 将图片绘制为特定大小
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, null, rect, null);
        return result;
    }

    /**
     * 初始化锁子
     */
    private void initLock(Bitmap lock, Bitmap keyTemp) {

        //获取随机x坐标
        int x = CommonExt.getRandom(getMeasuredWidth() - mBitmapKeyTemp.getWidth(), mBitmapKeyTemp.getWidth());
        mKeyLocation = getKeyLocation(x);
        //获取锁子
        mBitmapLostLock = getKeyOrLock(lock, keyTemp, mKeyLocation, 1);
        //获取钥匙
        mBitmapKey = getKeyOrLock(lock, keyTemp, mKeyLocation, 2);

    }
    /**
     * 获取锁子或者钥匙
     *
     * @param lock        锁子
     * @param keyTemp     钥匙模板
     * @param keyLocation 钥匙所处锁子的位置
     * @param mode        1：锁子，2：钥匙
     * @return
     */
    private Bitmap getKeyOrLock(Bitmap lock, Bitmap keyTemp, Rect keyLocation, int mode) {
        //根据mode设置Bitmap的大小
        int width, height;
        if(mode == 2){
            width = keyTemp.getWidth();
            height = keyTemp.getHeight();
        }else {
            width = lock.getWidth();
            height = lock.getHeight();
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        //根据mode设置不同的PorterDuffXfermode达到不同的遮罩效果
        if(mode == 1){
            canvas.drawBitmap(keyTemp, keyLocation.left, keyLocation.top, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            canvas.drawBitmap(lock, 0, 0, paint);
        }else {
            canvas.drawBitmap(keyTemp, 0, 0, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(lock, -keyLocation.left, -keyLocation.top, paint);
        }


        return result;
    }

    /**
     * 根据坐标点X计算钥匙所在位置
     *
     * @param x
     * @return
     */
    private Rect getKeyLocation(int x) {
        int offset = mKeyOffset / 2;
        Rect rect = new Rect(x, offset, x + mBitmapKeyTemp.getWidth(), offset + mBitmapKeyTemp.getHeight());
        return rect;
    }

    //------------------------------------------------------------------------------------------------------------以下为业务相关内容-----------------------------------------------------------------------------------------------------

    /**
     * 开锁结果接口回调
     */
    public interface OnLockResultListener {
        void onResult(boolean result);
    }

    /**
     * 绑定事件接口回调
     *
     * @param onLockResultListener
     */
    public void setOnLockResultListener(OnLockResultListener onLockResultListener) {
        this.onLockResultListener = onLockResultListener;
    }

    /**
     * 锁子被点击后的回调【不包含钥匙被点击】
     */
    public interface OnLockClickListener {
        void onLockClick();
    }

    /**
     * 绑定事件回调
     *
     * @param onLockClickListener
     */
    public void setOnLockClickListener(OnLockClickListener onLockClickListener) {
        this.onLockClickListener = onLockClickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(event.getX() > mBitmapKeyTemp.getWidth()){
                    if(onLockClickListener != null){
                        onLockClickListener.onLockClick();
                    }
                    break;
                }
                mDownX = (int) event.getX();
                isSelected = true;
                break;
                case MotionEvent.ACTION_MOVE:
                    if(isSelected == false){
                        break;
                    }
                    mKeyX = (int)event.getX() - mDownX;
                    invalidate();
                    break;
            case MotionEvent.ACTION_UP:
                //未点击或者移动距离小，则不刷新
                if(isSelected == false || mKeyX < 10){
                    break;
                }
                boolean result = isCoincide((int) event.getX());
                if(result){
                    isSucceeded = true;
                    invalidate();
                }else{

                    refreshLock();
                }
                if(onLockResultListener != null){
                    onLockResultListener.onResult(result);
                }
                break;
                default:
                    break;
        }
        return true;
    }

    public void refreshLock(){
        isSucceeded = false;
        isSelected = false;
        mKeyX = 0;
        initLock(mBitmapLock, mBitmapKeyTemp);
        invalidate();
    }

    /**
     * 是否重合
     * @param x
     * @return
     */
    private boolean isCoincide(int x){
        int offsetLeft = mKeyLocation.left + mDownX - mKeyCenterOffset;
        int offsetRight = mKeyLocation.left + mDownX + mKeyCenterOffset;
        if(x >= offsetLeft && x <= offsetRight){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 设置锁子图片
     *
     * @param bitmapLock
     */
    public void setLockBitmap(Bitmap bitmapLock) {
        this.mBitmapLock = bitmapLock;
        initLockRes(this.mBitmapLock, this.mBitmapKeyTemp);
        this.initLock(this.mBitmapLock, this.mBitmapKeyTemp);
        this.invalidate();
    }
}
