package com.jian.android.whiteblackgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2017/4/27.
 */

public class WhiteBlack extends View{

    private int mPanelWidth;
    private float mLineHeight;       //行高
    private int MAX_LINE=10;

    private Paint mPaint=new Paint();

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    private float retioPieceOffset=3*1.0f/4;    //设置棋子大小为行高的四分之三

    private ArrayList<Point> mWhiteArray=new ArrayList<>();
    private ArrayList<Point> mBlackArray=new ArrayList<>();

    private boolean mIsGameOver;
    private boolean mIsWhiteWinner;

    private boolean mIsWhite=true;      //白棋先手  或者当前轮到白棋

    private int MAX_COUNT_IN_LINE=5;

    public WhiteBlack(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(0x44ff0000);

        init();
    }

    /**
     *初始化操作
     */
    private void init(){
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mWhitePiece= BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);
        mBlackPiece= BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);

        int heightSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);

        int width=Math.min(widthSize,heightSize);

        if (widthMode==MeasureSpec.UNSPECIFIED){
            width=heightSize;
        }else if (heightMode==MeasureSpec.UNSPECIFIED){
            width=widthSize;
        }

        setMeasuredDimension(width,width);
    }

    //宽高确定发生改变的时候回调 对尺寸有关的成员变量进行赋值
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth=w;
        mLineHeight=mPanelWidth*1.0f/MAX_LINE;

        int pieceWidth= (int) (mLineHeight*retioPieceOffset);

        mWhitePiece=Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece=Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);

        drawPieces(canvas);

        checkGameOver();
    }

    /**
     *绘制棋子
     * @param canvas
     */
    private void drawPieces(Canvas canvas){

        //绘制白子
        for (int i=0,n=mWhiteArray.size();i<n;i++){

            Point whitePoint=mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x+(1-retioPieceOffset)/2)*mLineHeight,
                    (whitePoint.y+(1-retioPieceOffset)/2)*mLineHeight,null);
        }

        //绘制黑子
        for (int i=0,n=mBlackArray.size();i<n;i++){

            Point blackPoint=mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x+(1-retioPieceOffset)/2)*mLineHeight,
                    (blackPoint.y+(1-retioPieceOffset)/2)*mLineHeight,null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mIsGameOver)  return false;

        int action=event.getAction();
        if (action==MotionEvent.ACTION_UP){        //DOWN的时候 return true 表明当前五子棋view对事件感兴趣

            int x= (int) event.getX();
            int y= (int) event.getY();

            Point p=getValidPoint(x,y);

            //判断当前坐标是否有棋子
            if (mWhiteArray.contains(p) || mBlackArray.contains(p)){
                return false;
            }

            if (mIsWhite){
                mWhiteArray.add(p);
            }else {
                mBlackArray.add(p);
            }
            invalidate();    //请求重绘
            mIsWhite=!mIsWhite;

            return true;
        }

        return true;
    }

    /**
     * 获取用户点击的有效坐标
     * @return
     */
    private Point getValidPoint(int x,int y){

        return new Point((int)(x/mLineHeight),(int)(y/mLineHeight));
    }

    /**
     * 绘制棋盘
     */
    private void drawBoard(Canvas canvas){

        int w=mPanelWidth;
        float lineHeight=mLineHeight;

        //绘制横线
        for (int i=0;i<MAX_LINE;i++){

            int startX= (int) (lineHeight/2);
            int endX= (int) (w-lineHeight/2);

            int y= (int) ((0.5+i)*lineHeight);

            canvas.drawLine(startX,y,endX,y,mPaint);
        }

        //绘制竖线
        for (int i=0;i<MAX_LINE;i++){

            int startY= (int) (lineHeight/2);
            int endY= (int) (w-lineHeight/2);

            int x= (int) ((0.5+i)*lineHeight);

            canvas.drawLine(x,startY,x,endY,mPaint);
        }
    }

    /**
     * 游戏结束检测
     */
    private void checkGameOver(){

        boolean whiteWin=checkFiveInLine(mWhiteArray);
        boolean blackWin=checkFiveInLine(mBlackArray);

        if (whiteWin || blackWin){
            mIsGameOver=true;
            mIsWhiteWinner=whiteWin;

            String result=mIsWhiteWinner?"白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),result,Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFiveInLine(List<Point> points){

        for (Point p:points){

            int x=p.x;
            int y=p.y;

            boolean win=checkHorizontal(x,y,points);
            if (win) return true;
            win=checkVertical(x,y,points);
            if (win) return true;
            win=checkLeftDiagonal(x,y,points);
            if (win) return true;
            win=checkRightDiagonal(x,y,points);
            if (win) return true;
        }

        return false;
    }

    /**
     * 判断棋子是否横向相邻五个一致
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x,int y,List<Point> points){

        int count=1;

        //向左数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        //向右数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }

    /**
     * 判断棋子是否纵向相邻五个一致
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkVertical(int x,int y,List<Point> points){

        int count=1;

        //向上数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x,y-i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        //向下数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x,y+i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }

    /**
     * 判断棋子是否左斜相邻五个一致
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkLeftDiagonal(int x,int y,List<Point> points){

        int count=1;

        //向下数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y+i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        //向上数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y-i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }

    /**
     * 判断棋子是否右斜相邻五个一致
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkRightDiagonal(int x,int y,List<Point> points){

        int count=1;

        //向下数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x+i,y+i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        //向上数
        for (int i=1;i<MAX_COUNT_IN_LINE;i++){
            if (points.contains(new Point(x-i,y-i))){
                count++;
            }else {
                break;
            }
        }

        if (count==MAX_COUNT_IN_LINE) return true;

        return false;
    }

    /**
     * 再来一局
     */
    private void restart(){

        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver=false;
        mIsWhiteWinner=false;
        invalidate();
    }

    //标准的view的存储与恢复

    private static final String INSTANCE="instance";
    private static final String INSTANCE_GAME_OVER="instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY="instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY="instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle=new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY,mBlackArray);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle){

            Bundle bundle= (Bundle) state;
            mIsGameOver=bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray=bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray=bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
        }

        super.onRestoreInstanceState(state);
    }
}
