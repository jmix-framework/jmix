/*
 * Copyright 2025 Haulmont.
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

package io.jmix.authserver.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface to provide ability to customize ObjectMapper that is used by JdbcOAuth2AuthorizationService.
 * <p>
 * JdbcOAuth2AuthorizationService requires mixin for User entity to properly serialize/deserialize it.
 * Create bean that implements this interface and add mixin for User entity generated in project.
 * <p>
 * Custom mixin can be either implemented on project side or {@link DefaultOAuth2TokenUserMixin} can be used.
 * <p>
 * Example:
 * <pre>
 * &#064;Component
 * public class MyJdbcOAuth2TokenObjectMapperCustomizer implements JdbcOAuth2AuthorizationServiceObjectMapperCustomizer {
 *
 *     &#064;Override
 *     public void customize(&#064;NonNull ObjectMapper objectMapper) {
 *         objectMapper.addMixIn(User.class, DefaultOAuth2TokenUserMixin.class);
 *     }
 * }
 * </pre>
 */
public interface JdbcOAuth2AuthorizationServiceObjectMapperCustomizer {

    void customize(ObjectMapper objectMapper);
}
