package www.bizpro.com.tw.app.gmap.webapi;

import io.reactivex.rxjava3.core.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import www.bizpro.com.tw.app.gmap.response.GoogleMapAddressTranslateResponse;
import www.bizpro.com.tw.app.gmap.response.GoogleMapPathResponse;

/**
 * 路徑集合
 */
public interface ApiService {
    @GET("/maps/api/directions/json?")
    Single<Response<GoogleMapPathResponse>> doQueryPath(@Query("origin") String  origin , @Query("destination") String destination, @Query("key") String apiKey , @Query("language") String language);
    @GET("/maps/api/geocode/json?")
    Single<Response<GoogleMapAddressTranslateResponse>> doQueryAddress (@Query("address") String address,@Query("key") String apiKey ,@Query("language") String language);

}
