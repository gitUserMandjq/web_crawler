package com.crawler.base.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HostUtils {
    public static final int MAX_PORT = 65535;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < MAX_PORT; i++) {
            if(isUsed(i)){
                System.out.println("端口"+i+"被使用");
            }
        }
//        int inputPort = 8400;
//        System.out.println("输入端口：" + inputPort + ", 递增递归找到可用端口为：" + getUsablePort(inputPort));
    }

    /**
     * 根据输入端口号，递增递归查询可使用端口
     * @param port  端口号
     * @return  如果被占用，递归；否则返回可使用port
     */
    public static int getUsablePort(int port) throws IOException {


        if (isUsed(port)) {
            //端口被占用，port + 1递归
            port = port + 1;
            return getUsablePort(port);
        } else {
            //可用端口
            return port;
        }
    }
    public static boolean isUsed(int port)  {
        boolean flag = false;
        Socket socket = null;
        try {
            try{
                InetAddress theAddress = InetAddress.getByName("127.0.0.1");
                socket = new Socket(theAddress, port);
                flag = true;
            } catch (IOException e) {
                //如果测试端口号没有被占用，那么会抛出异常，通过下文flag来返回可用端口
            } finally {
                if(socket!=null) {
                    //new了socket最好释放
                    socket.close();
                }
            }
        }catch(Exception e){

        }
        return flag;
    }
}
