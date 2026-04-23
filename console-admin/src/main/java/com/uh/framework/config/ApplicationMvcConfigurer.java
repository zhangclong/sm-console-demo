package com.uh.framework.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;
import java.util.List;

import static com.uh.common.constant.FrameworkConstants.MAX_UPLOAD_SIZE;
import static com.uh.common.constant.FrameworkConstants.MAX_UPLOAD_SIZE_PER_FILE;

/**
 * 通用配置
 *
 * @author XiaoZhangTongZhi
 */
@Configuration
@EnableWebMvc
public class ApplicationMvcConfigurer implements WebMvcConfigurer {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 不再映射静态资源到特定路径，映射工作转移到：com.uh.framework.web.StaticResourceServlet
    }

    // 配置MultipartResolver以支持文件上传
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        // 设置默认编码
        resolver.setDefaultEncoding("UTF-8");
        // 设置最大上传文件大小
        resolver.setMaxUploadSize(MAX_UPLOAD_SIZE); // 10MB
        // 设置单个文件的最大大小
        resolver.setMaxUploadSizePerFile(MAX_UPLOAD_SIZE_PER_FILE); // 2MB
        // 设置内存中的缓存大小
        resolver.setMaxInMemorySize(4096);
        return resolver;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
