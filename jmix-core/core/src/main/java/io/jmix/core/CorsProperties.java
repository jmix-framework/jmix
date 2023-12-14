/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.util.pattern.PathPattern;

import org.springframework.lang.Nullable;
import java.util.List;

/**
 * CORS settings used by default {@link org.springframework.web.cors.CorsConfigurationSource} provided by Jmix core
 * module auto-configuration.
 */
@ConfigurationProperties(prefix = "jmix.cors")
public class CorsProperties {

    /**
     * A list of origins for which cross-origin requests are allowed.
     *
     * @see CorsConfiguration#getAllowedOrigins()
     */
    List<String> allowedOrigins;

    /**
     * A list of headers that a pre-flight request can list as allowed for use during an actual request.
     *
     * @see CorsConfiguration#getAllowedHeaders() ()
     */
    List<String> allowedHeaders;

    /**
     * HTTP methods to allow, e.g. "GET", "POST", "PUT", etc.
     *
     * @see CorsConfiguration#getAllowedMethods() ()
     */
    List<String> allowedMethods;

    /**
     * A list of response headers other than simple headers (i.e. Cache-Control, Content-Language, Content-Type,
     *     Expires, Last-Modified, or Pragma) that an actual response might have and can be exposed.<br/>
     *
     * <p>The special value "*" allows all headers to be exposed for non-credentialed requests.</p>
     *
     * <p>By default this is not set.</p>
     *
     * @see CorsConfiguration#getExposedHeaders()
     */
    List<String> exposedHeaders;

    /**
     * URL path patterns used for selecting a default CORS configurations provided by Jmix core starter.
     *
     * @see PathPattern
     * @see AntPathMatcher
     */
    List<String> urlPatterns;

    /**
     * Whether user credentials are supported.
     *
     * @see CorsConfiguration#getAllowCredentials()
     */
    Boolean allowCredentials;

    public CorsProperties(@DefaultValue("*") List<String> allowedOrigins,
                          @DefaultValue("*") List<String> allowedHeaders,
                          @DefaultValue("*") List<String> allowedMethods,
                          List<String> exposedHeaders,
                          @DefaultValue("/**") List<String> urlPatterns,
                          Boolean allowCredentials) {
        this.allowedOrigins = allowedOrigins;
        this.allowedHeaders = allowedHeaders;
        this.allowedMethods = allowedMethods;
        this.exposedHeaders = exposedHeaders;
        this.urlPatterns = urlPatterns;
        this.allowCredentials = allowCredentials;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    @Nullable
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    @Nullable
    public Boolean getAllowCredentials() {
        return allowCredentials;
    }
}
