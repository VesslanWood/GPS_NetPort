package android_serialport_api.sample.bean;

/**
 * <p>文件描述：<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/9/18<p>
 * <p>更新时间：2021/9/18<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public enum GPSType {

    INVALID(0),
    UNIT_SOLVE(1),
    PSEUDO_DIFF(2),
    FIXED_SOLVE(4),
    FLOAT_SOLVE(5);
    private int key;

    GPSType(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
