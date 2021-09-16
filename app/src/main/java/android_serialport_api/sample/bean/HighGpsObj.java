package android_serialport_api.sample.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>文件描述：高精度GPS对象<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/9/2<p>
 * <p>更新时间：2021/9/2<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public class HighGpsObj implements Serializable {
    private static final long serialVersionUID = 8528778371867075358L;
    private long ts;//系统时间
    private double longitude;//经度
    private double latitude; //纬度
    private double speed;    //速度
    private String gpsStatus;//GPS状态 A=数据有效；V=数据无效
    private int ggaType;//0：无效解；1：单点定位解；2：伪距差分；4：固定解；5：浮动解。
    private Date gpsTime;//GPS的时间

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(String gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public int getGgaType() {
        return ggaType;
    }

    public void setGgaType(int ggaType) {
        this.ggaType = ggaType;
    }

    public Date getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(Date gpsTime) {
        this.gpsTime = gpsTime;
    }

    @Override
    public String toString() {
        return "HighGpsObj{" +
                "ts=" + ts +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", speed=" + speed +
                ", gpsStatus='" + gpsStatus + '\'' +
                ", ggaType='" + ggaType + '\'' +
                ", gpsTime=" + gpsTime +
                '}';
    }


}
