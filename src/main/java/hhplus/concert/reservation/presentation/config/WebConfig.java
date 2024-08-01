package hhplus.concert.reservation.presentation.config;

import hhplus.concert.reservation.application.token.usecase.QueueUsecase;
import hhplus.concert.reservation.presentation.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final QueueUsecase queueUsecase;

    @Autowired
    public WebConfig(QueueUsecase queueUsecase) {
        this.queueUsecase = queueUsecase;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor(queueUsecase))
                .addPathPatterns("/api/reservation/**")
                .addPathPatterns("/api/concerts/**")
                .excludePathPatterns("/api/concerts");
    }
}
