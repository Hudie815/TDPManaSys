package com.portrait.config;

import com.portrait.interceptor.ApiVersionInterceptor;
import com.portrait.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import javax.annotation.Resource;

/**
 * Web MVC 配置
 * 
 * 配置内容：
 * 1. 跨域访问（CORS）
 * 2. 静态资源映射（上传文件访问）
 * 3. API 版本控制拦截器
 * 4. JWT 认证拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.path:./uploads}")
    private String uploadPath;
    
    @Resource
    private JwtInterceptor jwtInterceptor;
    
    @Resource
    private ApiVersionInterceptor apiVersionInterceptor;
    
    /**
     * 跨域配置
     * 
     * 允许的来源：
     * - 桌面端：http://localhost:3000, http://localhost:5173
     * - 移动端：H5 页面域名、App WebView
     * 
     * 生产环境应配置具体域名，不建议使用 *
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // 允许的来源（开发环境）
            .allowedOriginPatterns("*")
            // 允许的请求方法
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            // 允许的请求头
            .allowedHeaders("*")
            // 允许携带认证信息（cookies）
            .allowCredentials(true)
            // 预检请求缓存时间（秒）
            .maxAge(3600)
            // 暴露的响应头
            .exposedHeaders("X-API-Version", "X-Total-Count", "X-Page-Size");
        
        // 移动端 API 专用跨域配置
        registry.addMapping("/api/mobile/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type", "X-API-Version", "X-Client-Type")
            .allowCredentials(true)
            .maxAge(7200);
    }
    
    /**
     * 静态资源映射
     * 
     * 映射上传的文件目录，支持直接访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件映射
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath + "/");
        
        // 移动端静态资源（H5 页面）
        registry.addResourceHandler("/mobile/**")
            .addResourceLocations("classpath:/static/mobile/");
        
        // 文档资源
        registry.addResourceHandler("/docs/**")
            .addResourceLocations("classpath:/static/docs/");
    }
    
    /**
     * 拦截器配置
     * 
     * 1. JWT 认证拦截器：验证登录状态
     * 2. API 版本拦截器：处理版本控制
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // API 版本控制拦截器（优先级最高）
        registry.addInterceptor(apiVersionInterceptor)
            .addPathPatterns("/api/**")
            .order(1);
        
        // JWT 认证拦截器
        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")
            // 排除登录和公开接口
            .excludePathPatterns(
                "/api/auth/login",
                "/api/auth/logout",
                "/api/mobile/dashboard", // 移动端公开接口
                "/api/mobile/search",
                "/uploads/**",
                "/mobile/**"
            )
            .order(2);
    }
    
    /**
     * 视图控制器配置
     * 
     * 用于简单的 URL 映射（无需 Controller 处理）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 移动端首页
        registry.addViewController("/mobile").setViewName("forward:/mobile/index.html");
        
        // API 健康检查
        registry.addViewController("/api/health").setViewName("forward:/health");
    }
}