package com.crawler.base.utils;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class JSchUtil {
    private String ipAddress;   //主机ip
    private String username;   // 账号
    private String password;   // 密码
    private int port;  // 端口号

    Session session;

    public JSchUtil(String ipAddress, String username, String password, int port) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.port = port;
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
            //设置首次登录跳过主机检查
            session.setConfig("StrictHostKeyChecking", "no");
            //设置登录超时时间
            session.connect(3000);
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
        channelShell.connect(3000);

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
            boolean endFlag = s.contains("ubuntu@ip") && s.endsWith(":~$ ")
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
    public static class PrintProperty{
        public String console = "";
        public PrintWriter printWriter;
        public Boolean endFlag = false;
        public String stage = "0";
    }
    public String execCommandByShell(String command, Function<PrintProperty, String> func)throws IOException,JSchException{
        this.connect();
        String result = "";

//2.尝试解决 远程ssh只能执行一句命令的情况
        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        InputStream inputStream = channelShell.getInputStream();//从远端到达的数据  都能从这个流读取到
        channelShell.setPty(true);
        channelShell.connect(3000);

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
    public static JSchUtil getInstance(String ipAddr, String userName, String password){
        int port = 22;
        JSchUtil jSchUtil = new JSchUtil(ipAddr, userName, password, port);
        jSchUtil.connect();
        return jSchUtil;
    }
    public static void main(String[] args) throws IOException, JSchException {
//        String ipAddr = "38.55.97.242";
        String ipAddr = "44.214.26.1";//错误地址
        String userName = "root";
        String password = "ens_search2022";
        int port = 22;
        String privateKey = "f248eb72746b1fb4d4ac437745c3d2b01932340c6605812afe1b10135201c07a";
        JSchUtil jSchUtil = null;
        try {
            jSchUtil = new JSchUtil(ipAddr, userName, password, port);
//            String command =
//                    "sudo su\n" +
//                            "cd /root\n" +
//                            "./.shardeum/shell.sh\n" +
//                            "operator-cli unstake\n" +
//                            privateKey + "";

            String command = "sudo su\n" +
                    "cd /root\n";
            String result = jSchUtil.execCommandByShell(command, new Function<PrintProperty, String>() {
                @Override
                public String apply(PrintProperty pp) {
                    switch (pp.stage){
                        case "0":
                            pp.printWriter.print("./.shardeum/shell.sh\n");
                            pp.printWriter.flush();
                            pp.stage = "1";
                            break;
                        case "1":
                            if(pp.console.contains("Error: No such container: shardeum-dashboard")){
                                pp.printWriter.print("curl -O https://gitlab.com/shardeum/validator/dashboard/-/raw/main/installer.sh && chmod +x installer.sh && ./installer.sh\n");
                                pp.printWriter.flush();
                                pp.stage = "5";
                            }else if(pp.console.contains("Error response from daemon:")){
                                pp.printWriter.print("docker restart shardeum-dashboard\n");
                                pp.stage = "0";
                            }else if(pp.console.contains("~/app$")){
                                pp.printWriter.print("operator-cli unstake\n");
                                pp.printWriter.flush();
                            }else
                            if(pp.console.contains("Please enter your private key:")){
                                pp.printWriter.print(privateKey+"\n");
                                pp.printWriter.flush();
                                pp.stage = "2";
                            }
                            break;
                        case "2":
                            if(pp.console.contains("~/app$")){
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                pp.stage = "4";
                            }
                            break;
                        case "3":
                            if(pp.console.contains("~/app$")){//退出
                                pp.printWriter.print("exit\n");
                                pp.printWriter.flush();
                                pp.stage = "4";
                                return "";
                            }
                            break;
                        case "4":
                            if(pp.console.contains("]#")){
                                pp.printWriter.print("curl -O https://gitlab.com/shardeum/validator/dashboard/-/raw/main/installer.sh && chmod +x installer.sh && ./installer.sh\n");
                                pp.printWriter.flush();
                                pp.stage = "5";
                            }
                            break;
                        case "5":
                            if(pp.console.contains("By running this installer, you agree to allow the Shardeum team to collect this data. (Y/n)?")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("What base directory should the node use (default ~/.shardeum):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you really want to upgrade now (y/N)")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you want to run the web based Dashboard? (Y/n):")){
                                pp.printWriter.print("Y\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Set the password to access the Dashboard:")){
                                pp.printWriter.print("667889\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Do you want to change the password for the Dashboard? (y/N):")){
                                pp.printWriter.print("N\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Enter the port (1025-65536) to access the web based Dashboard (default 8080):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("enter an IPv4 address (default=auto)")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("This allows p2p communication between nodes. Enter the first port (1025-65536) for p2p communication (default 9001):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                            }else if(pp.console.contains("Enter the second port (1025-65536) for p2p communication (default 10001):")){
                                pp.printWriter.print("\n");
                                pp.printWriter.flush();
                                pp.stage = "6";
                            }
                            break;
                        case "6":
                            if(pp.console.contains("]#")){
                                pp.printWriter.print("./.shardeum/shell.sh\n");
                                pp.printWriter.print("operator-cli gui start\n");
                                pp.printWriter.print("exit\n");
                                pp.endFlag = true;
                            }
                            break;
                    }
                    return "";
                }
            });
            System.out.println("result:"+result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
        System.out.println("===========================================");
//        System.out.println("result:"+execute);
//        jSchUtil.remoteExecute(command);
//        jSchUtil.execute(command);
        log.info("end");
//        try {
//            ChannelShell channel = (ChannelShell) jSchUtil.session.openChannel("shell");
//            channel.connect();
//            InputStream input = channel.getInputStream();
//            BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
//            String inputLine = null;
//            while((inputLine = inputReader.readLine()) != null) {
//                System.out.println(inputLine);
//            }
//            OutputStream os = channel.getOutputStream();
//            os.write("ls".getBytes());
//            os.flush();
//            while((inputLine = inputReader.readLine()) != null) {
//                System.out.println(inputLine);
//            }
////            jSchUtil.remoteExecute("ls");
//            jSchUtil.execute("sudo su");
////            jSchUtil.remoteExecute("sudo cd /root");
////            List<String> sList = jSchUtil.remoteExecute("sudo cd /root/testnet-auto-install-v2/opside-chain && bash ./control-panel.sh");
////            boolean flag = false;
////            for(String s:sList){
////                if(s.contains("2. Restart the clients")){
////                    flag = true;
////                    break;
////                }
////            }
////            System.out.println("条件："+flag);
////            jSchUtil.remoteExecute("5");
////            jSchUtil.remoteExecute("1");
////            jSchUtil.remoteExecute("exit");
//        } catch (JSchException e) {
//            e.printStackTrace();
//        } finally {
//
//        }

    }
}
