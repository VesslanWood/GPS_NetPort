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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import android_serialport_api.sample.bean.GPSType;
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
    private StringBuffer receiveSb = new StringBuffer();
    private boolean onDestroy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_main);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        onDestroy = false;
        timeCleanGpsList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        onDestroy = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroy = true;
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
            dealRespData();
            if (s.length > 1) {
                receiveSb.append(s[1]);
                dealRespData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, ",GPS,onDataReceived,Exception:" + Log.getStackTraceString(e));
            receiveSb.delete(0, receiveSb.length());
        }
    }

    private void dealRespData() {
        if (GPSRespUtil.isFullResp(receiveSb.toString())) {
            String withOutFit = receiveSb.toString();
            receiveSb.delete(0, withOutFit.length());
            HighGpsObj highGpsObj = GPSRespUtil.parseGpsStr(withOutFit);
            refreshView(highGpsObj);
        }
    }

    @SuppressLint("SetTextI18n")
    private void refreshView(HighGpsObj gpsInfo) {
        if (null != gpsInfo) {
            if (gpsInfo.getGgaType() == GPSType.FIXED_SOLVE.getKey()) {
                gpsObjs.add(gpsInfo);
            }
            runOnUiThread(() -> {
                tvGPS1.setText("经度:" + gpsInfo.getLongitude());
                tvGPS2.setText("纬度:" + gpsInfo.getLatitude());
                tvGPS3.setText("GGA状态:" + gpsInfo.getGgaType());
            });
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
                Executors.newCachedThreadPool().execute(() -> {
                    gpsObjs.clear();
                    judgeListSize();
                    if (gpsObjs.size() > 0) {
                        HighGpsObj averageHighGpsObj = gpsObjs.get(0);
                        if (averageHighGpsObj == null) {
                            showPointFail();
                            return;
                        }
                        String[] gdGps = GPSUtil.gps_To_GD(averageHighGpsObj.getLongitude(), averageHighGpsObj.getLatitude());
                        double[] bdGps = GPSUtil.gps84_To_bd09(averageHighGpsObj.getLatitude(), averageHighGpsObj.getLongitude());
                        String msg = TimeUtil.date2Str(new Date(), TimeUtil.DEFAULT_TIME_FORMAT)
                                + "," + roadTxt
                                + "," + pointTxt + "-" + tagTxt
                                + "," + averageHighGpsObj.getLongitude()
                                + "," + averageHighGpsObj.getLatitude()
                                + "," + gdGps[0]
                                + "," + gdGps[1]
                                + "," + bdGps[1]
                                + "," + bdGps[0]
                                + "," + averageHighGpsObj.getGgaType();
                        FileUtil.createOrExistsFile(Constants.POINT_PATH);
                        FileUtil.writeFileFromLineString(new File(Constants.POINT_PATH), msg, true);
                        showPointSuccess();
                    } else {
                        showPointFail();
                    }
                });
                break;
            case R.id.clear:
                FileUtil.deleteFile(Constants.POINT_PATH);
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
     * 循环获取3个点的坐标，取平均值算精准
     **/
    private void judgeListSize() {
        long startCollectMillTime = System.currentTimeMillis();
        while (gpsObjs.size() <= 0) {
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


    @Override
    protected void initView() {
        etRoad = findViewById(R.id.road_num_et);
        etBerth = findViewById(R.id.editText);
        etTag = findViewById(R.id.editText1);
        Button btnAdd = findViewById(R.id.add);
        btnAdd.setOnClickListener(this);
        Button btnClear = findViewById(R.id.clear);
        btnClear.setOnClickListener(this);
        tvGPS1 = findViewById(R.id.gps1);
        tvGPS2 = findViewById(R.id.gps2);
        tvGPS3 = findViewById(R.id.gps3);
        progressDialog = new ProgressDialog(PointMainActivity.this);
        progressDialog.setTitle("正在打点");
    }

    protected void timeCleanGpsList() {
        Executors.newCachedThreadPool().execute(() -> {
            while (!onDestroy) {
                if (gpsObjs.size() > 100) {//10Hz相当于10秒的数据
                    gpsObjs.clear();
                }
                try {
                    Thread.sleep(Constants.ONE_MILL_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}