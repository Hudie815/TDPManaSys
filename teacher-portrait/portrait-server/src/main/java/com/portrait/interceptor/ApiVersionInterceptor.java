package com.portrait.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API 版本控制拦截器
 * 
 * 支持两种版本控制方式：
 * 1. Header 方式：X-API-Version: v1/v2
 * 2. Query 方式：?version=v1/v2
 * 
 * 版本信息会写入请求属性，供 Controller 使用
 */
@Slf4j
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {
    
    /**
     * API 版本 Header 名称
     */
    public static final String HEADER_API_VERSION = "X-API-Version";
    
    /**
     * 客户端类型 Header 名称
     */
    public static final String HEADER_CLIENT_TYPE = "X-Client-Type";
    
    /**
     * 请求属性中的版本键
     */
    public static final String ATTR_API_VERSION = "apiVersion";
    
    /**
     * 请求属性中的客户端类型键
     */
    public static final String ATTR_CLIENT_TYPE = "clientType";
    
    /**
     * 默认版本
     */
    private static final String DEFAULT_VERSION = "v1";
    
    /**
     * 默认客户端类型
     */
    private static final String DEFAULT_CLIENT_TYPE = "desktop";
    
    /**
     * 支持的版本列表
     */
    private static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取 API 版本
        String version = getApiVersion(request);
        
        // 验证版本是否支持
        if (!isVersionSupported(version)) {
            log.warn("不支持的 API 版本: {}, 请求路径: {}", version, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader("X-Error-Message", "Unsupported API version: " + version);
            return false;
        }
        
        // 获取客户端类型
        String clientType = getClientType(request);
        
        // 写入请求属性
        request.setAttribute(ATTR_API_VERSION, version);
        request.setAttribute(ATTR_CLIENT_TYPE, clientType);
        
        // 设置响应头，方便客户端确认版本
        response.setHeader(HEADER_API_VERSION, version);
        response.setHeader(HEADER_CLIENT_TYPE, clientType);
        
        log.debug("API 版本拦截器: version={}, clientType={}, path={}", 
            version, clientType, request.getRequestURI());
        
        return true;
    }
    
    /**
     * 获取 API 版本
     * 
     * 优先级：Header > Query > Default
     */
    private String getApiVersion(HttpServletRequest request) {
        // 优先从 Header 获取
        String version = request.getHeader(HEADER_API_VERSION);
        
        // 其次从 Query 参数获取
        if (version == null || version.isEmpty()) {
            version = request.getParameter("version");
        }
        
        // 最后使用默认值
        if (version == null || version.isEmpty()) {
            version = DEFAULT_VERSION;
        }
        
        return version.toLowerCase();
    }
    
    /**
     * 获取客户端类型
     */
    private String getClientType(HttpServletRequest request) {
        String clientType = request.getHeader(HEADER_CLIENT_TYPE);
        
        if (clientType == null || clientType.isEmpty()) {
            clientType = request.getParameter("client");
        }
        
        if (clientType == null || clientType.isEmpty()) {
            // 通过 User-Agent 判断
            clientType = detectClientType(request.getHeader("User-Agent"));
        }
        
        if (clientType == null || clientType.isEmpty()) {
            clientType = DEFAULT_CLIENT_TYPE;
        }
        
        return clientType.toLowerCase();
    }
    
    /**
     * 通过 User-Agent 判断客户端类型
     */
    private String detectClientType(String userAgent) {
        if (userAgent == null) {
            return DEFAULT_CLIENT_TYPE;
        }
        
        userAgent = userAgent.toLowerCase();
        
        // 移动端检测
        if (userAgent.contains("mobile") || 
            userAgent.contains("android") || 
            userAgent.contains("iphone") ||
            userAgent.contains("ipad")) {
            return "mobile";
        }
        
        // 平板检测
        if (userAgent.contains("tablet")) {
            return "tablet";
        }
        
        return "desktop";
    }
    
    /**
     * 验证版本是否支持
     */
    private boolean isVersionSupported(String version) {
        for (String supported : SUPPORTED_VERSIONS) {
            if (supported.equals(version)) {
                return true;
            }
        }
        return false;
    }
}