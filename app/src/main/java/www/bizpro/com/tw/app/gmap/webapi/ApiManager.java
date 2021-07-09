package www.bizpro.com.tw.app.gmap.webapi;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    public static final int TIMEOUT = 30;
    private ApiService apiService;
    private static ApiManager mInstance;
    OkHttpClient client;

    public ApiManager() {
        client = new OkHttpClient().newBuilder()
//                .addInterceptor(new ChuckInterceptor(MyApplication.getInstance()))
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();
        // 設置baseUrl即要連的網站，addConverterFactory用Gson作為資料處理Converter

    }

    public ApiService getAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
                .client(client)
                .build();
        apiService = retrofit.create(ApiService.class);
        return apiService;
    }

    public static ApiManager getInstance() {
        if (mInstance == null) {
            mInstance = new ApiManager();

        }
        return mInstance;
    }

    /**
     * 顯示成功訊息
     *
     * @param message
     * @return
     */
    public static ApiMessage parseSuccessResponse(String message) {
        return new ApiMessage(ApiMessage.CODE_SUCCESS, message);
    }

    /**
     * 解析後回傳錯誤
     *
     * @param code      HttpCode
     * @param errorBody OKHttp errorBody
     **/
    public static ApiMessage parserErrorResponse(int code, ResponseBody errorBody, String apiName) {
        ApiMessage apiMessage = null;
        try {
            String errorMsg;
            errorMsg = new JSONObject(errorBody.string()).getString("Msg");
            apiMessage = new ApiMessage(code, errorMsg);
            return apiMessage;
        } catch (Exception e) {
            apiMessage = ApiManager.parserErrorResponse("Api:" + apiName + " 發生異常請聯絡開發人員");
            return apiMessage;
        }
    }

    /**
     * 帶入請檢查網路環境是否正常訊息(自動生成)
     *
     * @param errorMessage 自訂錯誤訊息
     * @return ApiMessage
     */
    public static ApiMessage parserErrorResponse(String errorMessage) {
        ApiMessage apiMessage = new ApiMessage(100, errorMessage);
        return apiMessage;
    }


}
