package com.crawler.test;


import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class SSHTest {
    public static void main(String[] args) throws JSchException, FileNotFoundException {
        String username = "ubuntu";
        String password = "opside123";
        String host = "3.145.123.71";
//        String host = "52.90.238.5";
//        String username = "ec2-user";
//        String password = "";
//        String host = "3.84.25.163";
//        String host="44.201.55.175";
//        String username="root";
//        String password="ens_search2022";
        int port = 22;
        String pemPath = "C:\\Users\\PC\\Downloads\\flagdai1.pem";
        Connection connection = null;
        try {
            connection = new Connection(host);
            connection.connect();
//            boolean isAuthenticated = connection.authenticateWithPublicKey(username, new File(pemPath), password);
            boolean isAuthenticated = connection.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                throw new IOException("Authentication failed.");
            }
            log.info("sss");
            Session session = connection.openSession();
            session.execCommand("cd / && ls");
            writeInfo(session);
            writeErrorInfo(session);
            session.execCommand("ls");
            System.out.println("ExitCode: " + session.getExitStatus());
            System.out.println("signal: " + session.getExitSignal());
            session.close();
            Session session2 = connection.openSession();
            session2.execCommand("ls");
            writeInfo(session2);
            writeErrorInfo(session2);
            System.out.println("ExitCode: " + session2.getExitStatus());
            System.out.println("signal: " + session2.getExitSignal());
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            connection.close();
        }
    }
    // 取值
    public static void writeInfo(Session sess) throws Exception{
        log.info("输出所有信息");
        // StreamGobbler已经实现了缓冲区，不需要额外的缓存区封装，如：BufferedOutputStream
        // 读取正常内容后再读错误信息
        InputStream stdout = new StreamGobbler(sess.getStdout());
        BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
        // 打印输出结果
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    public static void writeErrorInfo(Session sess)  throws Exception{
        log.info("输出错误信息");
        // 只有在执行结果出现错误时，session.getStderr()才会返回inputstream
        // 如果有错误流，读取错误流，需要注意的是输入流、输出流、错误流共享缓冲区，所以别不管错误流内容
        // 不读错误流，一旦缓冲区被用尽，命令就会被阻塞，不能执行了。当然了缓冲区大小：30kb，一般也不是那么容易占满
        InputStream stderr = new StreamGobbler(sess.getStderr());
        BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
        // 打印输出结果
        // 打印输出结果
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

}
