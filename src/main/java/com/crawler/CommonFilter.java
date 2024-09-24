package com.crawler;

import com.crawler.base.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;


/**
 * 使用注解标注过滤器
 * @WebFilter将一个实现了javax.servlet.Filter接口的类定义为过滤器
 * 属性filterName声明过滤器的名称, 可选
 * 属性urlPatterns指定要过滤 的URL模式, 也可使用属性value来声明.(指定要过滤的URL模式是必选属性)
 *
 * @author   单红宇(365384722)
 * @myblog  http:// blog.csdn.net/catoop/
 * @create    2016年1月6日
  */
@WebFilter(filterName = "commonFilter", urlPatterns = "/*")
public class CommonFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(CommonFilter.class);
	private String language="ch";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        System.out.println("过滤器初始化");
//    	System.out.println(MessageUtils.get("com.kq.highnet2.CommonFilter.1","过滤器初始化"));
        ServletContext servletContext = filterConfig.getServletContext();
    	ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
    }
    @Override
    public void destroy() {
//        System.out.println("过滤器销毁");
//    	System.out.println(MessageUtils.get("com.kq.highnet2.CommonFilter.2","过滤器销毁"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response
            , FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();
        String msg = "uri:"+requestUri+";";
        msg += "ip:"+ IpUtils.getIpAddr(httpRequest)+";";
    	Enumeration paramNames = httpRequest.getParameterNames();
	  	  while (paramNames.hasMoreElements()) {
	  	   String paramName = (String) paramNames.nextElement();
	  	   String[] paramValues = httpRequest.getParameterValues(paramName);
	  	   if (paramValues.length == 1) {
	  	    String paramValue = paramValues[0];
	  	    if (paramValue.length() != 0) {
				try {
					paramValue = URLDecoder.decode(paramValue,"utf-8");
				}catch (Exception e){

				}

	  	    	msg += "参数：" + paramName + "=" + paramValue+";";
	  	    }
	  	   }
	  	  }
	    logger.info(msg);
		//增加返回消息长度，解决abox过滤短消息的问题
		Cookie cookie = new Cookie("toSloveLengthProblem", "1dsfdssfdsfsdfdsfhdsf23123451dsfdsfdsfsdfdadsf123dsfdsfdsfsdfdsfds");
		httpResponse.addCookie(cookie);
		chain.doFilter(httpRequest, httpResponse);
    }


}
