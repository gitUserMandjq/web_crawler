package com.crawler.base.common.exception;

import com.crawler.base.common.model.WebApiBaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

@RestControllerAdvice
public class ControllerExceptionHandleAdvice {
    private final static Logger logger = LoggerFactory.getLogger(ControllerExceptionHandleAdvice.class);

    @ExceptionHandler
    public WebApiBaseResult handler(HttpServletRequest req, HttpServletResponse res, Exception e) {
        logger.info("Restful Http请求发生异常...");

        if (res.getStatus() == HttpStatus.BAD_REQUEST.value()) {
            logger.info("修改返回状态值为200");
            res.setStatus(HttpStatus.OK.value());
        }

        if (e instanceof NullPointerException) {
            logger.error("代码00：" + e.getMessage(), e);
            return WebApiBaseResult.fail("发生空指针异常");
        } else if (e instanceof IllegalArgumentException) {
            logger.error("代码01：" + e.getMessage(), e);
            return WebApiBaseResult.fail("请求参数类型不匹配");
        } else if (e instanceof SQLException) {
            logger.error("代码02：" + e.getMessage(), e);
            return WebApiBaseResult.fail("数据库访问异常");
        } else {
            logger.error("代码99：" + e.getMessage(), e);
            return WebApiBaseResult.fail(e.getMessage());
        }
    }
}
