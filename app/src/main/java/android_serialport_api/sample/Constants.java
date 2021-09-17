package android_serialport_api.sample;

import android.os.Environment;

/**
 * <p>文件描述：常量<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/9/16<p>
 * <p>更新时间：2021/9/16<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public class Constants {
    public static final String[] NET_IPS = new String[]{"172.23.100.151", "172.23.100.1", "192.168.0.151", "192.168.0.100"};
    public static final String POINT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GPSPoint/" + "point.txt";
    public static final long ONE_MILL_TIME = 1000L;
    public static final long COLLECT_GPS_TIME_OUT = ONE_MILL_TIME;

}
