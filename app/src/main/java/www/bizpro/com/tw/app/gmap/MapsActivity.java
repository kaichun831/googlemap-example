package www.bizpro.com.tw.app.gmap;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;
import www.bizpro.com.tw.app.gmap.databinding.ActivityMapsBinding;
import www.bizpro.com.tw.app.gmap.response.PathResponse;
import www.bizpro.com.tw.app.gmap.service.GpsService;
import www.bizpro.com.tw.app.gmap.webapi.ApiManager;
import www.bizpro.com.tw.app.gmap.webapi.ApiService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    ActivityMapsBinding binding;
    private GoogleMap mMap;
    public static Intent GPS;
    private Timer timer;
    private boolean lockTouch = false;
    boolean locationDesFlag = true;
    int nowRouteDes = 0;
    private double diffValueLat;  //Lat差異值
    private double diffValueLng;  //Lng差異值

    private List<PathResponse.RoutesBean.LegsBean.StepsBean> stepLocation = new ArrayList<>();
    private ActivityResultLauncher gpsLaunch = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        binding.btn.setOnClickListener(this);
        binding.camera.setOnClickListener(this);
        binding.navigator.setOnClickListener(this);
        binding.navigatorStop.setOnClickListener(this);
        GPS = new Intent(MapsActivity.this, GpsService.class);
        startService(GPS);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                lockTouch = !lockTouch;
            }
        });
    }

    private void moveCamera(LatLng latLng) {
        CameraUpdateFactory.zoomTo(3);
        MarkerOptions mark = new MarkerOptions();
        mark.position(latLng);
        mMap.addMarker(mark);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                ApiService service = new ApiManager().getAPI();
                service.doQueryPath(Constants.APP_LAT_LNG.latitude+","+Constants.APP_LAT_LNG.longitude, "台北市", "AIzaSyCn_mqOYGGjZZt2Cm7Ma5itLVl7LGPOnBw", "zh-TW")
                        .timeout(ApiManager.TIMEOUT, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.single())
                        .subscribe(
                                response -> {
                                    if (response.isSuccessful()) {
                                        stepLocation = response.body().getRoutes().get(0).getLegs().get(0).getSteps();
                                        //起點座標
                                        final double startLat = response.body().getRoutes().get(0).getLegs().get(0).getStart_location().getLat();
                                        final double startLng = response.body().getRoutes().get(0).getLegs().get(0).getStart_location().getLng();
                                        //終點座標
                                        final double endLat = response.body().getRoutes().get(0).getLegs().get(0).getEnd_location().getLat();
                                        final double endLng = response.body().getRoutes().get(0).getLegs().get(0).getEnd_location().getLng();

                                        //計算差異值
                                        diffValueLat = startLat - endLat;
                                        diffValueLng = startLng - endLng;

                                        //Google路線解析畫圖
                                        PolylineOptions lineOptions = new PolylineOptions();
                                        List<LatLng> list = PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints());
                                        lineOptions.addAll(list);
                                        lineOptions.width(12);
                                        lineOptions.color(Color.RED);
                                        lineOptions.geodesic(true);
                                        //距離
                                        int distance = response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue();
                                        int km = distance / 1000;
                                        int mm = distance % 1000;
                                        Log.d("Kai", km + "km " + mm + "mm");
                                        //時間
                                        int duration = response.body().getRoutes().get(0).getLegs().get(0).getDuration().getValue();
                                        int hour = duration / 60 / 60;
                                        int minute = duration % 60;
                                        Log.d("Kai", hour + "hour" + minute + "minute");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.addPolyline(lineOptions);
                                                binding.tvDistance.setText(km + "公里" + mm + "公尺");
                                                binding.tvDuration.setText(hour + "小時" + minute + "分");
                                            }
                                        });
                                    } else {
                                        Log.d("KAI", "fail");
                                    }
                                }
                                , throwable -> {
                                    throwable.getStackTrace();
                                }
                        );
                ;
                break;
            case R.id.camera:
                moveCamera(Constants.APP_LAT_LNG);
                break;
            case R.id.navigator:
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Constants.APP_LAT_LNG, 15));
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MarkerOptions mark = new MarkerOptions();
                                mark.position(Constants.APP_LAT_LNG);
                                binding.lat.setText(String.valueOf(Constants.APP_LAT_LNG.latitude));
                                binding.lng.setText(String.valueOf(Constants.APP_LAT_LNG.longitude));

                                double startLat = stepLocation.get(nowRouteDes).getStart_location().getLat();
                                double startLng = stepLocation.get(nowRouteDes).getStart_location().getLng();

                                double endLat = stepLocation.get(nowRouteDes).getEnd_location().getLat();
                                double endLng = stepLocation.get(nowRouteDes).getEnd_location().getLng();

                                double diffKm = getDistance(Constants.APP_LAT_LNG.latitude, Constants.APP_LAT_LNG.longitude, startLat, startLng);
                                String path;
                                Log.d("KAI","距離下一個轉折點還有"+diffKm+"公尺");
                                if (diffKm < 200) {
                                    nowRouteDes++;
                                    path = filterPath(stepLocation.get(nowRouteDes).getHtml_instructions());
                                } else {
                                    path = filterPath(stepLocation.get(nowRouteDes).getHtml_instructions());
                                }
                                binding.step.setText(path);

                                mMap.addMarker(mark);
                                if (lockTouch) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Constants.APP_LAT_LNG, 15));
                                }

                            }
                        });
                    }
                };
                timer.schedule(task, 3000, 3000);
                break;
            case R.id.navigatorStop:
                if (timer != null) {
                    timer.cancel();
                }
                break;
        }
    }

    private String filterPath(String str) {
        String path = str.replace("<b>", "");
        path = path.replace("</b>", "");
        path = path.replace("/<wbr/>", "");
        return path;
    }

    //取得兩點的距離
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];//單位 公尺
    }
}