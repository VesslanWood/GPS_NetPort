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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.easysocket.EasySocket;

import android_serialport_api.utils.ByteConvert;
import android_serialport_api.utils.CommandUtil;
import android_serialport_api.utils.LogUtil;

public class ConsoleActivity extends NetPortActivity {

    private static final String TAG = ConsoleActivity.class.getName();

    private TextView mReception;
    private EditText Emission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        mReception = (TextView) findViewById(R.id.EditTextReception);
        MyClickListener listener = new MyClickListener();
        findViewById(R.id.Send).setOnClickListener(listener);
        findViewById(R.id.Clear).setOnClickListener(listener);
        Emission = (EditText) findViewById(R.id.EditTextEmission);
    }

    @Override
    void onDataReceive(byte[] readData) {
        //GPSRespUtil.parseHoleData(readData, receiveSb);
        runOnUiThread(() -> {
            if (mReception != null) {
                mReception.append(new String(readData));
            }
        });
    }

    class MyClickListener implements OnClickListener {
        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Send:
                    String str = Emission.getText().toString();
                    try {
                        if (!TextUtils.isEmpty(str)) {
                            byte[] command = ByteConvert.hexStringToBytes(str);
                            EasySocket.getInstance().upMessage(command, NET_ADDRESS);
                            LogUtil.d("", ",发送结束1:" + ByteConvert.bytesToHex(command));
                        } else {
                            byte[] command = CommandUtil.sendTest();
                            EasySocket.getInstance().upMessage(command, NET_ADDRESS);
                            LogUtil.d("", ",发送结束2:" + ByteConvert.bytesToHex(command));
                        }
                    } catch (Exception e) {
                        LogUtil.d(TAG, Thread.currentThread().getName() + ",GPS,发送异常:" + Log.getStackTraceString(e));
                    }
                    break;
                case R.id.Clear:
                    mReception.setText("");
                    Emission.setText("");
                    break;
                default:
                    break;
            }
        }
    }

}
