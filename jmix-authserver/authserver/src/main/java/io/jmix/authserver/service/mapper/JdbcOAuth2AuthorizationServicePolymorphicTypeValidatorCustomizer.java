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
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Interface to provide ability to customize {@link BasicPolymorphicTypeValidator}
 * that is used by {@link JsonMapper within {@link JdbcOAuth2AuthorizationService}.
 */
public interface JdbcOAuth2AuthorizationServicePolymorphicTypeValidatorCustomizer {

    void customize(BasicPolymorphicTypeValidator.Builder polymorphicTypeValidatorBuilder);
}