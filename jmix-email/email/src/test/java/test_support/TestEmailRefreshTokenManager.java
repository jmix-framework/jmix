/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.entity.RefreshToken;
import org.jspecify.annotations.Nullable;

/**
 * In-memory {@link EmailRefreshTokenManager} for unit tests.
 */
public class TestEmailRefreshTokenManager implements EmailRefreshTokenManager {

    private final String initialValue;
    private String storedValue;

    public TestEmailRefreshTokenManager(@Nullable String initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public RefreshToken storeRefreshTokenValue(String refreshTokenValue) {
        storedValue = refreshTokenValue;
        RefreshToken token = new RefreshToken();
        token.setTokenValue(refreshTokenValue);
        return token;
    }

    @Override
    public String getRefreshTokenValue() {
        if (storedValue != null) {
            return storedValue;
        }
        if (initialValue != null) {
            return initialValue;
        }
        throw new IllegalStateException("No refresh token available");
    }

    @Override
    @Nullable
    public RefreshToken loadRefreshToken() {
        if (storedValue == null) {
            return null;
        }
        RefreshToken token = new RefreshToken();
        token.setTokenValue(storedValue);
        return token;
    }
}
