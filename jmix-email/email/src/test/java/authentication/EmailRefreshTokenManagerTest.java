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

package authentication;

import io.jmix.email.EmailerProperties;
import io.jmix.email.authentication.impl.EmailRefreshTokenManagerImpl;
import io.jmix.email.entity.RefreshToken;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.Nullable;
import test_support.TestEmailerProperties;

import static org.junit.jupiter.api.Assertions.*;

class EmailRefreshTokenManagerTest {

    @Test
    void testInitialValueFromProperties() {
        EmailRefreshTokenManagerImpl manager = managerWithoutStoredToken("initial-rt");
        assertEquals("initial-rt", manager.getRefreshTokenValue());
    }

    @Test
    void testStoredTokenTakesPrecedenceOverProperty() {
        RefreshToken stored = new RefreshToken();
        stored.setTokenValue("stored-rt");
        EmailRefreshTokenManagerImpl manager = new EmailRefreshTokenManagerImpl(null, createProperties("initial-rt")) {
            @Override
            public RefreshToken loadRefreshToken() {
                return stored;
            }
        };
        assertEquals("stored-rt", manager.getRefreshTokenValue());
    }

    @Test
    void testMissingTokenProducesMeaningfulError() {
        EmailRefreshTokenManagerImpl manager = managerWithoutStoredToken(null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, manager::getRefreshTokenValue);
        assertTrue(exception.getMessage().contains("jmix.email.oauth2.refresh-token"));
    }

    private EmailRefreshTokenManagerImpl managerWithoutStoredToken(@Nullable String initialPropertyValue) {
        return new EmailRefreshTokenManagerImpl(null, createProperties(initialPropertyValue)) {
            @Override
            @Nullable
            public RefreshToken loadRefreshToken() {
                return null;
            }
        };
    }

    private EmailerProperties createProperties(@Nullable String refreshToken) {
        return TestEmailerProperties.create(new EmailerProperties.OAuth2(
                true, "google", "test-client", "test-secret", refreshToken, "common"));
    }
}
