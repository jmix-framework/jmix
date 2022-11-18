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

package io.jmix.securityoauth2.token.store;

import io.jmix.core.impl.StandardSerialization;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * {@link JdbcTokenStore} that can properly deserialize Jmix entities in such a way that they support lazy loading after
 * deserialization.
 */
public class JmixJdbcTokenStore extends JdbcTokenStore {

    protected StandardSerialization standardSerialization;

    public JmixJdbcTokenStore(DataSource dataSource, StandardSerialization standardSerialization) {
        super(dataSource);
        this.standardSerialization = standardSerialization;
    }

    @Override
    protected OAuth2AccessToken deserializeAccessToken(byte[] token) {
        return (OAuth2AccessToken) standardSerialization.deserialize(token);
    }

    @Override
    protected OAuth2RefreshToken deserializeRefreshToken(byte[] token) {
        return (OAuth2RefreshToken) standardSerialization.deserialize(token);
    }

    @Override
    protected OAuth2Authentication deserializeAuthentication(byte[] authentication) {
        return (OAuth2Authentication) standardSerialization.deserialize(authentication);
    }
}
