package com.gavinguo.skypevideo;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.office.sfb.appsdk.AudioService;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.ConversationActivityItem;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.ObservableList;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.ParticipantAudio;
import com.microsoft.office.sfb.appsdk.ParticipantService;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.Speaker;
import com.microsoft.office.sfb.appsdk.VideoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavinguo on 6/3/2016.
 */
public class AudioView extends RelativeLayout implements View.OnClickListener,DragMuneClickListener {

    private Context context;

    private GridView users;
    private AudioAdapter audioAdapter;

    public static boolean isGo = false;
    private Conversation conversation = null;
    private DevicesManager devicesManager = null;

    private AudioService audioService = null;
    private VideoService videoService = null;
    private boolean isPause = false;

    private ObservableList<ConversationActivityItem> activityListView = null;
    private UserJoinOrLeftListener userJoinOrLeftListener = null;

    private ObservableList<Participant> remoteUsers;

    private String myName;
    private String meetingUrl;

    private ConversationCallbackHandler listener;

    private RelativeLayout actionBar;
    private ImageView mute,speaker,pause,away;
    private Chronometer time;
    private TextView loading;

    Speaker.Endpoint endpoint = null;

    private RelativeLayout cancel;

    private AudioViewOperationListener audioViewOperationListener;
    private AudioViewMenuOperationResultListener audioViewMenuOperationResultListener;

    private boolean isFirst = false;

    private String title = "";
    private TextView titleTextView = null;

