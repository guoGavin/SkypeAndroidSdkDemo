package com.gavinguo.skypevideo;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以自由拖动的View
 * Created by gavinguo on 5/6/2015.
 */
public class DragView extends LinearLayout implements View.OnClickListener,AudioViewMenuOperationResultListener {

    private Context context;
    /**手势监听类**/
    private GestureDetector gestureDetector;
    /** 记录移动的最后的位置 **/
    int lastX, lastY,downX,downY,upX,upY;
    /** 记录屏幕的宽和高 **/
    int screenWidth,screenHeight;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private List<DragMuneClickListener> dragMuneClickListenerList = new ArrayList<>();

    private RelativeLayout close,open,mute,speaker,pause;
    private ImageView closeIcon,openIcon,muteIcon,speakerIcon,pauseIcon;

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        gestureDetector = new GestureDetector(context,listener);
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.draw_view_layout, this);
        initView();
    }

    private void initView(){
        close = (RelativeLayout) findViewById(R.id.close);
        open = (RelativeLayout) findViewById(R.id.open);
        mute = (RelativeLayout) findViewById(R.id.mute);
        speaker = (RelativeLayout) findViewById(R.id.speaker);
        pause = (RelativeLayout) findViewById(R.id.pause);
        closeIcon = (ImageView) findViewById(R.id.close_icon);
        openIcon = (ImageView) findViewById(R.id.open_icon);
        muteIcon = (ImageView) findViewById(R.id.mute_icon);
        speakerIcon = (ImageView) findViewById(R.id.speaker_icon);
        pauseIcon = (ImageView) findViewById(R.id.pause_icon);
        close.setOnClickListener(this);
        open.setOnClickListener(this);
        mute.setOnClickListener(this);
        speaker.setOnClickListener(this);
        pause.setOnClickListener(this);
        close.setOnTouchListener(onTouchListener);
        open.setOnTouchListener(onTouchListener);
        mute.setOnTouchListener(onTouchListener);
        speaker.setOnTouchListener(onTouchListener);
        pause.setOnTouchListener(onTouchListener);
    }

    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            if(e.getAction() == MotionEvent.ACTION_DOWN){
                downX = (int) e.getRawX();
                downY = (int) e.getRawY();
            }else if(e.getAction() == MotionEvent.ACTION_UP){
                upX = (int) e.getRawX();
                upY = (int) e.getRawY();
                if(Math.abs(upX - downX) > 10 || Math.abs(upY - downY) > 10){
                    gestureDetector.onTouchEvent(e);
                    return true;
                }
            }
            return gestureDetector.onTouchEvent(e);
        }
    };

    @Override
    public void onClick(View v) {
        if(dragMuneClickListenerList.size() == 0){
            return;
        }
        DragMenuType dragMenuType = null;
        switch (v.getId()){
            case R.id.close:
                dragMenuType = DragMenuType.CLOSE;
                break;
            case R.id.open:
                dragMenuType = DragMenuType.OPEN;
                break;
            case R.id.mute:
                dragMenuType = DragMenuType.MUTE;
                break;
            case R.id.speaker:
                dragMenuType = DragMenuType.SPEAKER;
                break;
            case R.id.pause:
                dragMenuType = DragMenuType.PAUSE;
                break;
        }
        for(DragMuneClickListener item : dragMuneClickListenerList){
            item.onClickMethod(dragMenuType);
        }
    }

    public void exit(){
        muteIcon.setBackgroundResource(R.drawable.skype_bar_nonmute);
        speakerIcon.setBackgroundResource(R.drawable.skype_bar_nonspeaker);
        pauseIcon.setBackgroundResource(R.drawable.skype_bar_pause);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;//事件已经消费，避免事件传递到其他的控件,因为这里消费了事件，所以不会触发onClickListener方法，所以使用GestureDetector和DragViewClickLister来控制
    }

    GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx =(int)e2.getRawX() - lastX;
            int dy =(int)e2.getRawY() - lastY;
            int left = getLeft() + dx;
            int top = getTop() + dy;
            int right = getRight() + dx;
            int bottom = getBottom() + dy;
            if(left < 0){
                left = 0;
                right = left + getWidth();
            }
            if(right > screenWidth){
                right = screenWidth;
                left = right - getWidth();
            }
            if(top < 0){
                top = 0;
                bottom = top + getHeight();
            }
            if(bottom > screenHeight){
                bottom = screenHeight;
                top = screenHeight - getHeight();
            }
            layout(left,top,right,bottom);
            lastX = (int) e2.getRawX();
            lastY = (int) e2.getRawY();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            lastX = (int) e.getRawX();
            lastY = (int) e.getRawY();
            return super.onDown(e);
        }
    };

    @Override
    public void onAudioViewOperationResult(AudioView.AudioViewOperationResultType resultType, int resourceId) {
        if(resourceId == 0){
            return;
        }
        switch (resultType){
            case MUTE:
                muteIcon.setBackgroundResource(resourceId);
                break;
            case SPEAKER:
                speakerIcon.setBackgroundResource(resourceId);
                break;
            case PAUSE:
                pauseIcon.setBackgroundResource(resourceId);
                break;
        }
    }

    public enum DragMenuType{
        CLOSE,OPEN,MUTE,SPEAKER,PAUSE
    }

    public void setDragMuneClickListener(DragMuneClickListener dragMuneClickListener) {
        dragMuneClickListenerList.add(dragMuneClickListener);
    }
}
