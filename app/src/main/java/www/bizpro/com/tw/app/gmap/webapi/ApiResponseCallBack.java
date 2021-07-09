package www.bizpro.com.tw.app.gmap.webapi;

import retrofit2.Response;


/**
 * 呼叫的API都須實作
 **/
public interface ApiResponseCallBack<T> {
    void setCallBackResponse(Response<T> response);
    void setErrorException(Throwable t);
}
