package com.gavinguo.skypevideo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gavinguo.skypevideo.explosionfield.ExplosionField2;
import com.gavinguo.skypevideo.permissoin.OnPermissionCallback;
import com.gavinguo.skypevideo.permissoin.PermissionAlterDialogFactory;
import com.gavinguo.skypevideo.permissoin.PermissionHelper;
import com.microsoft.office.lync.utility.SkypeDomain;
import com.microsoft.office.sfb.appsdk.AnonymousSession;
import com.microsoft.office.sfb.appsdk.Application;
import com.microsoft.office.sfb.appsdk.AudioService;
import com.microsoft.office.sfb.appsdk.ConfigurationManager;
import com.microsoft.office.sfb.appsdk.Conversation;
import com.microsoft.office.sfb.appsdk.DevicesManager;
import com.microsoft.office.sfb.appsdk.SFBException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText displayName,message,meetingUrl;
    private Button video,send;
    private TextView messageShow;

    private ArrayList<String> messageList = new ArrayList<>();

    private PermissionHelper permissionHelper;

    Application application2 = null;
    ConfigurationManager configurationManager2 = null;
    AnonymousSession mAnonymousSession2 = null;
    DevicesManager devicesManager2 = null;
    ConversationPropertyChangeListener2 conversationPropertyChangeListener2 = null;
    Conversation anonymousConversation2 = null;
    Timer timer = null;

    private DragView skypeAudioMeun;
    private AudioView skypeAudio;
    private int contentWidth;
    private int contentHeight;
    private int actionbarHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initSkypeView();
    }

    private void initView() {
        displayName = (EditText) findViewById(R.id.displayName);
        message = (EditText) findViewById(R.id.messageInput);
        meetingUrl = (EditText) findViewById(R.id.meetingUrl);

        video = (Button) findViewById(R.id.video);
        send = (Button) findViewById(R.id.send);

        messageShow = (TextView) findViewById(R.id.showMessage);

        video.setOnClickListener(this);
        send.setOnClickListener(this);
    }

    private void initSkypeView() {
        skypeAudioMeun = (DragView) findViewById(R.id.skypeAudioMeun);
        skypeAudio = (AudioView) findViewById(R.id.skypeAudio);

        skypeAudioMeun.setDragMuneClickListener(skypeAudio);
        skypeAudioMeun.setDragMuneClickListener(dragMuneClickListener);

        skypeAudio.setAudioViewMenuOperationResultListener(skypeAudioMeun);
        skypeAudio.setAudioViewOperationListener(audioViewOperationListener);
    }

    DragMuneClickListener dragMuneClickListener = new DragMuneClickListener() {

        @Override
        public void onClickMethod(DragView.DragMenuType mutype) {
            switch (mutype) {
                case CLOSE:
                    try {
                        if (anonymousConversation2 != null && anonymousConversation2.canLeave()) {
                            AudioView.isGo = false;
                            skypeAudio.exit();
                            skypeAudioMeun.exit();
                            ExplosionField2 mExplosionField = ExplosionField2.attach2Window(MainActivity.this);
                            mExplosionField.explode(skypeAudioMeun);
//                        skypeAudioMeun.setVisibility(View.GONE);
                            skypeAudio.setVisibility(View.GONE);
                            anonymousConversation2.leave();
                            configurationManager2 = null;
                            application2 = null;
                        }
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                    break;
                case OPEN:
                    float dragViewCenterPointX = skypeAudioMeun.getLeft() + skypeAudioMeun.getWidth() / 2;
                    float dragViewCenterPointY = skypeAudioMeun.getTop() + skypeAudioMeun.getHeight() / 2;
                    final float x = dragViewCenterPointX / contentWidth;
                    final float y = dragViewCenterPointY / contentHeight;
                    Animation scaleAnimation3 = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    scaleAnimation3.setDuration(1000);
                    scaleAnimation3.setInterpolator(new AccelerateDecelerateInterpolator());
                    scaleAnimation3.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            skypeAudioMeun.setVisibility(View.GONE);
                            skypeAudio.setVisibility(View.VISIBLE);
                            Animation scaleAnimation4 = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, x, Animation.RELATIVE_TO_SELF, y);
                            scaleAnimation4.setDuration(1000);
                            scaleAnimation4.setInterpolator(new AccelerateDecelerateInterpolator());
                            skypeAudio.startAnimation(scaleAnimation4);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    skypeAudioMeun.startAnimation(scaleAnimation3);
                    break;
                case MUTE:
                    break;
                case SPEAKER:
                    break;
                case PAUSE:
                    break;
            }
        }
    };

    AudioView.AudioViewOperationListener audioViewOperationListener = new AudioView.AudioViewOperationListener() {
        @Override
        public void onAudioViewOperationListener(AudioView.AudioViewOperationType operationType) {
            switch (operationType) {
                case CLOSE:
                    try {
                        if (anonymousConversation2 != null && anonymousConversation2.canLeave()) {
                            AudioView.isGo = false;
                            skypeAudio.exit();
                            skypeAudioMeun.exit();
                            skypeAudioMeun.setVisibility(View.GONE);
                            skypeAudio.setVisibility(View.GONE);
                            anonymousConversation2.leave();
                            configurationManager2 = null;
                            application2 = null;
                        }
                    } catch (SFBException e) {
                        e.printStackTrace();
                    }
                    break;
                case NARROW:
                    contentHeight = skypeAudio.getHeight();
                    contentWidth = skypeAudio.getWidth();
                    float y = (float) actionbarHeight / (float) contentHeight;
                    Log.e("", "contentHeight:" + contentHeight + ";contentWidth:" + contentWidth + ";actionBar.getHeight():" + actionbarHeight + ";y:" + y);
                    Animation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, y);
                    scaleAnimation.setDuration(1000);
                    scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            skypeAudio.setVisibility(View.GONE);
                            skypeAudioMeun.setVisibility(View.VISIBLE);
                            Animation scaleAnimation2 = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.5f);
                            scaleAnimation2.setDuration(1000);
                            scaleAnimation2.setInterpolator(new AccelerateDecelerateInterpolator());
                            skypeAudioMeun.startAnimation(scaleAnimation2);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
//                    skypeAudio.startAnimation(scaleAnimation);
                    skypeAudio.setVisibility(View.GONE);
                    skypeAudioMeun.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onServiceDisconnected() {
            AudioView.isGo = false;
            try {
                skypeAudio.exit();
                skypeAudioMeun.exit();
                skypeAudioMeun.setVisibility(View.GONE);
                skypeAudio.setVisibility(View.GONE);
                if (anonymousConversation2 != null) {
                    anonymousConversation2.leave();
                    configurationManager2 = null;
                    application2 = null;
                }
            } catch (SFBException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video:
                videoClickMethod();
                break;
            case R.id.send:
                sendClickMethod();
                break;
        }
    }

    public void videoClickMethod() {
        String displayNameStr = displayName.getText().toString();
        if(TextUtils.isEmpty(displayNameStr)) {
            Toast.makeText(MainActivity.this, "please input displayname", Toast.LENGTH_SHORT).show();
            return;
        }
        String meetingUrlStr = meetingUrl.getText().toString();
        if(TextUtils.isEmpty(meetingUrlStr)) {
            Toast.makeText(MainActivity.this, "please input meetingUrl", Toast.LENGTH_SHORT).show();
            return;
        }
        checkRecordAudioPermission(Manifest.permission.READ_PHONE_STATE, meetingUrlStr);
    }

    public void sendClickMethod() {
        String displayNameStr = displayName.getText().toString();
        if(TextUtils.isEmpty(displayNameStr)) {
            Toast.makeText(MainActivity.this, "please input displayname", Toast.LENGTH_SHORT).show();
            return;
        }
        String messageStr = message.getText().toString();
        if(TextUtils.isEmpty(messageStr)) {
            Toast.makeText(MainActivity.this, "please input message", Toast.LENGTH_SHORT).show();
            return;
        }
        messageShow.setText("");
        messageList.add(displayNameStr + "：" + messageStr + "\n");
        for(String messageItem : messageList) {
            messageShow.append(messageItem);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkRecordAudioPermission(String permissionNamePrama, final String meetingUrlParams) {
        permissionHelper = PermissionHelper.getInstance(this, new OnPermissionCallback() {
            @Override
            public void onPermissionGranted(@NonNull String[] permissionName) {
                String getPermissionName = permissionName[0];
                if (getPermissionName.equals(Manifest.permission.READ_PHONE_STATE)) {
                    checkRecordAudioPermission(Manifest.permission.RECORD_AUDIO, meetingUrlParams);
                    return;
                }
                if (getPermissionName.equals(Manifest.permission.RECORD_AUDIO)) {
                    checkRecordAudioPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, meetingUrlParams);
                    return;
                }
                if (getPermissionName.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkRecordAudioPermission(Manifest.permission.CAMERA, meetingUrlParams);
                    return;
                }
                joinAudioSkype();
            }

            @Override
            public void onPermissionDeclined(@NonNull String[] permissionName) {
                String getPermissionName = permissionName[0];
                Toast.makeText(MainActivity.this, "we need permission", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionPreGranted(@NonNull String permissionsName) {
                if (permissionsName.equals(Manifest.permission.READ_PHONE_STATE)) {
                    checkRecordAudioPermission(Manifest.permission.RECORD_AUDIO, meetingUrlParams);
                    return;
                }
                if (permissionsName.equals(Manifest.permission.RECORD_AUDIO)) {
                    checkRecordAudioPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, meetingUrlParams);
                    return;
                }
                if (permissionsName.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkRecordAudioPermission(Manifest.permission.CAMERA, meetingUrlParams);
                    return;
                }
                joinAudioSkype();
            }

            @Override
            public void onPermissionNeedExplanation(@NonNull final String permissionName) {
                AlertDialog alertDialog = PermissionAlterDialogFactory.getAlertDialog(
                        MainActivity.this,
                        "Permission",
                        "allow",
                        "we need permission",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                permissionHelper.requestAfterExplanation(permissionName);
                            }
                        }
                );
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }

            @Override
            public void onPermissionReallyDeclined(@NonNull String permissionName) {
                AlertDialog alertDialog = PermissionAlterDialogFactory.getAlertDialog(
                        MainActivity.this,
                        "Permission",
                        "allow",
                        "we need permission",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                permissionHelper.openSettingsScreen();
                            }
                        }
                );
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }

            @Override
            public void onNoPermissionNeeded(@NonNull Object permissionName) {
                joinAudioSkype();
            }
        });
        permissionHelper
                .setForceAccepting(false) // default is false. its here so you know that it exists.
                .request(permissionNamePrama);
    }

    private void joinAudioSkype() {
        try {
            Toast.makeText(MainActivity.this, "waiting...", Toast.LENGTH_SHORT).show();
            String displayNameStr = displayName.getText().toString();
            String meetingUrlStr = meetingUrl.getText().toString();
            this.application2 = Application.getInstance(this.getBaseContext());
            this.devicesManager2 = application2.getDevicesManager();
            this.configurationManager2 = application2.getConfigurationManager();

            // Note that the sample enable video over cellular network. This is not the default.
            this.configurationManager2.enablePreviewFeatures(true);
            this.configurationManager2.setRequireWiFiForVideo(false);
            this.configurationManager2.setRequireWiFiForAudio(false);
            this.configurationManager2.setMaxVideoChannelCount(5);
            this.configurationManager2.setEndUserAcceptedVideoLicense();

            this.mAnonymousSession2 = this.application2.joinMeetingAnonymously(
                    displayNameStr, URI.create(meetingUrlStr));
            this.anonymousConversation2 = this.mAnonymousSession2.getConversation();

            this.conversationPropertyChangeListener2 = new ConversationPropertyChangeListener2();
            this.anonymousConversation2.addOnPropertyChangedCallback(this.conversationPropertyChangeListener2);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(MainActivity.this, "join failed", Toast.LENGTH_SHORT).show();
                                timer = null;
                                AudioView.isGo = false;
                                if (MainActivity.this.anonymousConversation2 != null) {
                                    if (MainActivity.this.anonymousConversation2.canLeave()) {
                                        MainActivity.this.anonymousConversation2.leave();
                                    }
                                    MainActivity.this.anonymousConversation2.removeOnPropertyChangedCallback(MainActivity.this.conversationPropertyChangeListener2);
                                    MainActivity.this.anonymousConversation2 = null;
                                }
                            } catch (SFBException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, 30 * 1000);
        } catch (SFBException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "join failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback implementation for listening for conversation property changes.
     */
    class ConversationPropertyChangeListener2 extends com.microsoft.office.sfb.appsdk.Observable.OnPropertyChangedCallback {
        /**
         * onProperty changed will be called by the Observable instance on a property change.
         *
         * @param sender     Observable instance.
         * @param propertyId property that has changed.
         */
        @Override
        public void onPropertyChanged(com.microsoft.office.sfb.appsdk.Observable sender, int propertyId) {
            if (propertyId == Conversation.STATE_PROPERTY_ID) {
                updateConversationState2();
            }
        }
    }

    public void updateConversationState2() {
        boolean meetingJoined = false;
        Conversation.State state = this.anonymousConversation2.getState();
        switch (state) {
            case ESTABLISHED:
                meetingJoined = true;
                break;
            case IDLE:
                /*meetingJoined = false;
                if (this.anonymousConversation != null) {
                    this.anonymousConversation.removeOnPropertyChangedCallback(this.conversationPropertyChangeListener);
                    this.anonymousConversation = null;
                }*/
                break;
            default:
        }
        if (meetingJoined) {
            if (!AudioView.isGo) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                String displayNameStr = displayName.getText().toString();
                String meetingUrlStr = meetingUrl.getText().toString();
                AudioView.isGo = true;
                skypeAudio.setConversationAndDevice(this.anonymousConversation2, devicesManager2, displayNameStr, meetingUrlStr);
                skypeAudio.setTitle("Meeting");
                skypeAudio.setVisibility(View.VISIBLE);
//                startActivityForResult(new Intent(this, AudioActivity.class),AUDIO_ACTIVITY_CODE);
            }
        }
    }
}
