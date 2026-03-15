package org.example.demoapp.config;

import org.example.demoapp.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 告诉Spring：这是配置类
public class WebConfig implements WebMvcConfigurer {

    @Autowired  // 自动注入拦截器
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器并设置规则
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")          // 拦截所有路径
                .excludePathPatterns(            // 排除以下路径
                        "/api/auth/login",           // 登录接口
                        "/api/auth/register",        // 注册接口
                        "/error",
                        "/uploads/**"                 // 静态资源路径
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String projectRoot = System.getProperty("user.dir");

        // 规范化上传目录路径
        java.io.File uploadDir = new java.io.File(projectRoot, "uploads/images");
        String uploadPath = uploadDir.getAbsolutePath();

        // 确保路径以分隔符结尾（ResourceLocations要求）
        if (!uploadPath.endsWith(java.io.File.separator)) {
            uploadPath += java.io.File.separator;
        }

        // 转换为URL兼容格式（将反斜杠替换为正斜杠，并添加file:前缀）
        String resourceLocation = "file:" + uploadPath.replace("\\", "/");

        // 配置静态资源映射
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations(resourceLocation);

        System.out.println("静态资源映射配置: ");
        System.out.println("  项目根目录: " + projectRoot);
        System.out.println("  上传目录: " + uploadPath);
        System.out.println("  资源位置: " + resourceLocation);
        System.out.println("  访问路径: /uploads/images/** -> " + resourceLocation);
    }
}