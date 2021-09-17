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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import android_serialport_api.utils.LogUtil;

public class MainMenu extends Activity {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        providePermissions();
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

    }


    /**
     * 授权APP所需要的所有权限
     **/
    private void providePermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE// 写入权限
                , Manifest.permission.READ_EXTERNAL_STORAGE // 读取权限
                , Manifest.permission.READ_PHONE_STATE//手机状态权限
                , Manifest.permission.ACCESS_FINE_LOCATION//定位权限
                , Manifest.permission.ACCESS_COARSE_LOCATION//WIFI定位
        };
        XXPermissions.with(MainMenu.this)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            Toast.makeText(MainMenu.this, "获取读写权限成功", Toast.LENGTH_SHORT).show();
                            LogUtil.d("MainMenu", Thread.currentThread().getName() + ",当前的版本:" + BuildConfig.VERSION_NAME);
                        } else {
                            Toast.makeText(MainMenu.this, "获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            Toast.makeText(MainMenu.this, "被永久拒绝授权，请手动授予读写权限", Toast.LENGTH_SHORT).show();
                            XXPermissions.startPermissionActivity(MainMenu.this, permissions);
                        } else {
                            Toast.makeText(MainMenu.this, "获取读写权限失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
