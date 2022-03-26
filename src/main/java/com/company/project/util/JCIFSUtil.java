package com.company.project.util;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 读写 windows 共享目录工具类
 */
public class JCIFSUtil {

    // 用户名:share 密码：admin
    public static  String remotePhotoUrl = "smb://kaisikaipu:chenkskp@192.168.2.250/test/202203/";

    /**
     * 往远程共享目录写文件
     */
    public static void writeRemoteFile(){
        InputStream in = null;
        OutputStream out = null;
        try {
            //获取图片
            File localFile = new File("D:/test.txt");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS_");
            //SmbFile remoteFile = new SmbFile(remotePhotoUrl + "/" + fmt.format(new Date()) + localFile.getName());
            System.out.println("localFile name =" + localFile.getName());
            SmbFile remoteFile = new SmbFile(remotePhotoUrl + "20220320.txt");
            remoteFile.connect(); //尝试连接

            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));

            byte[] buffer = new byte[4096];
            int len = 0; //读取长度
            while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush(); //刷新缓冲的输出流
        }
        catch (Exception e) {
            e.printStackTrace();
            String msg = "发生错误：" + e.getMessage();
            System.out.println(msg);
        }
        finally {
            try {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
            }
            catch (Exception e) {}
        }
    }

    /**
     * 读取远程共享文件
     * @return
     */
    public static byte[] readRemoteFile(){
        InputStream in = null ;
        ByteArrayOutputStream out = null ;
        try {
            //创建远程文件对象
            String url = remotePhotoUrl + "test.txt";
            SmbFile remoteFile = new SmbFile(url);
            remoteFile.connect(); //尝试连接
            //创建文件流
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new ByteArrayOutputStream((int)remoteFile.length());
            //读取文件内容
            byte[] buffer = new byte[4096];
            int len = 0; //读取长度
            while ((len = in.read(buffer, 0, buffer.length)) != - 1) {
                out.write(buffer, 0, len);
            }
            out.flush(); //刷新缓冲的输出流
            return out.toByteArray();
        } catch (Exception e) {
            String msg = "下载远程文件出错：" + e.getLocalizedMessage();
            System.out.println(msg);
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
            } catch (Exception e) {}
        }
        return  null;
    }


    public static void smbPut(String shareDirectory, String localFilePath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File localFile = new File(localFilePath);

            String fileName = localFile.getName();
            SmbFile remoteFile = new SmbFile(shareDirectory + "/" + fileName);
            remoteFile.connect();
            in = new BufferedInputStream(new FileInputStream(localFile));
            out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        smbPut(remotePhotoUrl,"D:\\20220320.txt");
    }
}
