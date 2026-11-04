package com.example.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import javax.annotation.Resource;
import java.io.IOException;

@Configuration
public class WebConfig implements  WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    // 加自定义拦截器JwtInterceptor，设置拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/login")
                .excludePathPatterns("/register")
                .excludePathPatterns("/captcha")
                .excludePathPatterns("/files/**")
                // 前端打包产物嵌入后端时放行静态资源（接口路径与这些前缀不冲突）
                .excludePathPatterns("/index.html")
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/img/**")
                .excludePathPatterns("/fonts/**")
                .excludePathPatterns("/media/**")
                // 游客可浏览的只读接口（点赞/收藏/评论/发布等写操作仍需登录）
                .excludePathPatterns("/blog/selectPage")
                .excludePathPatterns("/blog/selectById/**")
                .excludePathPatterns("/blog/selectRecommend/**")
                .excludePathPatterns("/blog/selectTop")
                .excludePathPatterns("/blog/updateReadCount/**")
                .excludePathPatterns("/category/selectAll")
                .excludePathPatterns("/activity/selectPage")
                .excludePathPatterns("/activity/selectAll")
                .excludePathPatterns("/activity/selectById/**")
                .excludePathPatterns("/activity/selectTop")
                .excludePathPatterns("/activity/updateReadCount/**")
                .excludePathPatterns("/notice/selectAll")
                .excludePathPatterns("/notice/selectPage")
                .excludePathPatterns("/notice/selectById/**")
                .excludePathPatterns("/comment/selectForUser")
                .excludePathPatterns("/comment/selectCount")
                // 个人空间（公开视角）：信息/文章/粉丝/关注列表游客可看
                .excludePathPatterns("/space/**")
                // 错误页转发路径放行，否则错误处理时会被拦截器二次拦截导致原始错误页崩溃
                .excludePathPatterns("/error");
    }

    // 前端打包产物放在 classpath:/static/ 时，history 路由刷新后回退到 index.html
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String resourcePath, org.springframework.core.io.Resource location) throws IOException {
                        org.springframework.core.io.Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        org.springframework.core.io.Resource index = location.createRelative("index.html");
                        return index.exists() && index.isReadable() ? index : null;
                    }
                });
    }
}