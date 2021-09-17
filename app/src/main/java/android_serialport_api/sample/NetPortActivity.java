package android_serialport_api.sample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import android_serialport_api.utils.GPSRespUtil;
import android_serialport_api.utils.LogUtil;
import android_serialport_api.utils.StringUtil;

/**
 * <p>文件描述：网口抽象类<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/9/16<p>
 * <p>更新时间：2021/9/16<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public abstract class NetPortActivity extends Activity {
    public static final String TAG = NetPortActivity.class.getName();

    protected static String NET_ADDRESS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSocket();
    }

    private final ISocketActionListener socketActionListener = new ISocketActionListener() {
        @Override
        public void onSocketConnSuccess(SocketAddress socketAddress) {
            LogUtil.d(TAG, Thread.currentThread().getName() + ",GPS,连接成功开始读取");
        }

        @Override
        public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(TAG, Thread.currentThread().getName() + ",GPS,onSocketConnFail");
            initSocket();
        }

        @Override
        public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
            LogUtil.d(TAG, Thread.currentThread().getName() + ",GPS,onSocketDisconnect");
        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {

        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, String readData) {

        }

        @Override
        public void onSocketResponse(SocketAddress socketAddress, byte[] readData) {
            onDataReceive(readData);
        }
    };


    /**
     * 初始化网口GPS
     **/
    protected void initSocket() {
        // socket配置
        SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
        String ip = sp.getString("IP", "");
        int port = Integer.decode(sp.getString("PORT", "-1"));
        /* Check parameters */
        if ((ip.length() == 0) || (port == -1)) {
            Toast.makeText(this, "请先SETUP设置网络", Toast.LENGTH_LONG).show();
            finish();
            return;
            //throw new InvalidParameterException();
        }
        LogUtil.w(TAG, Thread.currentThread().getName() + ",GPS,初始化网口-连接:" + ip + ":" + port);
        NET_ADDRESS = ip + ":" + port;
        EasySocketOptions options = new EasySocketOptions.Builder()
                // 主机地址，请填写自己的IP地址，以getString的方式是为了隐藏作者自己的IP地址
                .setSocketAddress(new SocketAddress(ip, port))
                .setCharsetName("gbk")
                .build();
        /**
         * 创建指定的socket连接，如果你的项目有多个socket连接，可以用这个方法创建连接，后面你要操作某个连接的时候就要使用带有socket地址的方法
         * 比如：{@link EasySocket#upMessage(byte[], java.lang.String)}，{@link EasySocket#connect(String)} 等
         *
         * @param socketOptions
         * @return
         */
        Executors.newCachedThreadPool().execute(() -> {
            EasySocket.getInstance().createSpecifyConnection(options, MyApplication.getContext());
            EasySocket.getInstance().subscribeSocketAction(socketActionListener, NET_ADDRESS);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != NET_ADDRESS) {
            EasySocket.getInstance().destroyConnection(NET_ADDRESS);
        }

    }

    abstract void onDataReceive(byte[] readData);

    protected void initView() {
    }
}
