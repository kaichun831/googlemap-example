package www.bizpro.com.tw.app.gmap;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;
import www.bizpro.com.tw.app.gmap.databinding.ActivityMapsBinding;
import www.bizpro.com.tw.app.gmap.response.GoogleMapPathResponse;
import www.bizpro.com.tw.app.gmap.service.GpsService;
import www.bizpro.com.tw.app.gmap.webapi.ApiManager;
import www.bizpro.com.tw.app.gmap.webapi.ApiService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    ActivityMapsBinding binding;
    private GoogleMap mMap;
    public static Intent GPS;
    private Timer timer = null;
    private TimerTask task = null;
    private boolean lockTouch = false;
    int nowRouteDes = 0;
    private double oldDiff;
    private Location OLD_LOCATION;
    private ApiService service = new ApiManager().getAPI();
    private Marker positionMark;
    private float rotationBearing;
    private int distance;
    private int duration;
    private List<GoogleMapPathResponse.RoutesBean.LegsBean.StepsBean> stepLocation = new ArrayList<>();
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
        binding.btTranslateAddress.setOnClickListener(this);
        binding.btClear.setOnClickListener(this);
        binding.etAddress.setText("台中市新社區協成里華豐一街30-1號");
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                String targetAddress = binding.etAddress.getText().toString();
                if (Constants.APP_LAT_LNG != null) {
                    service.doQueryPath(Constants.APP_LAT_LNG.latitude + "," + Constants.APP_LAT_LNG.longitude, targetAddress, Constants.API_KEY, Constants.API_LANGUAGE)
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
                                        /*diffValueLat = startLat - endLat;
                                        diffValueLng = startLng - endLng;*/
                                            //Google路線解析畫圖
                                            PolylineOptions lineOptions = new PolylineOptions();
                                            List<LatLng> list = PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints());
                                            lineOptions.addAll(list);
                                            lineOptions.width(12);
                                            lineOptions.color(Color.RED);
                                            lineOptions.geodesic(true);
                                            //距離
                                            distance = response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue();
                                            int km = distance / 1000;
                                            int mm = distance % 1000;
                                            Log.d("Kai", km + "km " + mm + "mm");
                                            //時間
                                            duration = response.body().getRoutes().get(0).getLegs().get(0).getDuration().getValue();
                                            int hour = duration / 60 / 60;
                                            int minute = duration / 60;

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

                }
                break;
            case R.id.camera:
