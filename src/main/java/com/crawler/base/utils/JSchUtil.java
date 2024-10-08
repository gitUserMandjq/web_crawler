package com.crawler.base.utils;

import com.crawler.base.common.model.MyFunction;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.apache.poi.ss.formula.functions.T;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.contains;

@Slf4j
public class JSchUtil  implements SSHUtil {
    private String ipAddress;   //主机ip
    private String username;   // 账号
    private String password;   // 密码
    private int port;  // 端口号
    private String proxy;

    Session session;

    public JSchUtil(String ipAddress, String username, String password, int port) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
    }
    public JSchUtil(String ipAddress, String username, String password, int port, String proxy) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
        this.proxy = proxy;
    }

    /**
     *  连接到指定的ip
     */
    public void connect() {
        try {
            JSch jsch = new JSch();
            if (port < 0 || port > 65535){
                //连接服务器，如果端口号错误，采用默认端口
                session = jsch.getSession(username, ipAddress);
            }else {
                session = jsch.getSession(username, ipAddress, port);
            }
            //设置登录主机的密码
            session.setPassword(password);
            //如果服务器连接不上，则抛出异常
            if (session == null) {
                throw new Exception("session is null");
            }
            if(!StringUtils.isEmpty(proxy)){
                String[] split = proxy.split(":");
                session.setProxy(new ProxyHTTP(split[0], Integer.valueOf(split[1])));
            }
            //设置首次登录跳过主机检查
            session.setConfig("StrictHostKeyChecking", "no");
            //设置登录超时时间
            session.connect(60000);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

    }
    public List<String> remoteExecute(String command) throws JSchException {
        log.info(">> {}", command);
        List<String> resultLines = new ArrayList<>();
        ChannelExec channel = null;
        try{
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream input = channel.getInputStream();
            channel.connect();
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
                while(true) {
//if(jschChannel.isClosed)
                    if (channel.getExitStatus() == 0) {
                        System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
                String inputLine = null;
                while((inputLine = inputReader.readLine()) != null) {
                    log.info("输出命令：{}", inputLine);
                    resultLines.add(inputLine);
                }
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception e) {
                        log.error("JSch inputStream close error:", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOcxecption:", e);
        } finally {
            if (channel != null) {
                try {
                    channel.disconnect();
                } catch (Exception e) {
                    log.error("JSch channel disconnect error:", e);
                }
            }
        }
        return resultLines;
    }
    /**
     * 执行相关的命令（交互式）
     * @param command
     * @return
     */
    public List<String> execute(String command, String endCommand) throws IOException, JSchException {
        log.info("execute-start");
        List<String> stdout  = new ArrayList<>();
        ChannelShell channel = null;
        PrintWriter printWriter = null;
        BufferedReader input = null;
        try {
            //建立交互式通道
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();

            //获取输入
            InputStreamReader inputStreamReader = new InputStreamReader(channel.getInputStream());
            input = new BufferedReader(inputStreamReader);

            //输出
            printWriter = new PrintWriter(channel.getOutputStream());
            printWriter.println(command);
            printWriter.println("exit");
            printWriter.println(endCommand);
            printWriter.flush();
            log.info("The remote command is: ");
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
                System.out.println("命令行输出："+line);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw e;
        }finally {
            printWriter.close();
            input.close();
            if (channel != null) {
                //关闭通道
                channel.disconnect();
            }
        }
        log.info("execute-end");
        return stdout;
    }

    public void close(){
        if (session != null) {
            session.disconnect();
        }
    }
    /*
     * 上传文件到SFTP服务器
     * uploadDire     上传到的服务器文件夹
     * uploadFileName  上传后的文件名  lala_new.txt
     * localFileName  D:\lala_upload.txt
     */
    public  void sftpput(String uploadDire,String uploadFileName,String localFileName)  {
        Channel channel = null;
        try {
            //创建sftp通信通道
            channel = (Channel) this.session.openChannel("sftp");
            channel.connect(1000);
            ChannelSftp sftp = (ChannelSftp) channel;


            //进入服务器指定的文件夹
            sftp.cd(uploadDire);

            //列出服务器指定的文件列表
//            Vector v = sftp.ls("/");
//            for(int i=0;i<v.size();i++){
//                System.out.println(v.get(i));
//            }

            //以下代码实现从本地上传一个文件到服务器，如果要实现下载，对换以下流就可以了
            OutputStream outstream = sftp.put(uploadFileName);
            InputStream instream = new FileInputStream(new File(localFileName));

            byte b[] = new byte[1024];
            int n;
            while ((n = instream.read(b)) != -1) {
                outstream.write(b, 0, n);
            }

            outstream.flush();
            outstream.close();
            instream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.disconnect();
            channel.disconnect();
        }
    }
    /*
     * 从SFTP服务器下载文件
     * @param ftpHost SFTP IP地址
     * @param ftpUserName SFTP 用户名
     * @param ftpPassword SFTP用户名密码
     * @param ftpPort SFTP端口
     * @param ftpPath SFTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     */
    public  void downloadSftpFile(String ftpPath, String localPath,
                                  String fileName) throws JSchException {
        String ftpHost = this.ipAddress;
        String ftpUserName = this.username;
        String ftpPassword = this.password;
        int ftpPort = this.port;

        Session session = null;
        Channel channel = null;

        JSch jsch = new JSch();
        session = jsch.getSession(ftpUserName, ftpHost, ftpPort);
        session.setPassword(ftpPassword);
        session.setTimeout(100000);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp chSftp = (ChannelSftp) channel;

        String ftpFilePath = ftpPath + "/" + fileName;
        String localFilePath = localPath + File.separatorChar + fileName;

        try {
            chSftp.get(ftpFilePath, localFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            chSftp.quit();
            channel.disconnect();
            session.disconnect();
        }

    }
    public String execCommandByShell(String command)throws IOException,JSchException{
        String result = "";

//2.尝试解决 远程ssh只能执行一句命令的情况
        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        InputStream inputStream = channelShell.getInputStream();//从远端到达的数据  都能从这个流读取到
        channelShell.setPty(true);
        channelShell.connect(60000);

        OutputStream outputStream = channelShell.getOutputStream();//写入该流的数据  都将发送到远程端
        //使用PrintWriter 就是为了使用println 这个方法
        //好处就是不需要每次手动给字符加\n
        PrintWriter printWriter = new PrintWriter(outputStream);
//        printWriter.println("cd /opt/applog/MSISVCServer");
//        printWriter.println("ls");
//        printWriter.println("exit");//为了结束本次交互
        printWriter.println(command);//为了结束本次交互
//        printWriter.println("exit");//为了结束本次交互
        printWriter.flush();//把缓冲区的数据强行输出

/**
 shell管道本身就是交互模式的。要想停止，有两种方式：
 一、人为的发送一个exit命令，告诉程序本次交互结束
 二、使用字节流中的available方法，来获取数据的总大小，然后循环去读。
 为了避免阻塞
 */
        byte[] tmp = new byte[1024];
        while(true){
            StringBuilder sb = new StringBuilder();
            while(inputStream.available() > 0){
                int i = inputStream.read(tmp, 0, 1024);
                if(i < 0) break;
                String s = new String(tmp, 0, i);
                sb.append(s);
//                if(s.contains(exitMessage)){
//                    printWriter.println("exit");//为了结束本次交互
//                    printWriter.flush();//把缓冲区的数据强行输出
//                }
            }
            String s = sb.toString();
            if(s.indexOf("--More--") >= 0){
                outputStream.write((" ").getBytes());
                outputStream.flush();
            }
            boolean endFlag = s.contains("@") && s.endsWith(":~$ ")
                    || s.contains("root@ip") && s.endsWith("# ");
            System.out.println("控制台输出："+s);
            if(endFlag){
                printWriter.println("exit");//为了结束本次交互
                printWriter.flush();//把缓冲区的数据强行输出
            }
            System.out.println("循环一次");
            if(channelShell.isClosed()){
                System.out.println("exit-status:"+channelShell.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception e){}

        }
        outputStream.close();
        inputStream.close();
        channelShell.disconnect();
        session.disconnect();
        System.out.println("DONE");

        return result;
    }
    public String execCommandByShell(String command, Function<PrintProperty, String> func)throws IOException,JSchException{
        this.connect();
        String result = "";

//2.尝试解决 远程ssh只能执行一句命令的情况
        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        InputStream inputStream = channelShell.getInputStream();//从远端到达的数据  都能从这个流读取到
        channelShell.setPty(true);
        channelShell.connect(60000);
        OutputStream outputStream = channelShell.getOutputStream();//写入该流的数据  都将发送到远程端
        //使用PrintWriter 就是为了使用println 这个方法
        //好处就是不需要每次手动给字符加\n
        PrintWriter printWriter = new PrintWriter(outputStream);
        if(!StringUtils.isEmpty(command)){
            printWriter.println(command);//为了结束本次交互
            printWriter.flush();//把缓冲区的数据强行输出
        }
        PrintProperty pp = new PrintProperty();
        pp.printWriter = printWriter;
/**
 shell管道本身就是交互模式的。要想停止，有两种方式：
 一、人为的发送一个exit命令，告诉程序本次交互结束
 二、使用字节流中的available方法，来获取数据的总大小，然后循环去读。
 为了避免阻塞
 */
        byte[] tmp = new byte[1024];
        while(true){
            StringBuilder sb = new StringBuilder();
            while(inputStream.available() > 0){
                int i = inputStream.read(tmp, 0, 1024);
                if(i < 0) break;
                String s = new String(tmp, 0, i);
                sb.append(s);
            }
            String s = sb.toString();
            pp.console = s;
            if(!s.isEmpty()){
                System.out.println("命令行输出【"+s+"】");
            }
            if(pp.endFlag){//结束
                pp.printWriter.println("exit");
                pp.printWriter.flush();
            }else{
                result += func.apply(pp);
            }
            if(channelShell.isClosed()){
                System.out.println("exit-status:"+channelShell.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception e){}

        }
        outputStream.close();
        inputStream.close();
        channelShell.disconnect();
        session.disconnect();
        System.out.println("DONE");
        this.close();
        return result;
    }
    @Override
    public <T> String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> func)throws IOException,JSchException{
        return execCommandByShellExpect(func, null);
    }
    @Override
    public <T> String execCommandByShellExpect(MyFunction<PrintProperty<T>, String> func, T append)throws IOException,JSchException{
        this.connect();
        String result = "";

        Channel channel = session.openChannel("shell");
        channel.connect();

        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
                .withInputFilters(removeColors(), removeNonPrintable())
                .withExceptionOnFailure()
                .build();
/**
 shell管道本身就是交互模式的。要想停止，有两种方式：
 一、人为的发送一个exit命令，告诉程序本次交互结束
 二、使用字节流中的available方法，来获取数据的总大小，然后循环去读。
 为了避免阻塞
 */
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
            this.close();
        }
        return result;
    }
    public static JSchUtil getInstance(String ipAddr, String userName, String password){
        int port = 22;
        JSchUtil jSchUtil = new JSchUtil(ipAddr, userName, password, port);
        jSchUtil.connect();
        return jSchUtil;
    }
    public static void main(String[] args) throws IOException, JSchException {

        //密码登陆
        String username = "root";
        String host = "159.138.105.113";
        int port = 22;
        String password = "Huawei123@!";
//        JSchUtil sftp = new JSchUtil(host, username, password ,port, "127.0.0.1:7890");
        JSchUtil sftp = new JSchUtil(host, username, password ,port);

        sftp.execCommandByShellExpect(new MyFunction<SSHClientUtil.PrintProperty<T>, String>() {
            @Override
            public String apply(PrintProperty property) throws IOException {
                Expect expect = property.expect;
                expect.sendLine("sudo su");
                expect.sendLine("cd /root");
                expect.sendLine("echo 6|./Quili.sh");
                expect.expect(new SSHClientUtil.MatchProxy(contains("Unclaimed balance")){

                    @Override
                    public void dealInput(String s) {
                        log.info("input:【{}】",s);
                    }
                });
                return "";
            }
        });
    }
}
