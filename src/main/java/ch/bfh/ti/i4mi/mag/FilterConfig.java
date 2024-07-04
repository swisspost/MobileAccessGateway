package ch.bfh.ti.i4mi.mag;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TraceHeaderLoggingFilter> loggingFilter(){
        FilterRegistrationBean<TraceHeaderLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new TraceHeaderLoggingFilter());
        registrationBean.addUrlPatterns("/*"); // Apply filter to all URL patterns

        return registrationBean;
    }
}