//                moveCamera(Constants.APP_LAT_LNG);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(Constants.APP_LAT_LNG);
                Location targetLocation = new Location("");
                targetLocation.setLatitude(12);
                targetLocation.setLongitude(30);
                float bearing = Constants.APP_LOCATION.bearingTo(targetLocation);
                Log.d("KAI", "位置軸線角度" + bearing + "");
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car1);
                markerOptions.icon(icon);
                Marker marker = mMap.addMarker(markerOptions);
                marker.setRotation(bearing);
                marker.setFlat(true);
                marker.setAnchor(0.5f, 0.5f);
                moveCamera(Constants.APP_LAT_LNG);
                break;
            case R.id.navigator:
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Constants.APP_LAT_LNG, 18));
                if (stepLocation != null && stepLocation.size() != 0) {
                    timer = new Timer();
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DecimalFormat LocationDf = new DecimalFormat("###.####");
                                    binding.lat.setText(LocationDf.format(Constants.APP_LAT_LNG.latitude));
                                    binding.lng.setText(LocationDf.format(Constants.APP_LAT_LNG.longitude));

                               /* double startLat = stepLocation.get(nowRouteDes).getStart_location().getLat();
                                double startLng = stepLocation.get(nowRouteDes).getStart_location().getLng();*/
                                    double endLat = stepLocation.get(nowRouteDes).getEnd_location().getLat();
                                    double endLng = stepLocation.get(nowRouteDes).getEnd_location().getLng();

                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(Constants.APP_LAT_LNG);
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car1));
                                    if (positionMark != null) {
                                        positionMark.remove();
                                    }
                                    positionMark = mMap.addMarker(markerOptions);
                                    positionMark.setAnchor(0.5f, 0.5f);
                                    positionMark.setFlat(true); //不隨著攝影機轉動
                                    positionMark.setRotation(rotationBearing);
                                    Location targetLocation = new Location("");
                                    targetLocation.setLatitude(endLat);
                                    targetLocation.setLongitude(endLng);


                                    //轉折角度
                                    if (OLD_LOCATION == null) {
                                        OLD_LOCATION = Constants.APP_LOCATION;
                                    } else {
//                                        Log.d("KAI", "OLD" + OLD_LOCATION.getLatitude() + OLD_LOCATION.getLongitude());
//                                        Log.d("KAI", "NEW" + Constants.APP_LOCATION.getLatitude() + Constants.APP_LOCATION.getLongitude());
//                                    float bearing  = Constants.APP_LOCATION.bearingTo(targetLocation);
                                        //距離
                                        try {
                                            double d = getDistance(OLD_LOCATION.getLatitude(), OLD_LOCATION.getLongitude(), Constants.APP_LOCATION.getLatitude(), Constants.APP_LOCATION.getLongitude());
                                            int diffMM = Math.round(Float.parseFloat(String.valueOf(d)));
                                            if (d != 0) {
                                                distance = distance - diffMM;
                                                int km = distance / 1000;
                                                int mm = distance % 1000;
                                                Log.d("Kai", km + "km " + mm + "mm");
                                                binding.tvDistance.setText(km + "公里" + mm + "公尺");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (OLD_LOCATION != Constants.APP_LOCATION) {
                                            rotationBearing = OLD_LOCATION.bearingTo(Constants.APP_LOCATION);
                                            positionMark.setRotation(rotationBearing); //設定旋轉角度
                                            OLD_LOCATION = Constants.APP_LOCATION;
                                            Log.d("KAI", "舊的不等於");
                                        }

                                    }
                                    double newDiff = getDistance(Constants.APP_LAT_LNG.latitude, Constants.APP_LAT_LNG.longitude, endLat, endLng);

                                    /**
                                     * 如果現在與下一個節點的距離小於30並且
                                     * 第一次獲取的位置與節點的距離和下一次貨取與節點的距離必須 > 30
                                     *，開始導向下一個節點
                                     * */
                                    Log.d("Diff","最新距離"+newDiff);
                                    Log.d("Diff","上一個距離"+oldDiff);
                                    String path = null;
                                    if (oldDiff == 0) {
                                        oldDiff = newDiff;
                                    } else {
                                        if (newDiff < 5 && oldDiff != newDiff) {
                                            nowRouteDes++;
                                            Log.e("KAI", "變換位置下一個節點");
                                        }
                                        oldDiff=newDiff;

                                    }
                                    path = filterPath(stepLocation.get(nowRouteDes).getHtml_instructions());


                                    DecimalFormat df = new DecimalFormat("###.##");
                                    binding.nextLastM.setText(df.format(newDiff));
//                                    Log.d("KAI", "距離下一個轉折點還有" + newDiff + "公尺");

                                    binding.step.setText(path);
                                    if (lockTouch) {
                                       CameraPosition focus =  new CameraPosition.Builder().target(Constants.APP_LAT_LNG)
                                                .zoom(18f)
                                                .bearing(rotationBearing)
//                                               .tilt(80)
                                                .build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(focus));
                                    }

                                }
                            });
                        }
                    };
                    timer.schedule(task, 2000, 1000);
                } else {
                    Toast.makeText(this, "還未有路線", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.navigatorStop:
                if (timer != null) {
                    timer.cancel();
                }
                break;
            case R.id.bt_translateAddress:
                String address = binding.etAddress.getText().toString();
                service.doQueryAddress(address, Constants.API_KEY, Constants.API_LANGUAGE)
                        .timeout(ApiManager.TIMEOUT, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.single())
                        .subscribe(response -> {
                                    if (response.isSuccessful()) {
                                        double addLat = response.body().getResults().get(0).getGeometry().getLocation().getLat();
                                        double addLng = response.body().getResults().get(0).getGeometry().getLocation().getLng();
                                        LatLng latLng = new LatLng(addLat, addLng);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                moveCamera(latLng);
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "Fail Api", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                , throwable -> {
                                    throwable.getStackTrace();
                                });
                break;
            case R.id.bt_clear:
                nowRouteDes = 0;
                mMap.clear();
                break;
        }
    }

    private String filterPath(String str) {
        String path = str.replace("<", "");
        path = path.replace(">", "");
        path = path.replace("/", "");
        path = path.replace("\\/", "");
        path = path.replace("div", "");
        path = path.replace("em", "");
        path = path.replace("style", "");
        path = path.replace("=", "");
        path = path.replace("b", "");
        path = path.replace("w", "");
        path = path.replace("r", "");
        path = path.replace("-", "");
        path = path.replace("font", "");
        path = path.replace("size", "");
        path = path.replace(":", "");
        path = path.replace("\"", "");
        path = path.replace(".", "");
        path = path.replace("01", "");
        path = path.replace("09", "");
        return path;
    }

    //取得兩點的距離
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];//單位 公尺
    }


}