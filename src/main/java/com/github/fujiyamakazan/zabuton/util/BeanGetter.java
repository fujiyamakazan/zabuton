package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

import javax.servlet.ServletContext;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BeanGetter implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BeanGetter.class);

    /**
     * コンテキストからSpringBeanを取得します。
     */
    public static <T> T getBean(Class<T> requiredType) {
        T bean;
        ServletContext servletContext = WebApplication.get().getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
            .getRequiredWebApplicationContext(servletContext);
        bean = webApplicationContext.getBean(requiredType);
        return bean;

    }

}
