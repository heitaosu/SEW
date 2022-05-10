package com.company.project.configurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@PropertySource(value = "classpath:application.properties", encoding = "utf-8")
public class ProjectConfig {
    /**
     * 生成临时文件 储存本地 的目录
     */
    @Value("${system.sew.write.file.dir}")
    private String SYSTEM_SEW_WRITE_FILE_DIR;
    /**
     * 生成 excel 文件时记录的文件夹
     * (用于区分多个注油机 另外一台注油机的值与这个不一样)
     */
    @Value("${system.sew.domain.name}")
    private String SYSTEM_SEW_DOMAIN_NAME;

    /**
     *  读文件的 共享文件盘
     */
    @Value("${smb.read.remote.host}")
    private String SMB_READ_REMOTE_HOST;
    @Value("${smb.read.username}")
    private String SMB_READ_USERNAME;
    @Value("${smb.read.password}")
    private String SMB_READ_PASSWORD;
    @Value("${smb.read.share.path}")
    private String SMB_READ_SHARE_PATH;

    /**
     *  写文件的 共享文件盘
     */
    @Value("${smb.write.remote.host}")
    private String SMB_WRITE_REMOTE_HOST;
    @Value("${smb.write.username}")
    private String SMB_WRITE_USERNAME;
    @Value("${smb.write.password}")
    private String SMB_WRITE_PASSWORD;
    @Value("${smb.write.share.path}")
    private String SMB_WRITE_SHARE_PATH;
    @Value("${smb.write.share.basedir}")
    private String SMB_WRITE_SHARE_BASEDIR;

    /**
     * 页面初始化的值
     */
    @Value("${system.init.val.51.num}")
    private String SYSTEM_INIT_VAL_51_NUM;
    @Value("${system.init.val.52.num}")
    private String SYSTEM_INIT_VAL_52_NUM;
    @Value("${system.init.val.53.num}")
    private String SYSTEM_INIT_VAL_53_NUM;
    @Value("${system.init.val.54.num}")
    private String SYSTEM_INIT_VAL_54_NUM;


    public String getSMB_READ_REMOTE_HOST() {
        return SMB_READ_REMOTE_HOST;
    }

    public String getSMB_READ_USERNAME() {
        return SMB_READ_USERNAME;
    }

    public String getSMB_READ_PASSWORD() {
        return SMB_READ_PASSWORD;
    }

    public String getSMB_READ_SHARE_PATH() {
        return SMB_READ_SHARE_PATH;
    }

    public String getSMB_WRITE_REMOTE_HOST() {
        return SMB_WRITE_REMOTE_HOST;
    }

    public String getSMB_WRITE_USERNAME() {
        return SMB_WRITE_USERNAME;
    }

    public String getSMB_WRITE_PASSWORD() {
        return SMB_WRITE_PASSWORD;
    }

    public String getSMB_WRITE_SHARE_PATH() {
        return SMB_WRITE_SHARE_PATH;
    }

    public void setSMB_READ_REMOTE_HOST(String SMB_READ_REMOTE_HOST) {
        this.SMB_READ_REMOTE_HOST = SMB_READ_REMOTE_HOST;
    }

    public void setSMB_READ_USERNAME(String SMB_READ_USERNAME) {
        this.SMB_READ_USERNAME = SMB_READ_USERNAME;
    }

    public void setSMB_READ_PASSWORD(String SMB_READ_PASSWORD) {
        this.SMB_READ_PASSWORD = SMB_READ_PASSWORD;
    }

    public void setSMB_READ_SHARE_PATH(String SMB_READ_SHARE_PATH) {
        this.SMB_READ_SHARE_PATH = SMB_READ_SHARE_PATH;
    }

    public void setSMB_WRITE_REMOTE_HOST(String SMB_WRITE_REMOTE_HOST) {
        this.SMB_WRITE_REMOTE_HOST = SMB_WRITE_REMOTE_HOST;
    }

    public void setSMB_WRITE_USERNAME(String SMB_WRITE_USERNAME) {
        this.SMB_WRITE_USERNAME = SMB_WRITE_USERNAME;
    }

    public void setSMB_WRITE_PASSWORD(String SMB_WRITE_PASSWORD) {
        this.SMB_WRITE_PASSWORD = SMB_WRITE_PASSWORD;
    }

    public void setSMB_WRITE_SHARE_PATH(String SMB_WRITE_SHARE_PATH) {
        this.SMB_WRITE_SHARE_PATH = SMB_WRITE_SHARE_PATH;
    }

    public String getSYSTEM_INIT_VAL_51_NUM() {
        return SYSTEM_INIT_VAL_51_NUM;
    }

    public void setSYSTEM_INIT_VAL_51_NUM(String SYSTEM_INIT_VAL_51_NUM) {
        this.SYSTEM_INIT_VAL_51_NUM = SYSTEM_INIT_VAL_51_NUM;
    }

    public String getSYSTEM_INIT_VAL_52_NUM() {
        return SYSTEM_INIT_VAL_52_NUM;
    }

    public void setSYSTEM_INIT_VAL_52_NUM(String SYSTEM_INIT_VAL_52_NUM) {
        this.SYSTEM_INIT_VAL_52_NUM = SYSTEM_INIT_VAL_52_NUM;
    }

    public String getSYSTEM_INIT_VAL_53_NUM() {
        return SYSTEM_INIT_VAL_53_NUM;
    }

    public void setSYSTEM_INIT_VAL_53_NUM(String SYSTEM_INIT_VAL_53_NUM) {
        this.SYSTEM_INIT_VAL_53_NUM = SYSTEM_INIT_VAL_53_NUM;
    }

    public String getSYSTEM_INIT_VAL_54_NUM() {
        return SYSTEM_INIT_VAL_54_NUM;
    }

    public void setSYSTEM_INIT_VAL_54_NUM(String SYSTEM_INIT_VAL_54_NUM) {
        this.SYSTEM_INIT_VAL_54_NUM = SYSTEM_INIT_VAL_54_NUM;
    }

    public String getSYSTEM_SEW_WRITE_FILE_DIR() {
        return SYSTEM_SEW_WRITE_FILE_DIR;
    }

    public void setSYSTEM_SEW_WRITE_FILE_DIR(String SYSTEM_SEW_WRITE_FILE_DIR) {
        this.SYSTEM_SEW_WRITE_FILE_DIR = SYSTEM_SEW_WRITE_FILE_DIR;
    }

    public String getSYSTEM_SEW_DOMAIN_NAME() {
        return SYSTEM_SEW_DOMAIN_NAME;
    }

    public void setSYSTEM_SEW_DOMAIN_NAME(String SYSTEM_SEW_DOMAIN_NAME) {
        this.SYSTEM_SEW_DOMAIN_NAME = SYSTEM_SEW_DOMAIN_NAME;
    }

    public String getSMB_WRITE_SHARE_BASEDIR() {
        return SMB_WRITE_SHARE_BASEDIR;
    }

    public void setSMB_WRITE_SHARE_BASEDIR(String SMB_WRITE_SHARE_BASEDIR) {
        this.SMB_WRITE_SHARE_BASEDIR = SMB_WRITE_SHARE_BASEDIR;
    }
}
