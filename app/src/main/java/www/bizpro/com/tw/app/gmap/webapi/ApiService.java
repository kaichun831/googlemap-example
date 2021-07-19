package www.bizpro.com.tw.app.gmap.webapi;

import io.reactivex.rxjava3.core.Single;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import www.bizpro.com.tw.app.gmap.response.PathResponse;

/**
 * 路徑集合
 */
public interface ApiService {
    @GET("/maps/api/directions/json?")
    Single<Response<PathResponse>> doQueryPath(@Query("origin") String  origin , @Query("destination") String destination, @Query("key") String apiKey ,@Query("language") String language);
}
