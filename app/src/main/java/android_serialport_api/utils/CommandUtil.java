package android_serialport_api.utils;

/**
 * <p>文件描述：<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/8/30<p>
 * <p>更新时间：2021/8/30<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public class CommandUtil {
    public static byte[] sendTest() {
        byte[] strByte = new byte[5];
        int count = strByte.length;
        while (count > 0) {
            strByte[5 - count] = (byte) 0XBE;
            count--;
        }
        return strByte;
    }
}
