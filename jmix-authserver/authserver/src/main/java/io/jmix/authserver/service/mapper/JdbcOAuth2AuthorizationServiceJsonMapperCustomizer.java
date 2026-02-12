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

import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import tools.jackson.databind.json.JsonMapper;

/**
 * Interface to provide ability to customize {@link JsonMapper} that is used by {@link JdbcOAuth2AuthorizationService}.
 *
 * These customizers will be applied at the last step of {@link JsonMapper} configuration.
 */
// TODO [SB4] Renamed from JdbcOAuth2AuthorizationServiceObjectMapperCustomizer.
public interface JdbcOAuth2AuthorizationServiceJsonMapperCustomizer {

    void customize(JsonMapper.Builder jsonMapperBuilder);
}
