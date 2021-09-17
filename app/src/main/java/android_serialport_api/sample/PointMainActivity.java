package android_serialport_api.sample;

import android.annotation.SuppressLint;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android_serialport_api.sample.bean.HighGpsObj;
import android_serialport_api.utils.FileUtil;
import android_serialport_api.utils.GPSRespUtil;
import android_serialport_api.utils.GPSUtil;
import android_serialport_api.utils.LogUtil;
import android_serialport_api.utils.StringUtil;
import android_serialport_api.utils.TimeUtil;


/**
 * 打点界面
 **/
public class PointMainActivity extends NetPortActivity implements View.OnClickListener {
    ProgressDialog progressDialog;
    private final static String TAG = PointMainActivity.class.getName();
    private EditText etRoad, etBerth, etTag;
    private TextView tvGPS1, tvGPS2, tvGPS3;
    private final CopyOnWriteArrayList<HighGpsObj> gpsObjs = new CopyOnWriteArrayList<>();
    private final StringBuffer receiveSb = new StringBuffer();

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

    @SuppressLint("SetTextI18n")
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
                runOnUiThread(() -> {
                    tvGPS1.setText("经度:" + gpsInfo.getLongitude());
                    tvGPS2.setText("纬度:" + gpsInfo.getLatitude());
                    tvGPS3.setText("GGA状态:" + gpsInfo.getGgaType());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, Thread.currentThread().getName() + ",数据为空/有误:" + Log.getStackTraceString(e));
        }

    }

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
                String tagTxt = etTag.getText().toString();
                if (TextUtils.isEmpty(tagTxt)) {
                    Toast.makeText(PointMainActivity.this, "请输入标号", Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog.show();
                new Thread(() -> {
                    gpsObjs.clear();
                    judgeListSize();
                    if (gpsObjs.size() > 0) {
                        HighGpsObj averageHighGpsObj = average(gpsObjs);
                        if (averageHighGpsObj == null) {
                            showPointFail();
                            return;
                        }
                        String[] gdGps = GPSUtil.gps_To_GD(averageHighGpsObj.getLongitude(), averageHighGpsObj.getLongitude());
                        double[] bdGps = GPSUtil.gps84_To_bd09(averageHighGpsObj.getLongitude(), averageHighGpsObj.getLongitude());
                        FileUtil.createOrExistsFile(Constants.POINT_PATH);
                        String msg = TimeUtil.date2Str(new Date(), TimeUtil.DEFAULT_TIME_FORMAT)
                                + "," + roadTxt
                                + "," + pointTxt + "-" + tagTxt
                                + "," + averageHighGpsObj.getLongitude()
                                + "," + averageHighGpsObj.getLongitude()
                                + "," + gdGps[0]
                                + "," + gdGps[1]
                                + "," + bdGps[1]
                                + "," + bdGps[0]
                                + "," + averageHighGpsObj.getGgaType();
                        FileUtil.writeFileFromLineString(new File(Constants.POINT_PATH), msg, true);
                        showPointSuccess();
                    } else {
                        showPointFail();
                    }
                }).start();
                break;
            case R.id.clear:
                File pointFile = new File(Constants.POINT_PATH);
                if (pointFile.exists()) {
                    pointFile.delete();
                }
                Toast.makeText(PointMainActivity.this, "清除文件成功", Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }
    }

    private void showPointSuccess() {
        runOnUiThread(() -> {
            if (null != progressDialog && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            Toast.makeText(PointMainActivity.this, "打点成功", Toast.LENGTH_LONG).show();
            etBerth.setText("");
            etTag.setText("");
        });
    }

    private void showPointFail() {
        runOnUiThread(() -> {
            progressDialog.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(PointMainActivity.this).create();
            alertDialog.setTitle("打点失败");
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定", (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });
    }

    /**
     * 循环获取点的长度
     **/
    private void judgeListSize() {
        long startCollectMillTime = System.currentTimeMillis();
        while (gpsObjs.size() <= 3) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (System.currentTimeMillis() - startCollectMillTime > Constants.COLLECT_GPS_TIME_OUT) {
                runOnUiThread(() -> Toast.makeText(PointMainActivity.this, "附近没有GPS有效信息", Toast.LENGTH_LONG).show());
                LogUtil.d(TAG, Thread.currentThread().getName() + ",GPS,附近没有GPS信号");
                break;
            }
        }
    }

    /**
     * 计算多点的平均值
     **/
    private HighGpsObj average(CopyOnWriteArrayList<HighGpsObj> gpsObjs) {
        HighGpsObj result = new HighGpsObj();
        int count = gpsObjs.size();
        double averageLat = 0d;
        double averageLon = 0d;
        long averageTs = 0L;
        int maxGPSType = -1;
        Map<Integer, Integer> gpsTypeMap = new ConcurrentHashMap<>();
        try {
            double latSum = 0d;
            double lonSum = 0d;
            long tsSum = 0L;
            for (HighGpsObj gpsInfo : gpsObjs) {
                latSum += gpsInfo.getLatitude();
                lonSum += gpsInfo.getLongitude();
                tsSum += gpsInfo.getTs();
                int gpsType = gpsInfo.getGgaType();
                if (gpsTypeMap.containsKey(gpsType)) {
                    int next = gpsTypeMap.get(gpsType) + 1;
                    gpsTypeMap.put(gpsType, next);
                } else {
                    gpsTypeMap.put(gpsType, 1);
                }
            }
            maxGPSType = (int) StringUtil.getMaxValue(gpsTypeMap);
            averageLat = latSum / count;
            averageLon = lonSum / count;
            averageTs = tsSum / count;
        } catch (Exception e) {
            LogUtil.e(TAG, Thread.currentThread().getName() + ",average:" + Log.getStackTraceString(e));
            return null;
        }
        result.setLatitude(averageLat);
        result.setLongitude(averageLon);
        result.setTs(averageTs);
        result.setGgaType(maxGPSType);
        return result;
    }

    @Override
    protected void initView() {
        etRoad = findViewById(R.id.road_num_et);
        etBerth = findViewById(R.id.editText);
        etTag = findViewById(R.id.editText1);
        Button btnAdd = findViewById(R.id.add);
        Button btnClear = findViewById(R.id.clear);
        tvGPS1 = findViewById(R.id.gps1);
        tvGPS2 = findViewById(R.id.gps2);
        tvGPS3 = findViewById(R.id.gps3);
        progressDialog = new ProgressDialog(PointMainActivity.this);
        progressDialog.setTitle("正在打点");
    }

    @Override
    protected void initData() {
    }
}