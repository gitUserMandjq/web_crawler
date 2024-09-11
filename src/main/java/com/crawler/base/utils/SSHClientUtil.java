package com.crawler.base.utils;

import com.crawler.base.common.model.MyFunction;
import com.crawler.base.common.model.ProxySocketFactory;
import com.jcraft.jsch.ProxyHTTP;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

@Slf4j
public class SSHClientUtil implements SSHUtil {



    //SFTP 登录用户名
    private String username;
    //SFTP 登录密码
    private String password;
    // 私钥
    private String privateKey;
    //SFTP 服务器地址IP地址
    private String host;
    //SFTP 端口
    private int port;
    private String proxy;

//    private SFTPClient sftpClient;
    private SSHClient sshClient;

    /**
     * 构造基于密码认证的sftp对象
     */
    public SSHClientUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    /**
     * 构造基于密码认证的sftp对象
     */
    public SSHClientUtil(String username, String password, String host, int port, String proxy) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.proxy = proxy;
    }

    /**
     * 构造基于秘钥认证的sftp对象
     */
    public SSHClientUtil(String username, String host, int port, String privateKey, String proxy) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
        this.proxy = proxy;
    }
    /**
     * 构造基于秘钥认证的sftp对象
     */
    public SSHClientUtil(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public SSHClientUtil() {

    }

    /**
     * 连接sftp服务器
     */
    public Boolean openConnection() {
        try {
            // 没有特殊要求的忽略config 直接 SSHClient sshClient = new SSHClient();
            MyConfig myConfig = new MyConfig();
            // 设置CiphersFactories
            myConfig.setCiphersFactories("aes256-ctr,aes192-ctr,aes128-ctr,aes256-gcm@openssh.com,aes128-gcm@openssh.com");
            // 设置 MACsFactories
            myConfig.setMacFactories("hmac-sha2-512,hmac-sha2-256,hmac-sha2-512-etm@openssh.com,hmac-sha2-256-etm@openssh.com");
            // 设置 KEXFactories
            myConfig.setKexFactories("ecdh-sha2-nistp521,ecdh-sha2-nistp384,ecdh-sha2-nistp256,curve25519-sha256@libssh.org");
//            else{
//                myConfig.setWaitForServerIdentBeforeSendingClientIdent(true);
//            }
            Config config = myConfig;
            sshClient = new SSHClient(config);
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            if(!com.crawler.base.utils.StringUtils.isEmpty(proxy)){
                String[] split = proxy.split(":");
                sshClient.setSocketFactory(new ProxySocketFactory(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.valueOf(split[1])))));
            }

            // 密钥链接
            if(StringUtils.isNotBlank(privateKey)){
                // getPrivateKeyPath 只是个获取路径的方法 酌情修改
//                String path = getPrivateKeyPath(privateKey);
                KeyProvider keys = sshClient.loadKeys(privateKey, "", null);
                sshClient.authPublickey(username, keys);
            }
            // 密码链接
            if(StringUtils.isNotBlank(password)){
                sshClient.authPassword(username,password);
            }

            // 创建sftp链接


            log.info("建立SSH连接成功--------");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public void closeConnection() {
        if (sshClient != null) {
            try {
                sshClient.disconnect();
                sshClient.close();
                log.info("Connection closed");
            } catch (IOException e) {
                log.error("Error closing SSH connection: {}", e.getMessage());
            }
        }
    }

    // 执行单条命令
    public String executeCommand(String command) throws IOException {
        try (var session = sshClient.startSession()) {
            try (var cmd = session.exec(command)) {
                String output = IOUtils.readFully(cmd.getInputStream()).toString(); // 读取命令输出
                cmd.join(5, TimeUnit.SECONDS); // 等待命令执行完成
                log.info("Executed command: {}. Output: {}", command, output);
                return output;
            }
        }
    }

    // 执行多条命令
    public void executeCommands(List<String> commands) throws IOException {
        for (String command : commands) {
            executeCommand(command);
        }
    }

    // 上传文件并可选验证哈希值
    public boolean uploadFile(String localFilePath, String remoteFilePath, String localFileHash) throws IOException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            sftp.put(new FileSystemFile(localFilePath), remoteFilePath); // 上传文件
            log.info("Uploaded file from {} to {}", localFilePath, remoteFilePath);
            if (localFileHash != null && !localFileHash.isEmpty()) {
                return verifyRemoteFileHash(remoteFilePath, localFileHash); // 验证远程文件哈希值
            }
            return true;
        }
    }

    // 下载文件并验证文件大小
    public boolean downloadFile(String remoteFilePath, String localFilePath) throws IOException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            sftp.get(remoteFilePath, new FileSystemFile(localFilePath)); // 下载文件
            log.info("Downloaded file from {} to {}", remoteFilePath, localFilePath);
            return verifyFileSize(localFilePath, remoteFilePath); // 验证文件大小
        }
    }

    // 验证远程文件哈希值
    public boolean verifyRemoteFileHash(String remoteFilePath, String localFileHash) throws IOException {
        String remoteCommand = String.format("md5sum %s | awk '{ print $1 }'", remoteFilePath);
        String remoteFileHash = executeCommand(remoteCommand).trim(); // 执行远程命令获取哈希值

        boolean result = localFileHash.equals(remoteFileHash); // 比较哈希值
        if (result) {
            log.info("File hash verification successful for file: {}", remoteFilePath);
        } else {
            log.error("File hash mismatch for file: {}. Local hash: {}, Remote hash: {}", remoteFilePath, localFileHash, remoteFileHash);
        }
        return result;
    }

    // 验证文件大小
    private boolean verifyFileSize(String localFilePath, String remoteFilePath) throws IOException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            File localFile = new File(localFilePath);
            long localFileSize = localFile.length(); // 获取本地文件大小

            FileAttributes remoteFile = sftp.stat(remoteFilePath);
            long remoteFileSize = remoteFile.getSize(); // 获取远程文件大小

            boolean result = localFileSize == remoteFileSize; // 比较文件大小
            if (result) {
                log.info("File size verification successful for file: {}", localFilePath);
            } else {
                log.error("File size mismatch for file: {}. Local size: {}, Remote size: {}", localFilePath, localFileSize, remoteFileSize);
            }
            return result;
        }
    }

    // 列出远程目录文件
    public List<RemoteResourceInfo> listFiles(String remoteDirectory) throws IOException {
        try (SFTPClient sftp = sshClient.newSFTPClient()) {
            List<RemoteResourceInfo> files = sftp.ls(remoteDirectory); // 列出远程目录文件
            log.info("Listed files in directory: {}", remoteDirectory);
            return files;
        }
    }


    public String getPrivateKeyPath(String privateKey){
        String path = this.getClass().getClassLoader().getResource("").getPath();
        return path += privateKey;
    }
    @Override
    public  <T>  String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> func) throws Exception {
        return execCommandByShellExpect(func, null);
    }
    @Override
    public  <T>  String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> func, T append) throws Exception {

        boolean isLogin = this.openConnection();
        if(isLogin){
            log.info("IP:"+ this.host +"SSH login success");
        }else{
            log.info("IP:"+ this.host +"SSH login fail");
            return "";
        }
        String result = "";
        Session session = this.sshClient.startSession();
        Session.Shell shell = session.startShell();
        Expect expect = new ExpectBuilder()
                .withOutput(shell.getOutputStream())
                .withInputs(shell.getInputStream(), shell.getErrorStream())
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
//                .withInputFilters(removeColors(), removeNonPrintable())
                .withExceptionOnFailure()
                .build();
        try {
            PrintProperty printProperty = new PrintProperty();
            printProperty.expect = expect;
            printProperty.append = append;
            func.apply(printProperty);
//            expect.expect(contains("[RETURN]"));
        }catch(Exception e){
            log.error(e.getMessage(), e);
        } finally {
            expect.close();
            session.close();
            this.closeConnection();
        }
        return result;
    }
    public static abstract class MatchProxy implements Matcher{
        private Matcher<Result> match;

        public MatchProxy(Matcher<Result> match) {
            this.match = match;
        }

        public abstract void dealInput(String s);

        @Override
        public Result matches(String s, boolean b) {
            Result matches = match.matches(s, b);
            if(matches.isSuccessful()){
                dealInput(s);
            }
            return matches;
        }
        @Override
        public String toString(){
            return match.toString();
        }
    }
    public static void main(String[] args) throws Exception {
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEowIBAAKCAQEAiY/161GBlMjb87OtkBTdV9q1bHbAd+XU241NIgckAB1xHazA\n" +
                "xTJBluOIAfNb2x8Ozb+OKOeqNDUagP5NlrGB59dMD0Oo5DhHLNsRBBdUN7a4q3hl\n" +
                "1ZzrutcJwaxQrYfGAIXZfvswTsfAA1FyhJCWCyto9GF5ue2iX86WOiPr87wmq4zv\n" +
                "3OmF89jvkfhh1ISnH7TQ2KLgStXB/94QYo7QjfGTq01edybXxj3+onrfogS2QSsR\n" +
                "qeiDzEIcTNC0o+sg/3IGQiE6+eAAOn4OZJUxepcd1KWZF4S0Uo1rF7y6oraCv/ur\n" +
                "iA33bHDHMN9H0hyJ45gEsS/a9rjf6iaeITSgCQIDAQABAoIBACf4HjoLap0yZ0Y9\n" +
                "9LYiedWnBIPJVU8BBoHsYnb6oCkwZMd8hF5HpesTnVcDwLLAKWr6t3GHLlsQ+uxz\n" +
                "91NLrDUSx1TAEfiNqZCM7jrEFIJDoxCfYOdaOtwE0x7k1GQ3SP6p5XIRxg192Hqe\n" +
                "VanS4OXJ7Srkj5vIQh+YROoLuekNWxl/5LLJEjCMLSnZm507yCNgUh+KhK9sby3k\n" +
                "7HxGVFedxjNo9rgKPX6nftpu7TM2/UJ65NYlVrX/k0ucj7Bo55NLYmLqgyFXUv5/\n" +
                "pQSA3+MeW5mJieCDgmAKuRcBww14AVFVrtfQzTgYB1CJV92x43dCG04MU7Br3FXO\n" +
                "zWUBVgECgYEAvImhb9BuXTmTp1SkBRpN9DEciiZUap94hSgLwxkLZs5/sUJeQIRS\n" +
                "DgECH6WoWlXZ5nZPdRhoeI0XljOH+YSvd++M3jUFc9GduphT7/PxW/51uiOavOgm\n" +
                "KCVsHP33UgprSI1MEIRXwPRDnhZhaVzgN1qmwbaK38wO0fiDVFP/9+ECgYEAusjo\n" +
                "UyMEKrjyvXh4/aPug2fMqRuy1EnvJn6o466gvVD19dxgC2jGkhmz0yGG/1cHBHkW\n" +
                "74DvUlIJn1uC0XrOLEXEAgcS0tsA9OsTdXloKhMMkYlytYvfP4ZbO71mTh7hcIwg\n" +
                "746Q2f1jq5wi7bdiHHmKmI+wcFxwnJa9yJuPjSkCgYB3PbCNkEyMx4J8HQsTcxyE\n" +
                "aZ88PJu9R5io1rgBr1BHuCiIiooj0cw3sSmPrGz6nTuQB5mfXE2OmOWnCHZiOCnl\n" +
                "9+qnCu+k3ZkdT4QeybEH1rrMfAI8obKoR2rGN1V5XL+Xhk3qUFlT3uj5DfyT77qb\n" +
                "J8k50OKqQlvJLpyY8/t64QKBgQCYKA+xoeXEE7OSCzCslj03CHidDbZI3w2VEzrr\n" +
                "dpb0gZ5LsUEAKlMQMyXdCQPav3808ptvcV4DdlbmXPqdQOfxAsggSrjX0ZXe7hyE\n" +
                "5uim1au4Zvptz8qGCiIJ8UkEvH0zXMN7wmxXV+Y4ptGBSpD+1zfC/dJiRbtqpBVn\n" +
                "9OkkuQKBgERbcMcRtWOGADEZYd826VcTa55D/Navp992mFCMupgj8iKSJPgXhtAt\n" +
                "wp8XhxGweM2KeCHucHr0Ws3ugT6jiu1b/I6Ks9xd6ScAI+vqqHv72TMXqC2PkFTe\n" +
                "DvXhJklioT/+P2RX5fPFpATh2ndNyMAp8YBAvcCK3rAFc8n1QrDC\n" +
                "-----END RSA PRIVATE KEY-----";
        int port = 22;

//        String username = "ubuntu";
//        String host = "52.82.21.176";// 目标地址
        //密码登陆
        String username = "root";
        String host = "159.138.105.113";

        String password = "Huawei123@!";
        SSHClientUtil sftp = new SSHClientUtil(username, password,host,port);

        sftp.execCommandByShellExpect(new MyFunction<SSHClientUtil.PrintProperty<T>, String>() {
            @Override
            public String apply(PrintProperty property) throws IOException {
                Expect expect = property.expect;
                expect.sendLine("sudo su");
                expect.sendLine("cd /root");
                expect.sendLine("echo 6|./Quili.sh");
                expect.expect(new MatchProxy(contains("Unclaimed balance")){

                    @Override
                    public void dealInput(String s) {
                        log.info("input:【{}】",s);
                    }
                });
                return "";
            }
        });
//        {
//            String command = "cd /root\nls";
//            sftp.execCommandByShell("", new Function<JSchUtil.PrintProperty, String>() {
//                @Override
//                public String apply(JSchUtil.PrintProperty pp) {
//                    if(pp.console.contains("System restart required")){
//                        log.info("开始处理");
//                        pp.printWriter.print("echo 1 > /root/d.txt");
////                        pp.printWriter.flush();
//                    }
////                    pp.endFlag = true;
//                    return "";
//                }
//            });
//        }

//        File file = new File("/test/test.txt");
//
//        if(file != null){
//            boolean isUpload = sftp.upload(directory,file.getName(),file);
//            if(isUpload){
//                log.info("fileName:"+file.getName()+" , targetPath:"+ directory +" ; Success !!!");
//            }else{
//                log.info("fileName:"+file.getName()+" , targetPath:"+ directory +" ; Fail !!!");
//            }
//        }
//
//        boolean isExist = sftp.isExistsFile(directory,file.getName());
//        if(isExist){
//            log.info("fileName:"+file.getName()+" , targetPath:"+ directory +" ;  File Exist to SFTP !!!");
//        }else{
//            log.info("fileName:"+file.getName()+" , targetPath:"+ directory +" ; File Not Exist to SFTP !!!");
//        }

    }
}

