package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/*
    권한설정
    GPS, WIFI 설정
 */

public class IntroActivity extends AppCompatActivity {

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.RECORD_AUDIO"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        init();
    }

    private void init() {
        if (!checkPermissions(PERMISSIONS)) {
            requestNecessaryPermissions(PERMISSIONS);
        } else {
            moveMain();
        }
    }

    private boolean checkPermissions(String[] permissions) {
        int res = 0;
        for (String perms : permissions) {
            res = this.checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!readAccepted || !writeAccepted) {
                            ToastMsg("권한을 허용해주세요..");
                            finish();
                            return;
                        } else {
                            //권한 허용 하고 나서...
                            moveMain();
                        }
                    }
                }
                break;
        }
    }

    private void moveMain() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class) ;
        startActivity(intent);
        finish();
    }

    private void ToastMsg(final String msg) {
        final String message = msg;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
