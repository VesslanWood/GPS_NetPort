/*
 * Copyright 2011 Cedric Priscal
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

import java.io.IOException;
import java.util.Arrays;

import android.os.Bundle;

import com.easysocket.EasySocket;
import com.easysocket.interfaces.conn.ISocketActionListener;

public class Sending01010101Activity extends NetPortActivity {

    SendingThread mSendingThread;
    byte[] mBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sending01010101);
        mBuffer = new byte[1024];
        Arrays.fill(mBuffer, (byte) 0x55);
        mSendingThread = new SendingThread();
        mSendingThread.start();

    }

    @Override
    void onDataReceive(byte[] readData) {
        //接收数据

    }


    private class SendingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    EasySocket.getInstance().upMessage(mBuffer,NET_ADDRESS);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
