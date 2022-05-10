package com.company.project.util;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Configuration
public class SmbUtil {

   /* @Value("${smb.remote.host}")
    public String SMB_REMOTE_HOST = ""; // 共享服务器IP
    @Value("${smb.username}")
    //public static final String SMB_USERNAME = "cn1zhayoz"; // SMB 协议用户名
    public String SMB_USERNAME = ""; // SMB 协议用户名
    @Value("${smb.password}")
    //public static final String SMB_PASSWORD = "Zy202203"; // SMB 协议用户密码
    public String SMB_PASSWORD = ""; // SMB 协议用户密码
    @Value("${smb.share.path}")
    public String SMB_SHARE_PATH = ""; // 共享目录*/

    //共享盘的域名
    @Value("${smb.remote.domain ?: cn.eu.sew}")
    private static String SMB_REMOTE_DOMAIN;

    private static SMBClient client = null;

    // 连接配置
    private static SmbConfig config = SmbConfig.builder().withTimeout(120, TimeUnit.SECONDS)
            .withTimeout(120, TimeUnit.SECONDS) // 超时设置读，写和Transact超时（默认为60秒）
            .withSoTimeout(180, TimeUnit.SECONDS) // Socket超时（默认为0秒）
            .build();

    // 获取分享的句柄  如果用户名为空则使用guest账户登录
    private static DiskShare getDiskShare(String host, String username, String password, String path) {
        if (client == null) {
            client = new SMBClient(config); // 创建连接客户端
        }
        DiskShare share = null;
        try {
            Connection connection = client.connect(host);// 执行连接
            AuthenticationContext ac = new AuthenticationContext(username, password.toCharArray(), SMB_REMOTE_DOMAIN);
            Session session = connection.authenticate(ac); // 执行权限认证
            share = (DiskShare) session.connectShare(path); // 连接共享文件夹
            return share;
        } catch (SocketTimeoutException e) {
            // host 配置错误 connect timed out
            System.out.println(e.getMessage());
            System.out.println("执行远程连接是失败，请检查远程地址是否正确，或远程共享是否已开启");
        } catch (SMBApiException e) {
            String errMessage = e.getMessage();

            if (errMessage.contains("Could not connect to")) {
                // Could not connect to
                System.out.println(e.getMessage());
                System.out.println("请检查共享目录是否正确");
            }

            if (errMessage.contains("Authentication failed")) {
                // Authentication failed
                System.out.println(e.getMessage());
                System.out.println("请检查连接用户名或密码是否正确");
            }

            if (errMessage.contains("STATUS_OBJECT_NAME_NOT_FOUND")) {
                // STATUS_OBJECT_NAME_NOT_FOUND
                System.out.println(e.getMessage());
                System.out.println("远程目录" + path + "不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return share;
    }


    public static void listFile(String host, String username, String password, String path, String innerDir) {
        DiskShare share = getDiskShare(host, username, password, path);

        // innerDir 是否存在
        if (!share.folderExists(innerDir)) {
            System.out.println("远程目录" + innerDir + "不存在");
            closeClient(); // 关闭客户端
            return;
        }

        System.out.println("Remote File List:");
        int fileIndex = 0;
        for (FileIdBothDirectoryInformation f : share.list(innerDir, "*")) {
            String fileName = f.getFileName();
            if (!fileName.equals(".") && !fileName.equals("..")) {
                System.out.println(++fileIndex + " : " + f.getFileName());
            }
        }

        closeClient(); // 关闭客户端
    }


    private static void closeClient() {
        if (client != null) {
            client.close();
        }
    }


    public static void makeDir(String host, String username, String password, String path, String innerDir) {
        DiskShare share = getDiskShare(host, username, password, path);
        createRemoteDir(share, innerDir);
        closeClient(); // 关闭客户端
    }


    private static void createRemoteDir(DiskShare share, String dirName) {
        try{
            // 是否已存在
            if (share.folderExists(dirName)) {
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        // 目录不存在，逐层创建
        String[] fileNameArr = dirName.split("/");
        String tempDirName = ""; // 逐层目录

        for (String fileName : fileNameArr) {
            tempDirName += fileName + "/"; // 下一层目录
            try{
                if (share.folderExists(tempDirName)) {
                    continue;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.println("目录: " + tempDirName + " 不存在，即将创建");
            share.mkdir(tempDirName);
        }
    }


    public static void listFile(String host, String username, String password, String path) {
        listFile(host, username, password, path, "");
    }


    public static void removeFile(String host, String username, String password, String path, String fileName) {
        DiskShare share = getDiskShare(host, username, password, path); // 连接共享文件夹
        //TODO fileName 是否存在

        // TODO  是目录还是文件 share.rmdir();
        // TODO System.out.println("删除文件夹成功" + fileName);
        share.rm(fileName);
        System.out.println("删除文件" + fileName + "成功.");

        closeClient(); // 关闭客户端
    }


    /**
     *
     * @param host
     * @param username
     * @param password
     * @param path         远程共享文件夹
     * @param innerDir     需要存储的远程文件夹路径
     * @param localFileName  本地文件的全地址
     */
    public static void uploadFile(String host, String username, String password, String path, String innerDir, String localFileName) throws Exception {
        OutputStream oStream = null;
        try{
            DiskShare share = getDiskShare(host, username, password, path);
            // 如果目录不存在，逐层创建
            createRemoteDir(share, innerDir);

            Set accessMasks = new HashSet<>();
            accessMasks.add(AccessMask.FILE_ADD_FILE);
            Set attributes = new HashSet<>();
            attributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
            Set smb2ShareAccesses = new HashSet<>();
            smb2ShareAccesses.add(SMB2ShareAccess.FILE_SHARE_WRITE);
            Set smb2CreateOptions = new HashSet<>();
            smb2CreateOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);

            // 文件名称
            String fileName = innerDir + java.io.File.separator + localFileName.substring(localFileName.lastIndexOf(java.io.File.separator) + 1);

            System.out.println("上传文件: " + fileName);

            File openFile = share.openFile(fileName, accessMasks, attributes, smb2ShareAccesses, SMB2CreateDisposition.FILE_OVERWRITE_IF, smb2CreateOptions);
            oStream = openFile.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(localFileName));

            byte[] buffer = new byte[1024];
            int len = 0; //Read length
            while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                oStream.write(buffer, 0, len);
            }
            System.out.println("文件上传成功");
        }finally {
            oStream.flush();
            oStream.close();
            closeClient();
        }

    }


    private static String checkDirEnd(String dir) {
        if (!dir.substring(dir.length() - 1).equals("/")) {
            dir += "/";
        }

        return dir;
    }


    private static void createLocalDirIfNotExist(String dirName) {
        java.io.File dir = new java.io.File(dirName);
        if (dir.exists()) {
            return;
        }
        System.out.println("不存在目录" + dir);
        // 不存在，逐层创建
        String[] fileNameArr = dirName.split("/");
        String tempDirName = ""; // 逐层目录

        for (String fileName : fileNameArr) {
            tempDirName += fileName + "/"; // 下一层目录

            java.io.File tempDir = new java.io.File(tempDirName);
            if (tempDir.exists()) {
                continue;
            }

            System.out.println("目录: " + tempDirName + " 不存在，即将创建");
            tempDir.mkdir();//创建文件夹
        }
    }


    public static void downloadFile(String host, String username, String password, String path, String remoteFile, String localDir) {
        DiskShare share = getDiskShare(host, username, password, path);
        // 添加文件夹分隔符
        localDir = checkDirEnd(localDir);
        try {
            // 文件夹是否存在，创建本地目录
            createLocalDirIfNotExist(localDir);
            // TODO 远程文件夹
            String remoteDir = "";
            for (FileIdBothDirectoryInformation f : share.list(remoteDir, remoteFile)) {
                String filePath = f.getFileName();

                String dstPath = localDir + f.getFileName();

                FileOutputStream fos = new FileOutputStream(dstPath);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                if (share.fileExists(filePath)) {
                    System.out.println("正在下载文件: " + f.getFileName());

                    File smbFileRead = share.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
                    InputStream in = smbFileRead.getInputStream();

                    byte[] buffer = new byte[4096];
                    int len = 0;
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, len);
                    }

                    bos.flush();
                    bos.close();

                    System.out.println("文件下载成功, 保存在 " + dstPath);
                } else {
                    System.out.println("文件 " + filePath + " 不存在");
                }
            }
        } catch (Exception e) {
            System.out.println("文件下载失败");
            e.printStackTrace();
        }
    }

    public static String readOneFileString(String host, String username, String password, String path, String remoteDir,String remoteFile) {
        DiskShare share = getDiskShare(host, username, password, path);
        // 添加文件夹分隔符
        try {
            // TODO 远程文件夹
            File smbFileRead = share.openFile(remoteDir+remoteFile, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);
            InputStream in = smbFileRead.getInputStream();
            StringBuilder sb = new StringBuilder();
            for (int ch; (ch = in.read()) != -1; ) {
                sb.append((char) ch);
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("文件下载失败");
            e.printStackTrace();
            return  null;
        }
    }

    public static void main(String[] args) {
        // 测试，展示远程共享目录下的文件里列表
        //SmbUtil.listFile(SMB_REMOTE_HOST, SMB_USERNAME, SMB_PASSWORD, SMB_SHARE_PATH, "E25");

        // 测试，删除远程共享目录的文件(或文件夹)
//        SmbUtil.removeFile(SMB_REMOTE_HOST, SMB_USERNAME, SMB_PASSWORD, SMB_SHARE_PATH, "b.txt");

        // 测试，上传文件到远程目录
        //SmbUtil.uploadFile(SMB_REMOTE_HOST, SMB_USERNAME, SMB_PASSWORD, SMB_SHARE_PATH, "202204/", "d:/1647746409709.xlsx");

        // TODO 上传文件夹, 这个比较复杂，先不做了

        // 测试，从远程目录下载文件
//        SmbUtil.downloadFile(SMB_REMOTE_HOST, SMB_USERNAME, SMB_PASSWORD, SMB_SHARE_PATH, "*.pdf", "c:/test/a/b");

        // 创建远程文件夹
//        SmbUtil.makeDir(SMB_REMOTE_HOST, SMB_USERNAME, SMB_PASSWORD, SMB_SHARE_PATH, "a/b");
    }
}
