package com.crawler.test;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

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
                String inputLine = null;
                while((inputLine = inputReader.readLine()) != null) {
                    log.info("   {}", inputLine);
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
    public List<String> execute(String command) throws IOException, JSchException {
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
//            printWriter.println("exit");
            printWriter.flush();
            log.info("The remote command is: ");
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
                System.out.println(line);
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
    public static void main(String[] args) throws IOException {
        String ipAddr = "18.221.33.188";
        String userName = "ubuntu";
        String password = "opside123";
        int port = 22;
        JSchUtil jSchUtil = new JSchUtil(ipAddr, userName, password, port);
        jSchUtil.connect();
//        jSchUtil.execute("sudo su\n" + "cd /root/testnet-auto-install-v2/opside-chain && bash ./control-panel.sh\n" +
//                "5\n" +
//                "1\n");

        try {
            ChannelShell channel = (ChannelShell) jSchUtil.session.openChannel("shell");
            channel.connect();
            InputStream input = channel.getInputStream();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
            String inputLine = null;
            while((inputLine = inputReader.readLine()) != null) {
                System.out.println(inputLine);
            }
            OutputStream os = channel.getOutputStream();
            os.write("ls".getBytes());
            os.flush();
            while((inputLine = inputReader.readLine()) != null) {
                System.out.println(inputLine);
            }
//            jSchUtil.remoteExecute("ls");
            jSchUtil.execute("sudo su");
//            jSchUtil.remoteExecute("sudo cd /root");
//            List<String> sList = jSchUtil.remoteExecute("sudo cd /root/testnet-auto-install-v2/opside-chain && bash ./control-panel.sh");
//            boolean flag = false;
//            for(String s:sList){
//                if(s.contains("2. Restart the clients")){
//                    flag = true;
//                    break;
//                }
//            }
//            System.out.println("条件："+flag);
//            jSchUtil.remoteExecute("5");
//            jSchUtil.remoteExecute("1");
//            jSchUtil.remoteExecute("exit");
        } catch (JSchException e) {
            e.printStackTrace();
        } finally {

        }

    }
}
