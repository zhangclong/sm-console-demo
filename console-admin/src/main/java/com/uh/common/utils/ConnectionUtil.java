package com.uh.common.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionUtil {

    private final static int TIME_OUT = 1000; //time out milliseconds



    /**
     * 测试本机端口是否被使用
     * @param port
     * @return
     */
    public static boolean isPortUsing(int port){
        return isPortUsing(null, port);
    }

    /**
     * 测试本地端口socket是否可被正常连接。
     * @param port
     * @return
     */
    public static boolean isPortConnectable(int port) {
        return isPortConnectable(null, port);
    }

    /**
     * 测试本机的port端口是否被使用。
     * @param port
     * @return
     */
    public static int getUsablePort(int port) {
        return getUsablePort(null, port);
    }


    /**
     * 测试指定地址和端口socket是否可被正常连接。
     * @param address
     * @param port
     * @return true 可被正常连接，false 无法连接
     * @throws IOException
     */
    public static boolean isPortConnectable(String address, int port) {
        try(Socket socket = new Socket()) {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(TIME_OUT);// 1 second
            socket.connect(
                    (address == null) ? new InetSocketAddress(port) : new InetSocketAddress(InetAddress.getByName(address), port),
                    TIME_OUT);
            return true;
        }
        catch (UnknownHostException ue) {
            throw new RuntimeException(ue);
        }
        catch (IOException e) {
            return false;
        }
    }

    /***
     * 测试主机Host的port端口是否被使用
     * @param host
     * @param port
     * @throws UnknownHostException
     */
    public static boolean isPortUsing(String host, int port)  {
        boolean flag = false;
        InetAddress hostAddr;
        try {
            hostAddr = (host == null) ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        }  catch (UnknownHostException ue) {
            throw new RuntimeException(ue);
        }

        try(Socket socket = new Socket(hostAddr, port)) {
            flag = true;
        }
        catch (IOException e) { }
        return flag;
    }


    /**
     * 根据输入端口号，递增递归查询可使用端口
     * @param host 主机名
     * @param port  端口号
     * @return  如果被占用，递归；否则返回可使用port
     */
    public static int getUsablePort(String host, int port) {
        boolean flag = false;

        InetAddress theAddress;
        try {
            theAddress = (host == null) ? InetAddress.getLocalHost() : InetAddress.getByName(host);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try(Socket socket = new Socket(theAddress, port)){
            flag = true;
        } catch (IOException e) {
            //如果测试端口号没有被占用，那么会抛出异常，通过下文flag来返回可用端口
        }

        if (flag) { //端口被占用，port + 1递归
            port = port + 1;
            return getUsablePort(host, port);
        } else {
            return port; //可用端口
        }
    }


}
