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

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.authentication.EmailRefreshTokenManager;
import io.jmix.email.entity.RefreshToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.EmailTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EmailTestConfiguration.class})
public class RefreshTokenStorageTest {

    @Autowired
    EmailRefreshTokenManager tokenManager;

    @Autowired
    SystemAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator.begin();
    }

    @AfterEach
    void tearDown() {
        authenticator.end();
    }

    @Test
    void testStoreAndLoadRoundTrip() {
        RefreshToken stored = tokenManager.storeRefreshTokenValue("rt-1");
        assertNotNull(stored.getId());
        assertEquals("rt-1", tokenManager.getRefreshTokenValue());

        RefreshToken updated = tokenManager.storeRefreshTokenValue("rt-2");
        assertEquals(stored.getId(), updated.getId());
        assertEquals("rt-2", tokenManager.getRefreshTokenValue());
    }
}
