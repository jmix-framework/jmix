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

import io.jmix.email.authentication.OAuth2JavaMailSender;
import io.jmix.email.authentication.OAuth2TokenProvider;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class OAuth2JavaMailSenderTest {

    TestTokenProvider tokenProvider = new TestTokenProvider();
    TestTransport transport = new TestTransport(Session.getInstance(new Properties()), null);
    OAuth2JavaMailSender sender = new OAuth2JavaMailSender(tokenProvider) {
        @Override
        protected Transport getTransport(Session session) {
            return transport;
        }
    };

    OAuth2JavaMailSenderTest() {
        sender.setHost("smtp.example.com");
        sender.setPort(587);
        sender.setUsername("mailbox@example.com");
    }

    @Test
    void testFreshTokenIsPassedOnEveryConnect() throws Exception {
        tokenProvider.accessToken = "access-token-1";
        sender.testConnection();

        tokenProvider.accessToken = "access-token-2";
        sender.testConnection();

        // The current token is passed explicitly as the password, so the jakarta.mail session
        // cache of password authentication is never consulted and never populated.
        assertEquals(List.of("access-token-1", "access-token-2"), transport.passwords);
        assertEquals("mailbox@example.com", transport.lastUser);
    }

    @Test
    void testTokenAcquisitionFailureIsPropagated() {
        tokenProvider.failure = new IllegalStateException("invalid_grant");

        IllegalStateException exception = assertThrows(IllegalStateException.class, sender::testConnection);
        assertTrue(exception.getMessage().contains("invalid_grant"));
        assertTrue(transport.passwords.isEmpty());
    }

    static class TestTokenProvider implements OAuth2TokenProvider {

        String accessToken = "access-token";
        RuntimeException failure;

        @Override
        public String getAccessToken() {
            if (failure != null) {
                throw failure;
            }
            return accessToken;
        }

        @Override
        public String getRefreshToken() {
            return "refresh-token";
        }
    }

    static class TestTransport extends Transport {

        List<String> passwords = new ArrayList<>();
        String lastUser;

        TestTransport(Session session, URLName urlname) {
            super(session, urlname);
        }

        @Override
        protected boolean protocolConnect(String host, int port, String user, String password) {
            lastUser = user;
            passwords.add(password);
            return true;
        }

        @Override
        public void sendMessage(Message msg, Address[] addresses) {
        }
    }
}
