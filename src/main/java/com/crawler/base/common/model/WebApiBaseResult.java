/**
 * 
 */
package com.crawler.base.common.model;


/**
 * @author myself
 *
 */
public class WebApiBaseResult {

	/** 
     * 消息
      */
    private String message = "调用成功";
    /** 
     * statusCode是返回值: * 
     * {"statusCode":"200", "message":"操作成功", "navTabId":"navNewsLi", "forwardUrl":"", "callbackType":"closeCurrent"}

 * {"statusCode":"300", "message":"操作失败"}

 * {"statusCode":"301", "message":"会话超时"}

      */
    private String stackMessage;
    private String statusCode = "200";
    private String navTabId;
    private String rel;
    //refreshNavTab 刷新页面,closeCurrent 关闭当前页面
    private String callbackType;
    private String forwardUrl;
    private String js;
    public final static String SUCCESS = "success"; 
    public final static String FAIL = "fail";
    public static final String COMMONERROR = "300";
    private String traceId;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static WebApiBaseResult fail(String errorMessage) {
        WebApiBaseResult result = new WebApiBaseResult();
        result.setErrorMessage(errorMessage);
        result.setStatusCode(COMMONERROR);
        return result;
    }
    public static WebApiBaseResult success() {
        WebApiBaseResult result = new WebApiBaseResult();
        return result;
    }
    public static WebApiBaseResult success(Object o) {
        WebApiBaseResult result = new WebApiBaseResult();
        result.setData(o);
        return result;
    }


    //新加总线的id
    public String getTraceId() {
		return traceId;
	}




	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	
	
	
	public String getStackMessage() {
		return stackMessage;
	}




	public void setStackMessage(String stackMessage) {
		this.stackMessage = stackMessage;
	}



	/** 
     * 返回对象
     */
    
    private Object data = null;
    
    
    public WebApiBaseResult(String result , String msg, Object data) {
        
        this.message = msg;
        this.statusCode = result;
        this.data = data;
        
    }
    
    

  
    public WebApiBaseResult() {
		this.message = "调用成功";
	}




	public String getMessage() {
        return message;
    }
    public void setMessage(String result) {
        this.message = result;
    }
    public String getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(String msg) {
        this.statusCode = msg;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getNavTabId() {
        return navTabId;
    }
    public void setNavTabId(String navTabId) {
        this.navTabId = navTabId;
    }
    public String getRel() {
        return rel;
    }
    public void setRel(String rel) {
        this.rel = rel;
    }
    public String getCallbackType() {
        return callbackType;
    }
    public void setCallbackType(String callbackType) {
        this.callbackType = callbackType;
    }
    public String getForwardUrl() {
        return forwardUrl;
    }
    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }
    public String getJs() {
        return js;
    }
    public void setJs(String js) {
        this.js = js;
    }

	public void setStatusCode(String string, String message2) {
		
		this.setStatusCode(string);
		this.setMessage(message2);
		
	}
	
}
