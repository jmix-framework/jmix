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

package io.jmix.securityoauth2.token.store.cleanup.impl;

import io.jmix.securityoauth2.token.store.cleanup.OAuth2ExpiredTokenCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryOAuth2ExpiredTokenCleaner implements OAuth2ExpiredTokenCleaner {

    private static final Logger log = LoggerFactory.getLogger(InMemoryOAuth2ExpiredTokenCleaner.class);

    @Override
    public int removeExpiredAccessTokens() {
        log.warn("Cleanup of access tokens is not supported for in-memory Token Store");
        return 0;
    }

    @Override
    public int removeExpiredRefreshTokens() {
        log.warn("Cleanup of refresh tokens is not supported for in-memory Token Store");
        return 0;
    }
}