    public AudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        isFirst = true;
    }

    public void setConversationAndDevice(Conversation conv, DevicesManager dManager, String myNameParam, String meetingUrl) {
        isFirst = true;
        conversation = conv;
        devicesManager = dManager;
        myName = myNameParam;
        this.meetingUrl = meetingUrl;
        listener = new ConversationCallbackHandler();
        if(devicesManager != null){
            this.endpoint = devicesManager.getSelectedSpeaker().getActiveEndpoint();
        }
        if(conversation != null){
            audioService = conversation.getAudioService();
            videoService = conversation.getVideoService();
            audioService.addOnPropertyChangedCallback(listener);
            conversation.getSelfParticipant().getParticipantAudio().addOnPropertyChangedCallback(listener);
        }
    }

    private void init(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.audio_view, this);
        initTitleBar();
        initView();
    }

    private void initTitleBar(){
        cancel = (RelativeLayout) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    private void initView(){
        users = (GridView) findViewById(R.id.participants);
        actionBar = (RelativeLayout) findViewById(R.id.action_bar);
        mute = (ImageView) findViewById(R.id.mute_skype);
        mute.setOnClickListener(this);
        speaker = (ImageView) findViewById(R.id.speaker_skype);
        speaker.setOnClickListener(this);
        pause = (ImageView) findViewById(R.id.pause_skype);
        pause.setOnClickListener(this);
        away = (ImageView) findViewById(R.id.away_skype);
        away.setOnClickListener(this);
        time = (Chronometer) findViewById(R.id.time);
        time.setOnChronometerTickListener(timeListener);
        time.stop();
        time.setVisibility(INVISIBLE);
        titleTextView = (TextView) findViewById(R.id.title);
        if(!TextUtils.isEmpty(title)) {
            titleTextView.setText(title);
        }
        loading = (TextView) findViewById(R.id.loading);
    }

    public void refreshData() {
        if(audioAdapter != null) {
            audioAdapter.setNewSurfaceView(true);
            audioAdapter.notifyDataSetChanged();
        }
    }

    Chronometer.OnChronometerTickListener timeListener = new Chronometer.OnChronometerTickListener(){
        public void onChronometerTick(Chronometer cArg) {
            long t = SystemClock.elapsedRealtime() - cArg.getBase() - 8 * 60 * 60 * 1000;
            cArg.setText(DateFormat.format("kk:mm:ss", t));
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mute_skype:
                muteMethod();
                break;
            case R.id.speaker_skype:
                speakerMethod();
                break;
            case R.id.pause_skype:
                pauMethod();
                break;
            case R.id.cancel:
//                showChooseDialog();
                if(audioViewOperationListener != null){
                    audioViewOperationListener.onAudioViewOperationListener(AudioViewOperationType.NARROW);
                }
                break;
            case R.id.away_skype:
                audioAdapter.clear();
                conversation = null;
                devicesManager = null;
                myName = null;
                if(audioViewOperationListener != null){
                    audioViewOperationListener.onAudioViewOperationListener(AudioViewOperationType.CLOSE);
                }
                break;
        }
    }

    @Override
    public void onClickMethod(DragView.DragMenuType mutype) {
        switch (mutype){
            case CLOSE:
                exit();
                break;
            case OPEN:
                break;
            case MUTE:
                muteMethod();
                break;
            case SPEAKER:
                speakerMethod();
                break;
            case PAUSE:
                pauMethod();
                break;
        }
    }

    public void exit(){
        isFirst = true;
        mute.setBackgroundResource(R.drawable.skype_nonmute);
        speaker.setBackgroundResource(R.drawable.skype_nonspeaker);
        pause.setBackgroundResource(R.drawable.skype_pause);
        time.setBase(SystemClock.elapsedRealtime());
        time.stop();
        time.setVisibility(INVISIBLE);
        loading.setVisibility(VISIBLE);
        actionBar.setVisibility(GONE);
        if(audioAdapter != null){
            audioAdapter.clear();
        }
        if(remoteUsers != null){
            remoteUsers.removeOnListChangedCallback(userJoinOrLeftListener);
        }
        if(conversation != null && listener != null){
            audioService.removeOnPropertyChangedCallback(listener);
            conversation.getSelfParticipant().getParticipantAudio().removeOnPropertyChangedCallback(listener);
        }
        endpoint = null;
        conversation = null;
        devicesManager = null;
        myName = null;
    }

    private void muteMethod(){
        if (audioService.canToggleMute()) {
            try {
                audioService.toggleMute();
                    /*mute.setBackgroundResource(R.drawable.skype_mute);
                    if(audioViewMenuOperationResultListener != null){
                        audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.MUTE,R.drawable.skype_bar_mute);
                    }
                    mute.setBackgroundResource(R.drawable.skype_nonmute);
                    if(audioViewMenuOperationResultListener != null){
                        audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.MUTE,R.drawable.skype_bar_nonmute);
                    }*/
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(context, "can't operation", Toast.LENGTH_SHORT).show();
        }
    }

    private void speakerMethod(){
        switch(this.endpoint) {
            case LOUDSPEAKER:
                devicesManager.getSelectedSpeaker().setActiveEndpoint(Speaker.Endpoint.NONLOUDSPEAKER);
                //非免提
                speaker.setBackgroundResource(R.drawable.skype_nonspeaker);
                if(audioViewMenuOperationResultListener != null){
                    audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.SPEAKER,R.drawable.skype_bar_nonspeaker);
                }
                break;
            case NONLOUDSPEAKER:
                devicesManager.getSelectedSpeaker().setActiveEndpoint(Speaker.Endpoint.LOUDSPEAKER);
                //免提
                speaker.setBackgroundResource(R.drawable.speaker);
                if(audioViewMenuOperationResultListener != null){
                    audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.SPEAKER,R.drawable.skype_bar_speaker);
                }
                break;
            default:
        }
        this.endpoint = this.devicesManager.getSelectedSpeaker().getActiveEndpoint();
    }

    public void pauMethod(){
        try {
            boolean isOnHold = audioService.isOnHold();
            if (audioService.canSetHold()) {
                audioService.setHold(!isOnHold);
                if(!isOnHold){
                    //暂停
                    pause.setBackgroundResource(R.drawable.skype_nopause);
                    if(audioViewMenuOperationResultListener != null){
                        audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.PAUSE,R.drawable.skype_bar_nonpause);
                    }
                }else{
                    //非暂停
                    pause.setBackgroundResource(R.drawable.skype_pause);
                    if(audioViewMenuOperationResultListener != null){
                        audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.PAUSE,R.drawable.skype_bar_pause);
                    }
                }
            }else{
                Toast.makeText(context, "can't operation", Toast.LENGTH_SHORT).show();
            }
        } catch (SFBException e) {
            e.printStackTrace();
        }
    }

    public interface AudioViewOperationListener{
        void onAudioViewOperationListener(AudioViewOperationType operationType);

        void onServiceDisconnected();
    }

    public enum AudioViewOperationType{
        CLOSE,NARROW
    }

    public enum AudioViewOperationResultType{
        MUTE,SPEAKER,PAUSE
    }

    Handler serviceState = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            ParticipantService.State state = (ParticipantService.State)msg.obj;
            switch (state){
                case DISCONNECTED:
//                    Toast.makeText(context,"连接中断",Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTING:
                    Toast.makeText(context,"conecting", Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTED:
                    Toast.makeText(context,"conected",Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    class ConversationCallbackHandler extends Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         * @see Observable.OnPropertyChangedCallback
         */
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            try {
                if(conversation == null) {
                    return;
                }
                if (ParticipantAudio.class.isInstance(sender)) {
                    ParticipantAudio selfParticipantAudio = (ParticipantAudio)sender;
                    switch (propertyId) {
                        case ParticipantService.PARTICIPANT_SERVICE_STATE_PROPERTY_ID:
                            ParticipantService.State state = selfParticipantAudio.getState();
                            if(state == null){
                                return;
                            }
                            Message message = new Message();
                            message.obj = state;
                            serviceState.sendMessage(message);
                            break;
                        case ParticipantAudio.PARTICIPANT_IS_MUTED_PROPERTY_ID:
                            if(isFirst){
                                time.setBase(SystemClock.elapsedRealtime());
                                time.start();
                                time.setVisibility(VISIBLE);
                                isFirst = false;
                                actionBar.setVisibility(View.VISIBLE);
                                loading.setVisibility(GONE);
                                if(selfParticipantAudio.isMuted()){
//                                    selfParticipantAudio.setMuted(false);
                                    remoteUsers = conversation.getRemoteParticipants();
                                    List<BOParticipant> usersParticipant = new ArrayList<>();
                                    BOParticipant boParticipant = new BOParticipant();
                                    boParticipant.displayName = myName;
                                    boParticipant.isMySelf = true;
                                    boParticipant.participant = conversation.getSelfParticipant();
                                    usersParticipant.add(boParticipant);
                                    if(remoteUsers != null){
                                        for(Participant item : remoteUsers){
                                            BOParticipant boParticipantItem = new BOParticipant();
                                            boParticipantItem.isMySelf = false;
                                            boParticipantItem.participant = item;
                                            usersParticipant.add(boParticipantItem);
                                        }
                                        userJoinOrLeftListener = new UserJoinOrLeftListener();
                                        remoteUsers.addOnListChangedCallback(userJoinOrLeftListener);
                                    }
                                    audioAdapter = new AudioAdapter(context,meetingUrl,AudioView.this);
                                    audioAdapter.setData(usersParticipant);
                                    audioAdapter.setConversation(conversation,videoService);
                                    audioAdapter.setDevicesManager(devicesManager);
                                    users.setAdapter(audioAdapter);
                                    if(selfParticipantAudio.isMuted()){
                                        mute.setBackgroundResource(R.drawable.skype_mute);
                                    }else{
                                        mute.setBackgroundResource(R.drawable.skype_nonmute);
                                    }
                                }
                            }else{
                                if(selfParticipantAudio.isMuted()){
                                    mute.setBackgroundResource(R.drawable.skype_mute);
                                }else{
                                    mute.setBackgroundResource(R.drawable.skype_nonmute);
                                }
                            }
                            break;
                        case ParticipantAudio.PARTICIPANT_IS_ON_HOLD_PROPERTY_ID:
                            break;
                        case ParticipantAudio.PARTICIPANT_IS_SPEAKING_PROPERTY_ID:
                            break;
                    }
                }else if(AudioService.class.isInstance(sender)){
                    AudioService audioService = (AudioService) sender;
                    switch (propertyId){
                        case AudioService.CAN_START_PROPERTY_ID:
                            audioService.start();
                            break;
                        case AudioService.CAN_STOP_PROPERTY_ID:
                            break;
                        case AudioService.CAN_SET_HOLD_PROPERTY_ID:
                            break;
                        case AudioService.HOLD_PROPERTY_ID:
                            break;
                        case AudioService.MUTE_STATE_PROPERTY_ID:
                            AudioService.MuteState muteState = audioService.getMuteState();
                            if(muteState == AudioService.MuteState.UNMUTED) {
                                mute.setBackgroundResource(R.drawable.skype_nonmute);
                                if(audioViewMenuOperationResultListener != null){
                                    audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.MUTE,R.drawable.skype_bar_nonmute);
                                }
                            } else {
                                mute.setBackgroundResource(R.drawable.skype_mute);
                                if(audioViewMenuOperationResultListener != null){
                                    audioViewMenuOperationResultListener.onAudioViewOperationResult(AudioViewOperationResultType.MUTE,R.drawable.skype_bar_mute);
                                }
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class UserJoinOrLeftListener<Observable> extends ObservableList.OnListChangedCallback<Observable>{

        @Override
        public void onChanged(Observable observable) {
            Log.e("onChanged", "observable:" + observable);
        }

        @Override
        public void onItemRangeChanged(Observable observable, int i, int i1) {
            Log.e("onItemRangeChanged","observable:"+observable+";i:"+i+";i1:"+i1);
        }

        @Override
        public void onItemRangeInserted(Observable observable, int i, int i1) {
//            Log.e("onItemRangeInserted","observable:"+observable+";i:"+i+";i1:"+i1);
//            Log.e("onItemRangeInserted","remoteUsers:"+remoteUsers);
//            Log.e("onItemRangeInserted","conversation.getRemoteParticipants():"+conversation.getRemoteParticipants());
            List<BOParticipant> usersParticipant = new ArrayList<>();
            BOParticipant boParticipant = new BOParticipant();
            boParticipant.displayName = myName;
            boParticipant.isMySelf = true;
            boParticipant.participant = conversation.getSelfParticipant();
            usersParticipant.add(boParticipant);
            if(remoteUsers != null){
                for(Participant item : remoteUsers){
                    BOParticipant boParticipantItem = new BOParticipant();
                    boParticipantItem.isMySelf = false;
                    boParticipantItem.participant = item;
                    usersParticipant.add(boParticipantItem);
                }
            }
            if(audioAdapter == null){
                audioAdapter = new AudioAdapter(context,meetingUrl,AudioView.this);
                audioAdapter.setData(usersParticipant);
                audioAdapter.setConversation(conversation,videoService);
                audioAdapter.setDevicesManager(devicesManager);
            }else{
                audioAdapter.setData(usersParticipant);
            }
        }

        @Override
        public void onItemRangeMoved(Observable observable, int i, int i1, int i2) {
            Log.e("onItemRangeMoved","observable:"+observable+";i:"+i+";i1:"+i1);
        }

        @Override
        public void onItemRangeRemoved(Observable observable, int i, int i1) {
//            Log.e("onItemRangeRemoved","observable:"+observable+";i:" + i + ";i1:" + i1);
//            Log.e("onItemRangeRemoved", "remoteUsers:" + remoteUsers);
//            Log.e("onItemRangeRemoved", "conversation.getRemoteParticipants():" + conversation.getRemoteParticipants());
            List<BOParticipant> usersParticipant = new ArrayList<>();
            BOParticipant boParticipant = new BOParticipant();
            boParticipant.displayName = myName;
            boParticipant.isMySelf = true;
            boParticipant.participant = conversation.getSelfParticipant();
            usersParticipant.add(boParticipant);
            if(remoteUsers != null){
                for(Participant item : remoteUsers){
                    BOParticipant boParticipantItem = new BOParticipant();
                    boParticipantItem.isMySelf = false;
                    boParticipantItem.participant = item;
                    usersParticipant.add(boParticipantItem);
                }
            }
            if(audioAdapter == null){
                audioAdapter = new AudioAdapter(context,meetingUrl,AudioView.this);
                audioAdapter.setData(usersParticipant);
                audioAdapter.setConversation(conversation,videoService);
                audioAdapter.setDevicesManager(devicesManager);
            }else{
                audioAdapter.setData(usersParticipant);
            }
        }
    }

    public AudioViewOperationListener getAudioViewOperationListener() {
        return audioViewOperationListener;
    }

    public void setAudioViewOperationListener(AudioViewOperationListener audioViewOperationListener) {
        this.audioViewOperationListener = audioViewOperationListener;
    }

    public AudioViewMenuOperationResultListener getAudioViewMenuOperationResultListener() {
        return audioViewMenuOperationResultListener;
    }

    public void setAudioViewMenuOperationResultListener(AudioViewMenuOperationResultListener audioViewMenuOperationResultListener) {
        this.audioViewMenuOperationResultListener = audioViewMenuOperationResultListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if(titleTextView != null) {
            titleTextView.setText(this.title);
        }
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }
}
