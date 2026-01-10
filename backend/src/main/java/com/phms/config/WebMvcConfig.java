package com.phms.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 *
 * @author PHMS
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 路由认证配置
            SaRouter.match("/**")
                    // 排除不需要登录的接口
                    .notMatch(
                            // 登录相关
                            "/auth/**",
                            // 接口文档
                            "/doc.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            // 健康检查
                            "/actuator/**",
                            // 静态资源
                            "/favicon.ico",
                            "/error",
                            "/test/**",
                            // 文件上传（Controller层已做权限控制）
                            "/upload/**"
                    )
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
}
