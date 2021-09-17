package android_serialport_api.utils;


/**
 * @author liuyasong
 * 坐标系转换工具类
 */
public class GPSUtil {
	private static double pi = 3.1415926535897932384626;
	private static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	private static double a = 6378245.0;
	private static double ee = 0.00669342162296594323;
 
	private static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}
 
	private static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
 
	private static double[] transform(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return new double[] { lat, lon };
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new double[] { mgLat, mgLon };
	}
 
	private static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}
 
	/**
	 * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static double[] gps84_To_Gcj02(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return new double[] { lat, lon };
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new double[] { mgLat, mgLon };
	}
 
	/**
	 * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
	 * */
	public static double[] gcj02_To_Gps84(double lat, double lon) {
		double[] gps = transform(lat, lon);
		double lontitude = lon * 2 - gps[1];
		double latitude = lat * 2 - gps[0];
		return new double[] { latitude, lontitude };
	}
 
	/**
	 * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
	 * 
	 * @param lat
	 * @param lon
	 */
	public static double[] gcj02_To_Bd09(double lat, double lon) {
		double x = lon, y = lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
		double tempLon = z * Math.cos(theta) + 0.0065;
		double tempLat = z * Math.sin(theta) + 0.006;
		double[] gps = { tempLat, tempLon };
		return gps;
	}
 
	/**
	 * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
	 * bd_lat * @param bd_lon * @return
	 */
	public static double[] bd09_To_Gcj02(double lat, double lon) {
		double x = lon - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
		double tempLon = z * Math.cos(theta);
		double tempLat = z * Math.sin(theta);
		double[] gps = { tempLat, tempLon };
		return gps;
	}

	public static double[] gcj02_To_Mercator84(double lat,double lon)
	{
		//GCJ02转web墨卡托84
		double earthRad = 6378137.0;
		double mlng = lon * Math.PI / 180 * earthRad;
		double a = lat * Math.PI / 180;
		double mlat = earthRad / 2 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
		double[] mxy = {mlat,mlng};
		return mxy;
	}
 
	/**
	 * 将gps84转为bd09
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static double[] gps84_To_bd09(double lat, double lon) {
		double[] gcj02 = gps84_To_Gcj02(lat, lon);
		double[] bd09 = gcj02_To_Bd09(gcj02[0], gcj02[1]);
		return bd09;
	}
 
	public static double[] bd09_To_gps84(double lat, double lon) {
		double[] gcj02 = bd09_To_Gcj02(lat, lon);
		double[] gps84 = gcj02_To_Gps84(gcj02[0], gcj02[1]);
		// 保留小数点后六位
		gps84[0] = retain6(gps84[0]);
		gps84[1] = retain6(gps84[1]);
		return gps84;
	}
 
	/**
	 * 保留小数点后7位
	 * 
	 * @param num
	 * @return
	 */
	private static double retain6(double num) {
		String result = String.format("%.7f", num);
		return Double.valueOf(result);
	}
	/**
	 *	GPS转高德/腾讯
	 * @param lon
	 * @param lat
	 * @return String[lon,lat]
	 */
	public static String[] gps_To_GD(double lon, double lat) {
		if (outOfChina(lat, lon)) {
			return new String[] { lon+"", lat+"" };
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new String[] { String.format("%.7f", mgLon) ,String.format("%.7f", mgLat)};
	}
	/**
	 *	高德/腾讯转GPS
	 * @param lon
	 * @param lat
	 * @return String[lon,lat]
	 */
	public static String[] GD_To_GPS(double lon, double lat) {
		if (outOfChina(lat, lon)) {
			return new String[] { lon+"", lat+"" };
		}
		double[] gcj02_To_Gps84 = gcj02_To_Gps84(lat, lon);
		return new String[] { String.format("%.7f", gcj02_To_Gps84[1]) ,String.format("%.7f", gcj02_To_Gps84[0])};
	}
	/**
	 *	百度转高德/腾讯
	 * @param lon
	 * @param lat
	 * @return String[lon,lat]
	 */
	public static String[] BD_To_GD(double lon, double lat) {
		if (outOfChina(lat, lon)) {
			return new String[] { lon+"", lat+"" };
		}
		double[] bd09_To_Gcj02 = bd09_To_Gcj02(lat, lon);
		return new String[] { String.format("%.7f", bd09_To_Gcj02[1]) ,String.format("%.7f", bd09_To_Gcj02[0])};
	}

	public static void main(String[] args) {
//		String str = "113.2362898,21.9053547;113.2353993,21.9076491;113.2352312,21.9079071;113.2345464,21.9086146;113.2342782,21.9089879;113.2341989,21.9093424;113.2342173,21.9101219;113.2345064,21.9118989";
//		String[] arr = str.split(";");
//		String res = "";
//		for(String s : arr) {
//			String[] lntlat = s.split(",");
//			Double lnt = Double.parseDouble(lntlat[0]);
//			Double lat = Double.parseDouble(lntlat[1]);
//
//			double[] bd09 = gps84_To_Gcj02( lat,lnt);
//			res += bd09[0] + "," + bd09[1] + ";";
//		}
//		res = res.substring(0, res.lastIndexOf(";"));
//		System.out.println(res);
		double gpsLon=116.28633;
		double gpsLat=39.8426916666667;
		System.out.println("GPS:"+gpsLon+","+gpsLat);
		String[] gps_To_GD = gps_To_GD(gpsLon, gpsLat);
		System.out.println("gps_To_GD:"+gps_To_GD[0]+","+gps_To_GD[1]);
		double[] gps84_To_Gcj02 = gps84_To_Gcj02(gpsLat,gpsLon);
		System.out.println("gps84_To_Gcj02:"+gps84_To_Gcj02[1]+","+gps84_To_Gcj02[0]);
		String[] GD_To_GPS = GD_To_GPS(gps84_To_Gcj02[1], gps84_To_Gcj02[0]);
		System.out.println("GD_To_GPS:"+GD_To_GPS[0]+","+GD_To_GPS[1]);
		double[] gcj02_To_Gps84 = gcj02_To_Gps84(gps84_To_Gcj02[0], gps84_To_Gcj02[1]);
		System.out.println("gcj02_To_Gps84:"+gcj02_To_Gps84[1]+","+gcj02_To_Gps84[0]);
	}
 
}

