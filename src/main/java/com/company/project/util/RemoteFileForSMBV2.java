package com.company.project.util;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;

import java.io.IOException;

public class RemoteFileForSMBV2 {



    //@Bean(name = "Smb2Session")
    public static Session getSmb2Session() {
//        String hostName = "192.168.1.106";
/**
 * ip
 **/
         String hostName = "192.168.2.250";
        /**
         * 目标用户名（如需密码则填写上即可此处可以@Value写到配置文件中）
         **/
         String username = "";
        /**
         * 密码
         **/
         String password = "";
        Session s = null;
        try {
            SMBClient client = new SMBClient(SmbConfig.createDefaultConfig());
            Connection c = client.connect(hostName);
            System.out.println("是否链接：" + c.isConnected());
            s = c.authenticate(new AuthenticationContext(username, password.toCharArray(), ""));
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
