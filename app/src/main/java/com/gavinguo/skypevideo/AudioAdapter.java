package com.gavinguo.skypevideo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.microsoft.media.MMVRSurfaceView;
import com.microsoft.office.sfb.appsdk.Camera;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.Observable;
import com.microsoft.office.sfb.appsdk.Participant;
import com.microsoft.office.sfb.appsdk.ParticipantAudio;
import com.microsoft.office.sfb.appsdk.ParticipantVideo;
import com.microsoft.office.sfb.appsdk.SFBException;
import com.microsoft.office.sfb.appsdk.VideoService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavinguo on 6/1/2016.
 */
public class AudioAdapter extends BaseAdapter {

    private Context context;
    private String meetingUrl;

    private List<BOParticipant> users;

    private boolean isLoadingAllUser = false;
    private int loadingAllUserRetryCount = 0;

    private Conversation conversation;
    private DevicesManager devicesManager = null;
    private ArrayList<Camera> cameras;
    private Camera frontCamera = null;
    private Camera backCamera = null;
    private VideoService videoService;

    private AudioView audioView = null;


    private boolean isNewSurfaceView = false;

    public AudioAdapter(Context context, String meetingUrl, AudioView audioView) {
        this.context = context;
        this.audioView = audioView;
        this.meetingUrl = meetingUrl;
    }

