package com.portrait.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用自定义配置
 * 
 * 包含上传路径、移动端配置等自定义参数
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    
    /**
     * 上传配置
     */
    private UploadConfig upload = new UploadConfig();
    
    /**
     * 移动端配置
     */
    private MobileConfig mobile = new MobileConfig();
    
    @Data
    public static class UploadConfig {
        /**
         * 上传文件存储路径
         * 默认：./uploads
         */
        private String path = "./uploads";
        
        /**
         * 最大文件大小（字节）
         * 默认：10MB
         */
        private Long maxSize = 10485760L;
    }
    
    @Data
    public static class MobileConfig {
        /**
         * 移动端默认分页大小
         * 默认：10
         */
        private Integer defaultPageSize = 10;
        
        /**
         * 移动端最大分页大小
         * 默认：100
         */
        private Integer maxPageSize = 100;
    }
}