package com.crawler.test;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
@Slf4j
public class SSHTest {
    public static void main(String[] args) throws JSchException, FileNotFoundException {
        String username = "ubuntu";
        String password = "";
        String host = "3.145.123.71";
        int port = 22;

        // 创建JSch对象
        JSch jSch = new JSch();
//        File file = ResourceUtils.getFile("classpath:flagdai2.pem");
//        jSch.addIdentity(file.getAbsolutePath());
        jSch.addIdentity("C:\\Users\\PC\\Downloads\\flagdai2.pem");
        Session jSchSession = null;

        boolean reulst = false;

        try {

            // 根据主机账号、ip、端口获取一个Session对象
            jSchSession = jSch.getSession(username, host, port);
            // 存放主机密码
//            jSchSession.setPassword(password);

            Properties config = new Properties();

            // 去掉首次连接确认
            config.put("StrictHostKeyChecking", "no");

            jSchSession.setConfig(config);

            // 超时连接时间为3秒
            jSchSession.setTimeout(3000);

            // 进行连接
            jSchSession.connect();

            // 获取连接结果
            reulst = jSchSession.isConnected();
        } catch (JSchException e) {
            log.error(e.getMessage(),e);
        } finally {
            // 关闭jschSesson流
            if (jSchSession != null && jSchSession.isConnected()) {
                jSchSession.disconnect();
            }
        }

        if (reulst) {
            log.error("【SSH连接】连接成功");
        } else {
            log.error("【SSH连接】连接失败");
        }
    }


}
