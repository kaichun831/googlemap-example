/**
 * 封裝API錯誤訊息
 */
package www.bizpro.com.tw.app.gmap.webapi;


public class ApiMessage {
    public static int CODE_SUCCESS = 200;
    public int code;
    public String message;
    public String requestApiName;
    private String header;
    private String requestBody;
    private String errorBody;
    private String title;
    public ApiMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public void setRequestApiName(String requestApiName) {
        this.requestApiName = requestApiName;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String request) {
        this.requestBody = request;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
