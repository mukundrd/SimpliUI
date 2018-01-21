package com.trayis.simpliui.permission;

/**
 * Created by mukundrd on 6/8/17.
 */

public interface PermissionCallback {
    int PERMISSION_ALREADY_AVAILABLE = 0;
    int PERMISSION_REQUESTED = 1;
    int SHOW_PERMISSION_RATIONALE = 2;
    int PERMISSION_GRANTED = 3;
    int PERMISSION_DENIED = 4;

    void onRequestPermissionsResult(int requestCode, int grantResult, String permission);

    int getRequestCode();
}
