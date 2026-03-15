package org.example.demoapp.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demoapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 公开路径列表，与WebConfig中的excludePathPatterns保持一致
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/error",
        "/uploads"
    };

    /**
     * 检查请求路径是否为公开路径
     */
    private boolean isPublicPath(String requestUri, String method) {
        // 检查静态公开路径
        for (String publicPath : PUBLIC_PATHS) {
            if (requestUri.startsWith(publicPath)) {
                return true;
            }
        }

        // 商品相关路径的公开规则
        if (requestUri.startsWith("/api/products")) {
            // GET请求到商品列表和详情页面公开
            if ("GET".equalsIgnoreCase(method)) {
                // 排除需要认证的路径
                if (requestUri.startsWith("/api/products/my")) {
                    return false; // 我的商品需要认证
                }
                // 其他GET请求公开
                return true;
            }
            // 非GET请求需要认证
            return false;
        }

        return false;
    }

    /**
     * 统一发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response,
                                   int statusCode,
                                   String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("code", statusCode);

        objectMapper.writeValue(response.getWriter(), result);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String token = request.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        boolean isPublic = isPublicPath(requestUri, method);

        // 尝试解析token，如果token有效则设置userId
        if (token != null && !token.trim().isEmpty() && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                request.setAttribute("userId", userId);
            }
        }

        // 公开路径直接放行
        if (isPublic) {
            return true;
        }

        // 非公开路径需要有效token
        if (token == null || token.trim().isEmpty()){
            sendErrorResponse(response, 401, "请先登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)){
            sendErrorResponse(response, 401, "Token无效或已过期，请重新登录");
            return false;
        }

        return true;

    }
}
