/*
 * Copyright 2020 Haulmont.
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

package io.jmix.email;

import com.haulmont.cuba.core.sys.AppContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Component("email_MailSender")
public class JmixMailSender extends JavaMailSenderImpl {

    @Autowired
    protected EmailerProperties emailerProperties;

    @Autowired
    protected EmailSmtpProperties smtpProperties;

    private boolean propertiesInitialized;

    @Override
    public String getHost() {
        return smtpProperties.getHost();
    }

    @Override
    public void setHost(String host) {
        throw new UnsupportedOperationException("Use jmix.email.smtp.* properties");
    }

    @Override
    public int getPort() {
        return smtpProperties.getPort();
    }

    @Override
    public void setPort(int port) {
        throw new UnsupportedOperationException("Use jmix.email.smtp.* properties");
    }

    @Override
    public String getUsername() {
        return smtpProperties.isAuthRequired() && !StringUtils.isBlank(smtpProperties.getUser()) ?
                smtpProperties.getUser() : null;
    }

    @Override
    public void setUsername(String username) {
        throw new UnsupportedOperationException("Use jmix.email.smtp.* properties");
    }

    @Override
    public String getPassword() {
        return smtpProperties.isAuthRequired() && !StringUtils.isBlank(smtpProperties.getPassword()) ?
                smtpProperties.getPassword() : null;
    }

    @Override
    public void setPassword(String password) {
        throw new UnsupportedOperationException("Use jmix.email.smtp.* properties");
    }

    @Override
    public synchronized Session getSession() {
        if (!propertiesInitialized) {
            Properties properties = createJavaMailProperties();
            setJavaMailProperties(properties);
            propertiesInitialized = true;
        }
        return super.getSession();
    }

    public synchronized void updateSession(){
        propertiesInitialized = false;
    }

    protected Properties createJavaMailProperties() {
        long connectionTimeoutMillis = smtpProperties.getConnectionTimeoutSec() * 1000;
        long timeoutMillis = smtpProperties.getTimeoutSec() * 1000;

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", String.valueOf(smtpProperties.isAuthRequired()));
        properties.setProperty("mail.smtp.starttls.enable", String.valueOf(smtpProperties.isStartTlsEnabled()));
        properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(connectionTimeoutMillis));
        properties.setProperty("mail.smtp.timeout", String.valueOf(timeoutMillis));
        properties.setProperty("mail.smtp.ssl.enable", String.valueOf(smtpProperties.isSslEnabled()));
        properties.setProperty("mail.mime.allowutf8", String.valueOf(emailerProperties.isUtf8Enabled()));

        Set excludedProperties = new HashSet<>(properties.keySet());
        for (String name : AppContext.getPropertyNames()) {
            if (includeJavaMailProperty(name, excludedProperties)) {
                String value = AppContext.getProperty(name);
                if (value != null) {
                    properties.put(name, value);
                }
            }
        }
        return properties;
    }

    protected boolean includeJavaMailProperty(String name, Set excludedProperties) {
        return name.startsWith("mail.") && !excludedProperties.contains(name);
    }
}