/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.rest;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class})
@PropertySource("classpath:/io/jmix/rest/module.properties")
public class RestConfiguration implements WebMvcConfigurer {

    @Autowired
    protected RestProperties restProperties;

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(restProperties.getMaxUploadSize());
        return resolver;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer customizer) {
        customizer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    //Commented the next code for a while, need to figure out why it doesn't work.
    //It is replaced by customCorsFilter() (see below)
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = restProperties.getAllowedOrigins();

        CorsRegistration corsRegistration = registry.addMapping("/rest/**")
                .allowedOrigins(allowedOrigins)
                .allowedHeaders("*")
                .allowedMethods("*")
                .exposedHeaders("X-Total-Count", "Content-Disposition");

        CorsRegistration corsAuthRegistration = registry.addMapping("oauth/token")
                .allowedOrigins(allowedOrigins)
                .allowedHeaders("*")
                .allowedMethods("*")
                .exposedHeaders("X-Total-Count", "Content-Disposition");

        if (!Arrays.asList(allowedOrigins).contains(CorsConfiguration.ALL)) {
            corsRegistration.allowCredentials(true);
            corsAuthRegistration.allowCredentials(true);
        }
    }
    */

    /**
     * Enable CORS for token endpoints and REST endpoints
     */
    //todo /oauth/** endpoint CORS configuration must be defined in the security-oauth2 module
    @Bean
    public FilterRegistrationBean<CorsFilter> customCorsFilter() {
        List<String> allowedOrigins = Arrays.asList(restProperties.getAllowedOrigins());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/oauth/**", config);
        source.registerCorsConfiguration("/rest/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setUrlPatterns(Arrays.asList("/oauth/*", "/rest/*"));

        //The filter must be loaded before OAuth2 and REST security filters
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        return bean;
    }
}
