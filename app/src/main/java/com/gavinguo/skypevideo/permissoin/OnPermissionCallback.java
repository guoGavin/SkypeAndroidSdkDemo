package com.gavinguo.skypevideo.permissoin;

import android.support.annotation.NonNull;

public interface OnPermissionCallback {

    //user choose allow permission
    void onPermissionGranted(@NonNull String[] permissionName);

    //user choose reject permission,but not choose prompt again
    void onPermissionDeclined(@NonNull String[] permissionName);

    //already allow permission
    void onPermissionPreGranted(@NonNull String permissionsName);

    //first choose reject,second request and then,if user not choose not prompt again
    void onPermissionNeedExplanation(@NonNull String permissionName);

    //user choose reject permission,and choose not prompt again
    void onPermissionReallyDeclined(@NonNull String permissionName);

    //sdk < M,no do something
    void onNoPermissionNeeded(@NonNull Object permissionName);
}
