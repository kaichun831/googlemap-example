package www.bizpro.com.tw.app.gmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;
import www.bizpro.com.tw.app.gmap.databinding.ActivityMapsBinding;
import www.bizpro.com.tw.app.gmap.webapi.ApiManager;
import www.bizpro.com.tw.app.gmap.webapi.ApiService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    ActivityMapsBinding binding;

    private GoogleMap mMap;
    public static double latitude = 0;
    public static double longitude = 0;
    static public LocationManager locationManager;
    // 最短距離(公尺單位)
    static private final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 10;
    // 最小時間(微秒單位)
    public static long MINIMUM_TIME_BETWEEN_UPDATE = 1000000;
    public static Location myLocation;
    static LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            myLocation = location;
            // 繪製標記
            //LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        public void onStatusChanged(String s, int i, Bundle b) {
        }

        public void onProviderDisabled(String s) {
        }

        public void onProviderEnabled(String s) {
        }
    };
    private ActivityResultLauncher gpsLaunch = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {

    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        binding.btn.setOnClickListener(this);
        binding.camera.setOnClickListener(this);
        getLocation(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Taichung"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
                service.doQueryPath("台中市", "台北市", "AIzaSyCn_mqOYGGjZZt2Cm7Ma5itLVl7LGPOnBw")
                        .timeout(ApiManager.TIMEOUT, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.single())
                        .subscribe(
                                response -> {
                                    if (response.isSuccessful()) {
                                        PolylineOptions lineOptions = new PolylineOptions();
                                        List<LatLng> list = PolyUtil.decode(response.body().getRoutes().get(0).getOverview_polyline().getPoints());
                                        lineOptions.addAll(list);
                                        lineOptions.width(12);
                                        lineOptions.color(Color.RED);
                                        lineOptions.geodesic(true);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMap.addPolyline(lineOptions);
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
                Log.d("KAI", latitude + "");
                Log.d("KAI", longitude + "");
                LatLng latLng = new LatLng(latitude, longitude);
                moveCamera(latLng);
                break;
        }
    }

    synchronized static public void getLocation(Activity activity) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//設置為最大精度
        criteria.setAltitudeRequired(false);//不要求海拔資訊
        criteria.setBearingRequired(false);//不要求方位資訊
        criteria.setCostAllowed(true);//是否允許付費
        criteria.setPowerRequirement(Criteria.POWER_LOW);//對電量的要求

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATE,            //每一秒、十公尺偵測一次
                MINIMUM_DISTANCECHANGE_FOR_UPDATE, locationListener);

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_BETWEEN_UPDATE,            //每一秒、十公尺偵測一次
                MINIMUM_DISTANCECHANGE_FOR_UPDATE, locationListener);
        String provider = locationManager.getBestProvider(criteria, true);
        myLocation = locationManager.getLastKnownLocation(provider);
    }
}