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

package io.jmix.email.sandbox;

import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.URLName;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.smtp.SMTPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

//todo remove
public class OAuth2MailSender extends JavaMailSenderImpl implements JavaMailSender {

    private static final Logger log = LoggerFactory.getLogger(OAuth2MailSender.class);

    /*protected final OAuth2TokenProvider tokenProvider;

    public OAuth2MailSender(OAuth2TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Session getSession() {
        // Create session with custom authenticator
        return Session.getInstance(getJavaMailProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        getUsername(),
                        tokenProvider.getAccessToken()
                );
            }
        });
    }*/


    // =======================================

    protected final MailOAuth2AuthorizedClientManager authManager;
    protected final String registrationId;
    //protected final String username;

    public OAuth2MailSender(MailOAuth2AuthorizedClientManager authManager, String registrationId, String username) {
        super.setUsername(username);
        this.authManager = authManager;
        this.registrationId = registrationId;
        //this.username = username; //todo use from super?
    }

    @Override
    protected void doSend(MimeMessage[] mimeMessages, Object[] allRecipients) throws MailException {
        log.info("[IVGA] Send: start");

        // 1. Acquire access token
        String username = getUsername();

        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(username)
                .build();

        OAuth2AuthorizedClient client = authManager.authorize(request);
        if (client == null || client.getAccessToken() == null) {
            throw new MailException("Unable to acquire access token") {};
        }

        String accessToken = client.getAccessToken().getTokenValue();

        log.info("[IVGA] Send: client={}, token={}", client, accessToken);

        // 2. Prepare JavaMail Session and credentials
        Session session = getSession();
        URLName smtpUrl = new URLName("smtp", getHost(), getPort(), null, username, null);
        log.info("[IVGA] Send: smtpUrl={}", smtpUrl);
        PasswordAuthentication auth = new PasswordAuthentication(username, accessToken);
        session.setPasswordAuthentication(smtpUrl, auth);

        // 3. Send messages manually
        for (MimeMessage mimeMessage : mimeMessages) {
            SMTPTransport transport = null;
            try {
                transport = (SMTPTransport) session.getTransport("smtp");
                transport.connect(getHost(), getPort(), username, null);
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            } catch (Exception ex) {
                throw new MailException("Failed to send email", ex) {};
            } finally {
                if (transport != null) try { transport.close(); } catch (Exception ignored) {}
            }
        }
    }
}
