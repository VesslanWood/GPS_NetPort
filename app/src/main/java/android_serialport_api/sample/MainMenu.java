/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android_serialport_api.sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.util.Date;
import java.util.List;

import android_serialport_api.utils.CrashHandler;
import android_serialport_api.utils.FileUtil;
import android_serialport_api.utils.LogUtil;
import android_serialport_api.utils.TimeUtil;

public class MainMenu extends FragmentActivity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.ButtonSetup).setOnClickListener(v -> startActivity(new Intent(MainMenu.this, NetPortPreferences.class)));
        findViewById(R.id.point).setOnClickListener(v -> startActivity(new Intent(MainMenu.this, PointMainActivity.class)));
        findViewById(R.id.ButtonConsole).setOnClickListener(v -> startActivity(new Intent(MainMenu.this, ConsoleActivity.class)));
        findViewById(R.id.Button01010101).setOnClickListener(v -> startActivity(new Intent(MainMenu.this, Sending01010101Activity.class)));
        findViewById(R.id.ButtonAbout).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
            builder.setTitle("About");
            builder.setMessage(R.string.about_msg);
            builder.show();
        });
        findViewById(R.id.ButtonQuit).setOnClickListener(v -> MainMenu.this.finish());
        providePermissions();
    }


    /**
     * ??????APP????????????????????????
     **/
    private void providePermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE// ????????????
                , Manifest.permission.READ_EXTERNAL_STORAGE // ????????????
                , Manifest.permission.READ_PHONE_STATE//??????????????????
                , Manifest.permission.ACCESS_FINE_LOCATION//????????????
                , Manifest.permission.ACCESS_COARSE_LOCATION//WIFI??????
                , Manifest.permission.INTERNET
        };
        XXPermissions.with(MainMenu.this)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            Toast.makeText(MainMenu.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                            CrashHandler.getInstance().init(MyApplication.getContext());
                            //LogUtil.d("MainMenu", Thread.currentThread().getName() + ",???????????????:" + BuildConfig.VERSION_NAME);
                        } else {
                            Toast.makeText(MainMenu.this, "?????????????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(MainMenu.this, "???????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                            XXPermissions.startPermissionActivity(MainMenu.this, permissions);
                        } else {
                            Toast.makeText(MainMenu.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