    @Override
    public int getCount() {
        if(users == null){
            return 0;
        }else{
            return users.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if(position > 5) {
            return 2;
        } else {
            BOParticipant boparticipant = users.get(position);
            if(boparticipant.isMySelf) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final int currentItemType = getItemViewType(position);
        final BOParticipant boparticipant = users.get(position);
        String microsoftName;
        final Participant participant = boparticipant.participant;
        if(currentItemType == 0) {
            if(convertView == null){
                convertView = View.inflate(context, R.layout.audio_user_grid_item2,null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
                viewHolder.muteState = (ImageView) convertView.findViewById(R.id.mute_state_icon);
                viewHolder.holdState = (ImageView) convertView.findViewById(R.id.hold_state_icon);
                viewHolder.participantVideoLayout = (RelativeLayout)convertView.findViewById(R.id.participantVideoLayoutId);
                viewHolder.videoPreviewTextureView = (TextureView)convertView.findViewById(R.id.selfParticipantVideoView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
        } else if(currentItemType == 1) {
            if(convertView == null){
                convertView = View.inflate(context, R.layout.audio_user_grid_item,null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
                viewHolder.muteState = (ImageView) convertView.findViewById(R.id.mute_state_icon);
                viewHolder.holdState = (ImageView) convertView.findViewById(R.id.hold_state_icon);
                viewHolder.participantVideoLayout = (RelativeLayout)convertView.findViewById(R.id.participantVideoLayoutId);
                viewHolder.mmvrSurface = (MMVRSurfaceView)convertView.findViewById(R.id.mmvrSurfaceViewId);
                convertView.setTag(viewHolder);
            } else{
                if(isNewSurfaceView) {
                    convertView = View.inflate(context, R.layout.audio_user_grid_item,null);
                    viewHolder = new ViewHolder();
                    viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
                    viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
                    viewHolder.muteState = (ImageView) convertView.findViewById(R.id.mute_state_icon);
                    viewHolder.holdState = (ImageView) convertView.findViewById(R.id.hold_state_icon);
                    viewHolder.participantVideoLayout = (RelativeLayout)convertView.findViewById(R.id.participantVideoLayoutId);
                    viewHolder.mmvrSurface = (MMVRSurfaceView)convertView.findViewById(R.id.mmvrSurfaceViewId);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder)convertView.getTag();
                }
            }
        } else {
            if(convertView == null){
                convertView = View.inflate(context, R.layout.audio_user_grid_item3,null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.bigImageView = (ImageView) convertView.findViewById(R.id.avatar_big);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
                viewHolder.muteState = (ImageView) convertView.findViewById(R.id.mute_state_icon);
                viewHolder.holdState = (ImageView) convertView.findViewById(R.id.hold_state_icon);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
        }
        final ParticipantAudio participantAudio = participant.getParticipantAudio();
        if(boparticipant.isMySelf){
            microsoftName = boparticipant.displayName;
            viewHolder.nameView.setText(boparticipant.displayName);
        }else{
            microsoftName = participant.getPerson().getDisplayName();
            viewHolder.nameView.setText(participant.getPerson().getDisplayName());
        }
        if(currentItemType == 2) {
            viewHolder.bigImageView.setImageResource(R.drawable.ic_launcher);
        }

        if(participantAudio.isMuted()){
            viewHolder.muteState.setVisibility(View.VISIBLE);
        }else{
            viewHolder.muteState.setVisibility(View.INVISIBLE);
        }
        if(participantAudio.isOnHold()){
            viewHolder.holdState.setVisibility(View.VISIBLE);
        }else{
            viewHolder.holdState.setVisibility(View.INVISIBLE);
        }
        participant.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback(){

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
//                notifyDataSetChanged();
                if(participantAudio.isMuted()){
                    viewHolder.muteState.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.muteState.setVisibility(View.INVISIBLE);
                }
                if(participantAudio.isOnHold()){
                    viewHolder.holdState.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.holdState.setVisibility(View.INVISIBLE);
                }
            }
        });
        participantAudio.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
//                notifyDataSetChanged();
                if(participantAudio.isMuted()){
                    viewHolder.muteState.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.muteState.setVisibility(View.INVISIBLE);
                }
                if(participantAudio.isOnHold()){
                    viewHolder.holdState.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.holdState.setVisibility(View.INVISIBLE);
                }
            }
        });
        if(currentItemType == 0) {
            viewHolder.videoPreviewTextureView.setVisibility(View.VISIBLE);
            videoService.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable observable, int i) {
                    //do nothing
                    try {
                        // Check state of video service.
                        // If not started, start it.
                        if (videoService.canStart()) {
                            videoService.start();
                        } else {
                            // On joining the meeting the Video service is started by default.
                            // Since the view is created later the video service is paused.
                            // Resume the service.
                            if (videoService.getPaused() && videoService.canSetPaused()) {
                                videoService.setPaused(false);
                            }
                        }
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                }
            });
            viewHolder.videoPreviewTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    try {
                        // Display the preview
                        videoService.showPreview(surface);

                        // Check state of video service.
                        // If not started, start it.
                        if (videoService.canStart()) {
                            videoService.start();
                        } else {
                            // On joining the meeting the Video service is started by default.
                            // Since the view is created later the video service is paused.
                            // Resume the service.
                            if (videoService.getPaused() && videoService.canSetPaused()) {
                                videoService.setPaused(false);
                            }
                        }
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
            });
        } else if(currentItemType == 1) {
            viewHolder.mmvrSurface.setCallback(new MMVRSurfaceView.MMVRCallback() {
                @Override
                public void onSurfaceCreated(MMVRSurfaceView mmvrSurfaceView) {
                    viewHolder.mmvrSurfaceView = mmvrSurfaceView;
                    viewHolder.mmvrSurfaceView.setAutoFitMode(MMVRSurfaceView.MMVRAutoFitMode_Crop);
                    viewHolder.mmvrSurfaceView.requestRender();
                    try {
                        ParticipantVideo participantVideo = participant.getParticipantVideo();
                        participantVideo.subscribe(viewHolder.mmvrSurfaceView);
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFrameRendered(MMVRSurfaceView mmvrSurfaceView) {

                }

                @Override
                public void onRenderSizeChanged(MMVRSurfaceView mmvrSurfaceView, int i, int i1) {

                }
            });
        }
        if(position >= getCount() - 1) {
            isNewSurfaceView = false;
        }
        return convertView;
    }

    class ViewHolder{
        ImageView imageView;
        ImageView bigImageView;
        TextView nameView;
        ImageView muteState;
        ImageView holdState;
        MMVRSurfaceView mmvrSurfaceView;
        MMVRSurfaceView mmvrSurface;
        RelativeLayout participantVideoLayout;
        TextureView videoPreviewTextureView;
    }

    public void setData(List<BOParticipant> userNames){
        if(userNames == null){
            return;
        }
        this.users = userNames;
        notifyDataSetChanged();
    }

    public void clear(){
        if(this.users != null){
            this.users.clear();
            notifyDataSetChanged();
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation, VideoService videoService) {
        this.conversation = conversation;
        this.videoService = videoService;
    }

    public DevicesManager getDevicesManager() {
        return devicesManager;
    }

    public void setDevicesManager(DevicesManager devicesManager) {
        this.devicesManager = devicesManager;
        cameras = (ArrayList<Camera>) devicesManager.getCameras();
        for(Camera camera: cameras) {
            if (camera.getType() == Camera.Type.FRONTFACING){
                frontCamera = camera;
            }
            if (camera.getType() == Camera.Type.BACKFACING){
                backCamera = camera;
            }
        }
    }

    public boolean isNewSurfaceView() {
        return isNewSurfaceView;
    }

    public void setNewSurfaceView(boolean newSurfaceView) {
        isNewSurfaceView = newSurfaceView;
    }
}
