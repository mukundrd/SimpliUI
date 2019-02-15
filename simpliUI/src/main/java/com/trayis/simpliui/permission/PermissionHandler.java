package com.trayis.simpliui.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.trayis.simpliui.permission.PermissionCallback.PERMISSION_ALREADY_AVAILABLE;
import static com.trayis.simpliui.permission.PermissionCallback.PERMISSION_REQUESTED;
import static com.trayis.simpliui.permission.PermissionCallback.SHOW_PERMISSION_RATIONALE;

/**
 * Created by mukundrd on 6/8/17.
 */

public class PermissionHandler {

    private Activity activity;

    private final String permission;

    private final OnDeniedPermissionCallback deniedCallback;

    private final PermissionCallback permissionCallback;

    private PermissionHandler(Activity activity, String permission, OnDeniedPermissionCallback deniedCallback, PermissionCallback callback) {
        this.activity = activity;
        this.permission = permission;
        this.deniedCallback = deniedCallback;
        this.permissionCallback = callback;
    }

    public boolean checkIfPermissionIsAvailable(String permission) {
        return ContextCompat.checkSelfPermission(this.activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        requestPermission(false);
    }

    public void requestPermission(boolean isRationale) {
        if (isRationale) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCallback.getRequestCode());
            permissionCallback.onRequestPermissionsResult(permissionCallback.getRequestCode(), PERMISSION_REQUESTED, permission);
            return;
        }
        final int permissionResult = ContextCompat.checkSelfPermission(activity, permission);

        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permissionCallback.onRequestPermissionsResult(permissionCallback.getRequestCode(), SHOW_PERMISSION_RATIONALE, permission);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionCallback.getRequestCode());
                permissionCallback.onRequestPermissionsResult(permissionCallback.getRequestCode(), PERMISSION_REQUESTED, permission);
            }
        } else {
            permissionCallback.onRequestPermissionsResult(permissionCallback.getRequestCode(), PERMISSION_ALREADY_AVAILABLE, permission);
        }
    }

    public void clear() {
        this.activity = null;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || grantResults == null) {
            return;
        }
        if (permissionCallback != null && grantResults.length > 0) {
            int grantResult;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                grantResult = PermissionCallback.PERMISSION_GRANTED;
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                grantResult = PermissionCallback.PERMISSION_DENIED;
                if (deniedCallback != null) {
                    deniedCallback.onPermissionDenied();
                }
            } else {
                grantResult = SHOW_PERMISSION_RATIONALE;
            }
            permissionCallback.onRequestPermissionsResult(requestCode, grantResult, permission);
        }
    }

    public static class Builder {

        private final Activity activity;

        private String permission;

        private OnDeniedPermissionCallback deniedCallback;

        private PermissionCallback callback;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder withPermission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder withDeniedCallback(OnDeniedPermissionCallback deniedCallback) {
            this.deniedCallback = deniedCallback;
            return this;
        }

        public Builder withPermissionCallback(PermissionCallback callback) {
            this.callback = callback;
            return this;
        }

        public PermissionHandler build() {
            return new PermissionHandler(activity, permission, deniedCallback, callback);
        }

    }
}
