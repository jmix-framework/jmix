/*
 * Copyright 2020 Haulmont.
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

package io.jmix.autoconfigure.core;

import io.jmix.core.*;
import io.jmix.core.impl.JmixMessageSource;
import io.jmix.core.pessimisticlocking.LockManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxProperties;
import org.springframework.boot.autoconfigure.jmx.ParentAwareNamingStrategy;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.export.naming.ObjectNamingStrategy;
import org.springframework.scripting.ScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;
import java.util.Arrays;

@AutoConfiguration
@Import({CoreConfiguration.class})
@EnableConfigurationProperties(JmxProperties.class)
@AutoConfigureBefore({ValidationAutoConfiguration.class, JmxAutoConfiguration.class})
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource(JmixModules modules, Resources resources) {
        return new JmixMessageSource(modules, resources);
    }

    @Bean
    @ConditionalOnMissingBean(type = "org.springframework.scripting.ScriptEvaluator")
    public ScriptEvaluator scriptEvaluator() {
        return new GroovyScriptEvaluator();
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer lockCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(LockManager.LOCKS_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(LockManager.LOCKS_CACHE_NAME, configuration);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(value = ObjectNamingStrategy.class, search = SearchStrategy.CURRENT)
    public ParentAwareNamingStrategy objectNamingStrategy(JmxProperties properties) {
        String defaultDomain = properties.getDefaultDomain();
        ParentAwareNamingStrategy namingStrategy = new ParentAwareNamingStrategy(new JmixAnnotationJmxAttributeSource(defaultDomain));
        if (StringUtils.hasLength(defaultDomain)) {
            namingStrategy.setDefaultDomain(defaultDomain);
        }
        namingStrategy.setEnsureUniqueRuntimeObjectNames(properties.isUniqueNames());

        return namingStrategy;
    }

    /**
     * A standard implementation of {@link CorsConfigurationSource} that takes most of its settings from
     * {@link CorsProperties}.
     * <p>
     * A bean with corsConfigurationSource is used by CorsFilter in Spring Security filter chain. Define a bean with the
     * "corsConfigurationSource" name in the application if you want to override the standard implementation.
     *
     * @see HttpSecurity#cors()
     */
    @Bean
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.getAllowCredentials());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        for (String urlPattern : corsProperties.getUrlPatterns()) {
            source.registerCorsConfiguration(urlPattern, configuration);
        }
        return source;
    }
}
