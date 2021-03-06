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
        binding.etAddress.setText("???????????????????????????????????????30-1???");
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        GPS = new Intent(MapsActivity.this, GpsService.class);
        startService(GPS);
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                        if(Constants.APP_LOCATION==null) continue;
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onClick(binding.camera);
                    }
                });


            }
        }).start();

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


    private void moveCamera(LatLng latLng,int zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
                                            //????????????
                                            final double startLat = response.body().getRoutes().get(0).getLegs().get(0).getStart_location().getLat();
                                            final double startLng = response.body().getRoutes().get(0).getLegs().get(0).getStart_location().getLng();
                                            //????????????
                                            final double endLat = response.body().getRoutes().get(0).getLegs().get(0).getEnd_location().getLat();
                                            final double endLng = response.body().getRoutes().get(0).getLegs().get(0).getEnd_location().getLng();

                                            //???????????????
                                        /*diffValueLat = startLat - endLat;
                                        diffValueLng = startLng - endLng;*/
                                            //Google??????????????????
                                            PolylineOptions lineOptions = new PolylineOptions();
                                            List<LatLng> list = PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints());
                                            lineOptions.addAll(list);
                                            lineOptions.width(12);
                                            lineOptions.color(Color.RED);
                                            lineOptions.geodesic(true);
                                            //??????
                                            distance = response.body().getRoutes().get(0).getLegs().get(0).getDistance().getValue();
                                            int km = distance / 1000;
                                            int mm = distance % 1000;
                                            Log.d("Kai", km + "km " + mm + "mm");
                                            //??????
                                            duration = response.body().getRoutes().get(0).getLegs().get(0).getDuration().getValue();
                                            int hour = duration / 60 / 60;
                                            int minute = duration / 60;

                                            Log.d("Kai", hour + "hour" + minute + "minute");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mMap.addPolyline(lineOptions);
                                                    binding.tvDistance.setText(km + "??????" + mm + "??????");
                                                    binding.tvDuration.setText(hour + "??????" + minute + "???");
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
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car1);
                markerOptions.icon(icon);
                Marker marker = mMap.addMarker(markerOptions);
                marker.setAnchor(0.5f, 0.5f);
                moveCamera(Constants.APP_LAT_LNG,16);
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
                                    positionMark.setFlat(true); //????????????????????????
                                    positionMark.setRotation(rotationBearing);
                                    Location targetLocation = new Location("");
                                    targetLocation.setLatitude(endLat);
                                    targetLocation.setLongitude(endLng);


                                    //????????????
                                    if (OLD_LOCATION == null) {
                                        OLD_LOCATION = Constants.APP_LOCATION;
                                    } else {
//                                        Log.d("KAI", "OLD" + OLD_LOCATION.getLatitude() + OLD_LOCATION.getLongitude());
//                                        Log.d("KAI", "NEW" + Constants.APP_LOCATION.getLatitude() + Constants.APP_LOCATION.getLongitude());
//                                    float bearing  = Constants.APP_LOCATION.bearingTo(targetLocation);
                                        //??????
                                        try {
                                            double d = getDistance(OLD_LOCATION.getLatitude(), OLD_LOCATION.getLongitude(), Constants.APP_LOCATION.getLatitude(), Constants.APP_LOCATION.getLongitude());
                                            int diffMM = Math.round(Float.parseFloat(String.valueOf(d)));
                                            if (d != 0) {
                                                distance = distance - diffMM;
                                                int km = distance / 1000;
                                                int mm = distance % 1000;
                                                Log.d("Kai", km + "km " + mm + "mm");
                                                binding.tvDistance.setText(km + "??????" + mm + "??????");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (OLD_LOCATION != Constants.APP_LOCATION) {
                                            rotationBearing = OLD_LOCATION.bearingTo(Constants.APP_LOCATION);
                                            positionMark.setRotation(rotationBearing); //??????????????????
                                            OLD_LOCATION = Constants.APP_LOCATION;
                                            Log.d("KAI", "???????????????");
                                        }

                                    }
                                    double newDiff = getDistance(Constants.APP_LAT_LNG.latitude, Constants.APP_LAT_LNG.longitude, endLat, endLng);

                                    /**
                                     * ?????????????????????????????????????????????30??????
                                     * ???????????????????????????????????????????????????????????????????????????????????? > 30
                                     *??????????????????????????????
                                     * */
                                    Log.d("Diff","????????????"+newDiff);
                                    Log.d("Diff","???????????????"+oldDiff);
                                    String path = null;
                                    if (oldDiff == 0) {
                                        oldDiff = newDiff;
                                    } else {
                                        if (newDiff < 5 && oldDiff != newDiff) {
                                            nowRouteDes++;
                                            Log.e("KAI", "???????????????????????????");
                                        }
                                        oldDiff=newDiff;

                                    }
                                    path = filterPath(stepLocation.get(nowRouteDes).getHtml_instructions());


                                    DecimalFormat df = new DecimalFormat("###.##");
                                    binding.nextLastM.setText(df.format(newDiff));
//                                    Log.d("KAI", "??????????????????????????????" + newDiff + "??????");

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
                    Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
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
                                                moveCamera(latLng,16);
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

    //?????????????????????
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];//?????? ??????
    }


}