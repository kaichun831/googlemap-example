package www.bizpro.com.tw.app.gmap.webapi;

public class ApiTool {
    /*

     */
/**
 * 取得API路徑的最後的名稱
 *
 * @param response
 * @return
 *//*

    public static String getApiName(Response response) {
        int pathSegmentsLength = response.raw().request().url().pathSegments().size();
        if (pathSegmentsLength > 0) {
            String apiName = response.raw().request().url().pathSegments().get(pathSegmentsLength - 1);
            return apiName;
        }
        return null;
    }

    */
/**
 * 取得API路徑的功能+名稱
 *
 * @param response
 * @return
 *//*

    public static String getApiPath(Response response) {
        int pathSegmentsLength = response.raw().request().url().pathSegments().size();
        if (pathSegmentsLength > 0) {
            String progressName = response.raw().request().url().pathSegments().get(pathSegmentsLength - 2);
            String apiName = response.raw().request().url().pathSegments().get(pathSegmentsLength - 1);
            return progressName + "/" + apiName;
        }

        return null;
    }

    */
/**
 * 取得Request
 *
 * @param response
 * @return String
 *//*

    public static String requestBodyParser(Response response) {
        Buffer buffer = new Buffer();
        String str = "";
        try {
            RequestBody body = response.raw().request().body();
            body.writeTo(buffer);
            str = "\n" + buffer.readUtf8() + "\n";
            str = str.replace("=", ":");
            str = str.replace("&", "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
    //僅解析錯誤內容response
    public static ApiMessage getErrorApiMessage(Response response) {
        ApiMessage apiMessage = new ApiMessage(response.code(), response.message());
        String errorBodyStr;
        try {
            errorBodyStr = response.errorBody().string();
            apiMessage.setCode(response.code());
            apiMessage.setRequestApiName(getApiPath(response));
            apiMessage.setHeader(response.raw().request().headers().toString());
            apiMessage.setRequestBody(requestBodyParser(response));
            apiMessage.setErrorBody(errorBodyStr);
            apiMessage.setMessage(new JSONObject(errorBodyStr).getString("Msg"));
            String  token = new JSONObject(errorBodyStr).getString("NewToken");
            if(!token.equals("") && token!=null) Constants.TOKEN = token;

        } catch (IOException e) {
            apiMessage.setMessage("取得Response失敗");
            e.printStackTrace();
        } catch (JSONException e) {
            apiMessage.setMessage("無法獲取資料MSG");
            Logger.errorLog("回傳Response格式不符" + apiMessage.getErrorBody());
            return apiMessage;
        }
        return apiMessage;
    }
*/

}
