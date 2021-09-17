package android_serialport_api.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import android_serialport_api.sample.bean.HighGpsObj;
import android_serialport_api.utils.FileUtil;
import android_serialport_api.utils.GPSRespUtil;
import android_serialport_api.utils.LogUtil;
import android_serialport_api.utils.StringUtil;
import android_serialport_api.utils.TimeUtil;


/**
 * 打点界面
 **/
public class PointMainActivity extends NetPortActivity implements View.OnClickListener {
    ProgressDialog progressDialog;
    private final static String TAG = "Point";
    private EditText etRoad, etBerth, etTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_main);
        initView();
        initData();
    }

    @Override
    void onDataReceive(byte[] readData) {
        parseHoleData(readData);
    }

    private final StringBuffer receiveSb = new StringBuffer();

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean startPoint;

    private final CopyOnWriteArrayList<HighGpsObj> gpsObjs = new CopyOnWriteArrayList<>();

    private void parseHoleData(byte[] buffer) {
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

    private void parseGpsStr(String str) {
        HighGpsObj gpsInfo = new HighGpsObj();
        try {
            if (str.contains("$GPGGA")) {
                str = str.substring(str.indexOf("$GPGGA"));
                boolean validOK = GPSRespUtil.xorString(str);
                LogUtil.d(TAG, "GPS,解析,定位信息:" + str + "-->效验:" + validOK);
                gpsInfo.setTs(System.currentTimeMillis());
                String[] tempGPGGA = str.split(",");
                gpsInfo.setLatitude(Math.abs(Double.parseDouble(GPSRespUtil.parseLat(tempGPGGA[2], tempGPGGA[3]))));
                gpsInfo.setLongitude(Math.abs(Double.parseDouble(GPSRespUtil.parseLat(tempGPGGA[4], tempGPGGA[5]))));
                gpsInfo.setGgaType(Integer.parseInt(tempGPGGA[6]));
                gpsObjs.add(gpsInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, Thread.currentThread().getName() + ",数据为空/有误:" + Log.getStackTraceString(e));
        }

    }

    final long now = System.currentTimeMillis();


        if(!ggaGet)

    {
        return;
    }
//        if (now - lastUi < 200) {
//            wwcutils.e(TAG, "当前时间:" + TimeUtil.long2Str(now, TimeUtil.DEFAULT_TIME_FORMAT_MS) + ",上一次时间:" + TimeUtil.long2Str(lastUi, TimeUtil.DEFAULT_TIME_FORMAT_MS));
//            return;
//        }
        if(startPoint)

    {
        gga = "0";
        gga = gpsInfo.ggaType;
        if (gpsInfo.ggaType.equals("4")) {
            pointGps.clear();
            pointGps.add(gpsInfo);
            pointGps.add(gpsInfo);
            pointGps.add(gpsInfo);
            startPoint = false;
        } else if (gpsInfo.ggaType.equals("5")) {
            pointGps.add(gpsInfo);
        }
        //wwcutils.e(TAG, Thread.currentThread().getName() + ",解析的数据,startPoint = " + startPoint + ", gga = " + gga + ", pointGps = " + pointGps);
    }
    //wwcutils.e(TAG, Thread.currentThread().getName() + ",解析的数据,startPoint = " + startPoint + ", gpsInfo = " + gpsInfo);
    currentGps =gpsInfo;

    runOnUiThread(new Runnable() {
        @Override
        public void run () {
            lastUi = now;
            TextView g1 = findViewById(R.id.gps1);
            TextView g2 = findViewById(R.id.gps2);
            TextView g3 = findViewById(R.id.gps3);
            TextView g4 = findViewById(R.id.gps4);
            g1.setText("经度:" + gpsInfo.longitude);
            g2.setText("纬度:" + gpsInfo.latitude);
            g3.setText("GGA状态:" + gpsInfo.ggaType);
            g4.setText("速度:" + gpsInfo.speed + "km/h");


        }
    });

}
    private String gga = "0";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                final String roadTxt = etRoad.getText().toString();
                if (TextUtils.isEmpty(roadTxt)) {
                    Toast.makeText(PointMainActivity.this, "请输入路段", Toast.LENGTH_LONG).show();
                }
                final String pointTxt = etBerth.getText().toString();
                if (TextUtils.isEmpty(pointTxt)) {
                    Toast.makeText(PointMainActivity.this, "请输入点", Toast.LENGTH_LONG).show();
                    return;
                }
                if (startPoint) {
                    return;
                }
                progressDialog.show();
                new Thread(() -> {
                    gpsObjs.clear();
                    startPoint = true;
                    long start = System.currentTimeMillis();
                    long now = start;
                    while (startPoint && now - start < 10 * 1000) {
                        LogUtil.d(TAG, Thread.currentThread().getName() + ",>>>" + now + ">>>" + start);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        now = System.currentTimeMillis();
                        if (gpsObjs.size() > 3) {
                            startPoint = false;
//                                break;
                        }
                    }
                    int count = gpsObjs.size();
                    LogUtil.e(TAG, Thread.currentThread().getName() + ",pointGps count =" + count + ",gga = " + gga);
                    if (count > 0 && ("4".equals(gga) || "5".equals(gga))) {
                        final String txtGga = gga;
                        double lat = 0;
                        double lon = 0;
                        try {
                            double latSum = 0;
                            double lonSum = 0;
                            for (HighGpsObj gpsInfo : gpsObjs) {
                                latSum += gpsInfo.getLatitude();
                                lonSum += gpsInfo.getLongitude();
                            }
                            lat = latSum / count;
                            lon = lonSum / count;
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtil.e(TAG, e.getStackTrace());
                        }
                        FileUtil.createOrExistsFile(Constants.POINT_PATH);
                        String msg = TimeUtil.date2Str(new Date(), TimeUtil.DEFAULT_TIME_FORMAT)
                                + "," + roadTxt
                                + "," + pointTxt
                                + "," + lon
                                + "," + lat
                                + "," + gga;
                        FileUtil.writeFileFromString(new File(Constants.POINT_PATH), msg, true);
                        runOnUiThread(() -> {
                            try {
                                progressDialog.dismiss();
                                Toast.makeText(PointMainActivity.this, "打点成功", Toast.LENGTH_LONG).show();
                                etRoad.setText("");
                                etBerth.setText("");
                            } catch (Exception e) {
                                LogUtil.e(TAG, Thread.currentThread().getName()+",打点失败:"+Log.getStackTraceString(e));
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            AlertDialog alertDialog = new AlertDialog.Builder(PointMainActivity.this).create();
                            alertDialog.setTitle("打点失败");
                            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定", (dialog, which) -> dialog.dismiss());
                            alertDialog.show();
                        });
                    }


                }).start();
                break;
            case R.id.clear:
                File pointFile = new File(Constants.POINT_PATH);
                if (pointFile.exists()) {
                    pointFile.delete();
                }
                Toast.makeText(PointMainActivity.this, "清除成功", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    @Override
    protected void initView() {
        etRoad = findViewById(R.id.road_num_et);
        etBerth = findViewById(R.id.editText);
        etTag = findViewById(R.id.editText1);
        Button btnAdd = findViewById(R.id.add);
        Button btnClear = findViewById(R.id.clear);
        TextView tvGPS1 = findViewById(R.id.gps1);
        TextView tvGPS2 = findViewById(R.id.gps2);
        TextView tvGPS3 = findViewById(R.id.gps3);
        TextView tvGPS4 = findViewById(R.id.gps4);
        progressDialog = new ProgressDialog(PointMainActivity.this);
        progressDialog.setTitle("正在打点");
    }

    @Override
    protected void initData() {
    }
}