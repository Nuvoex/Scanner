package nuvoex.com.scanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by KhushbooGupta on 6/24/16.
 */
public abstract class MarshmallowSupportActivity extends AppCompatActivity {


    public interface FragmentPermissionCallback {
        void onPermissionGranted(int requestCode);
    }

    public FragmentPermissionCallback mFragmentPermissionCallback = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (verifyPermissions(grantResults)) {
            onPermissionGranted(requestCode, mFragmentPermissionCallback);
        } else {
            boolean showRationale = shouldShowRequestPermissionRationale(permissions);
            if (!showRationale) {
                showSettingsPermissionDialog(requestCode);
            } else {
                showRequestPermissionDialog(requestCode, permissions);
            }

        }

    }

    public void requestAppPermissions(final String[] requestedPermissions, final int requestCode) {
        requestAppPermissions(requestedPermissions, requestCode, null);
    }

    public void requestAppPermissions(final String[] requestedPermissions, final int requestCode, @Nullable FragmentPermissionCallback permissionCallback) {

        mFragmentPermissionCallback = permissionCallback;
        if (!hasPermissions(requestedPermissions)) {
            if (shouldShowRequestPermissionRationale(requestedPermissions)) {
                showRequestPermissionDialog(requestCode, requestedPermissions);
            } else {
                ActivityCompat.requestPermissions(MarshmallowSupportActivity.this, requestedPermissions, requestCode);
            }
        } else {
            onPermissionGranted(requestCode, mFragmentPermissionCallback);
        }
    }

    protected boolean hasPermissions(String[] permissions) {
        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    public boolean shouldShowRequestPermissionRationale(String[] permissions) {
        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]))
                return true;
        }
        return false;
    }

    private boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1)
            return false;

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private void showRequestPermissionDialog(final int requestCode, final String[] requestedPermissions) {
        String positiveButton;
        String message;
        message = getString(R.string.permission_camera_retry);
        positiveButton = getString(R.string.permission_retry_btn);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                ActivityCompat.requestPermissions(MarshmallowSupportActivity.this, requestedPermissions, requestCode);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void showSettingsPermissionDialog(int requestCode) {
        String positiveButton;
        String message;
        message = getString(R.string.permission_camera_setting);

        positiveButton = getString(R.string.permission_stng_btn);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);

            }
        });
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.show();

    }

    public abstract void onPermissionGranted(int requestCode, @Nullable FragmentPermissionCallback permissionCallback);

}
