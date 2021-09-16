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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.easysocket.EasySocket;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;

import java.io.IOException;

import android_serialport_api.utils.ByteConvert;
import android_serialport_api.utils.CommandUtil;
import android_serialport_api.utils.GPSRespUtil;
import android_serialport_api.utils.LogUtil;
import android_serialport_api.utils.StringUtil;

public class ConsoleActivity extends NetPortActivity {

    private static final String TAG = ConsoleActivity.class.getName();

    TextView mReception;
    EditText Emission;
    Button Send, Clear;
    CheckBox hexSend;
    private final StringBuffer receiveSb = new StringBuffer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.console);
        initSocket();
        mReception = (TextView) findViewById(R.id.EditTextReception);
        MyClickListener listener = new MyClickListener();
        Send = (Button) findViewById(R.id.Send);
        Clear = (Button) findViewById(R.id.Clear);
        Send.setText("Send");
        Clear.setText("Clear");
        Clear.setOnClickListener(listener);
        Send.setOnClickListener(listener);
        Emission = (EditText) findViewById(R.id.EditTextEmission);
    }

    @Override
    void onDataReceive(byte[] readData) {
        GPSRespUtil.parseHoleData(readData, receiveSb);
        runOnUiThread(() -> {
            if (mReception != null) {
                mReception.append(new String(readData));
            }
        });
    }

    class MyClickListener implements OnClickListener {
        Boolean thread_flag = true, PWM_flag = true, LASER_flag;

        @SuppressLint("NonConstantResourceId")
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Send:
                    String str = Emission.getText().toString();
                    if (!TextUtils.isEmpty(str)) {
                        byte[] command = ByteConvert.hexStringToBytes(str);
                        EasySocket.getInstance().upMessage(command, NET_ADDRESS);
                        LogUtil.e("", ",发送结束1:" + ByteConvert.bytesToHex(command));
                    } else {
                        byte[] command = CommandUtil.sendTest();
                        EasySocket.getInstance().upMessage(command, NET_ADDRESS);
                        LogUtil.e("", ",发送结束2:" + ByteConvert.bytesToHex(command));
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
