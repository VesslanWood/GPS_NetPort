package android_serialport_api.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import android_serialport_api.sample.MyApplication;
import android_serialport_api.sample.bean.HighGpsObj;

/**
 * <p>文件描述：解析GPS的工具<p>
 * <p>作者：jambestwick<p>
 * <p>创建时间：2021/8/24<p>
 * <p>更新时间：2021/8/24<p>
 * <p>版本号：<p>
 * <p>邮箱：jambestwick@126.com<p>
 */
public class GPSRespUtil implements Serializable {
    private static final long serialVersionUID = -3605916896608969049L;
    static long CRC32_POLYNOMIAL = 0xEDB88320L;
    private static final String TAG = GPSRespUtil.class.getName();


    /**
     * 是否包含开头的$
     **/
    public static boolean hasHead(String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        return data.contains("#") || data.contains("$");
    }

    public static boolean hasEnd(String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        return data.contains("*");
    }

    public static boolean isFullResp(String data) {
        if (!hasHead(data)) {
            return false;
        }
        if (!hasEnd(data)) {
            return false;
        }
        //LogUtil.d(TAG, "GPS,有头有尾:" + data);
        String subXor = data.substring(data.indexOf("*") + 1);
        if (subXor.length() < 2) {
            return false;
        }
        int tempHeadIndex = 0;
        int indexJing = data.indexOf("#");
        int indexDollar = data.indexOf("$");
        tempHeadIndex = Math.max(indexJing, indexDollar);
        int indexEnd = data.indexOf("*");
        int indexLastEnd = data.lastIndexOf("*");
        if (indexEnd == indexLastEnd) {
            return indexEnd >= tempHeadIndex;
        } else {
            return indexLastEnd > tempHeadIndex;
        }
    }

    public static boolean xorString(String origin) {
        String valueData = "";
        if (origin.contains("#")) {
            valueData = origin.substring(origin.indexOf("#") + 1, origin.indexOf("*"));
        }
        if (origin.contains("$")) {
            valueData = origin.substring(origin.indexOf("$") + 1, origin.indexOf("*"));
        }

        byte temp = 0;
        for (int i = 0; i < valueData.length(); i++) {

            temp ^= valueData.charAt(i);

        }
        String xorHex = ByteConvert.bytesToHex(new byte[]{temp});
        String end = origin.substring(origin.indexOf("*") + 1);
        return xorHex.equalsIgnoreCase(end);
    }

    /* --------------------------------------------------------------------------
    Calculate a CRC value
    value: Value
    -------------------------------------------------------------------------- */
    private static long CalcCRC32Value(int value) {
        int i;
        long ulCRC;
        ulCRC = value;
        for (i = 8; i > 0; --i) {
            if ((ulCRC & 1) == 0)
                ulCRC = (ulCRC >> 1) ^ CRC32_POLYNOMIAL;
            else
                ulCRC >>= 1;
        }
        return ulCRC;
    }
/* --------------------------------------------------------------------------
Calculates the CRC-32 of a data block
ulCount: Number of bytes in the data block
ucBuff: Data block
-------------------------------------------------------------------------- */

    public static long CalcBlockCRC32(long ulCount, String ucBuff) {
        char ucHash = (char) ucBuff.hashCode();
        long ulTmp1;
        long ulTmp2;
        long ulCRC = 0;
        while (ulCount-- != 0) {
            ulTmp1 = (ulCRC >> 8) & 0x00FFFFFFL;
            ulTmp2 = CalcCRC32Value(((int) ulCRC ^ ucHash++) & 0xFF);
            ulCRC = ulTmp1 ^ ulTmp2;
        }
        return ulCRC;
    }



    public static void parseHoleData(final byte[] buffer, StringBuffer receiveSb) {
        String res = new String(buffer);
        LogUtil.d(TAG, "收←◆" + res);
        String responseWithLine = StringUtil.addLineHeadByParams(res).trim();
        String[] s = null;
        try {
            s = responseWithLine.split("\r\n");
            receiveSb.append(s[0]);
            if (GPSRespUtil.isFullResp(receiveSb.toString())) {
                String withOutFit = receiveSb.toString();
                receiveSb.delete(0, withOutFit.length());
                parseGpsStr(withOutFit);
            }
            if (s.length > 1) {
                receiveSb.append(s[1]);
                if (GPSRespUtil.isFullResp(receiveSb.toString())) {
                    String withOutFit = receiveSb.toString();
                    receiveSb.delete(0, withOutFit.length());
                    parseGpsStr(withOutFit);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, ",GPS,onDataReceived,Exception:" + Log.getStackTraceString(e));
            receiveSb.delete(0, receiveSb.length());
            if (null != s && s.length > 1) {
                LogUtil.d(TAG, ",GPS,补到E后面:" + receiveSb.toString() + ",s数组长度:" + s.length);
                receiveSb.append(s[1]);
                LogUtil.d(TAG, ",GPS,补到E后面,结果:" + receiveSb.toString());
            }
        }
    }

    private static void parseGpsStr(String str) {
        HighGpsObj gpsInfo = new HighGpsObj();
        try {
            if (str.contains("$GPGGA")) {
                str = str.substring(str.indexOf("$GPGGA"));
                boolean validOK = GPSRespUtil.xorString(str);
                LogUtil.d(TAG, "GPS,解析,定位信息:" + str + "-->效验:" + validOK);
                gpsInfo.setTs(System.currentTimeMillis());
                String[] tempGPGGA = str.split(",");
                gpsInfo.setLatitude(Math.abs(Double.parseDouble(parseLat(tempGPGGA[2], tempGPGGA[3]))));
                gpsInfo.setLongitude(Math.abs(Double.parseDouble(parseLon(tempGPGGA[4], tempGPGGA[5]))));
                gpsInfo.setGgaType(Integer.parseInt(tempGPGGA[6]));
            } else if (str.contains("$GPRMC")) {
                str = str.substring(str.indexOf("$GPRMC"));
                LogUtil.d(TAG, "GPS,解析,速度信息:" + str);
                String[] tempGPRMC = str.split(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String parseLat(String lat, String type) {
        //纬度
        double latitude = Double.parseDouble(lat.substring(0, 2));
        latitude += Double.parseDouble(lat.substring(2)) / 60;
        if ("N".equals(type)) { //北纬
            return String.valueOf(latitude);
        } else { //南纬
            return "-" + String.valueOf(latitude);
        }
    }

    private static String parseLon(String lon, String type) {
        //经度
        double longitude = Double.parseDouble(lon.substring(0, 3));
        longitude += Double.parseDouble(lon.substring(3)) / 60;
        if ("E".equals(type)) {  //东经
            return String.valueOf(longitude);
        } else {  //西经
            return "-" + String.valueOf(longitude);
        }
    }


}
