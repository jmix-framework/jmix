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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.email.smtp")
@ConstructorBinding
public class EmailSmtpProperties {
    String host;
    int port;
    boolean authRequired;
    boolean startTlsEnabled;
    String user;
    String password;
    int connectionTimeoutSec;
    boolean sslEnabled;
    int timeoutSec;

    public EmailSmtpProperties(@DefaultValue("test.host") String host,
                               @DefaultValue("25") int port,
                               @DefaultValue("false") boolean authRequired,
                               @DefaultValue("false") boolean startTlsEnabled,
                               String user,
                               String password,
                               @DefaultValue("20") int connectionTimeoutSec,
                               @DefaultValue("false") boolean sslEnabled,
                               @DefaultValue("60") int timeoutSec) {
        this.host = host;
        this.port = port;
        this.authRequired = authRequired;
        this.startTlsEnabled = startTlsEnabled;
        this.user = user;
        this.password = password;
        this.connectionTimeoutSec = connectionTimeoutSec;
        this.sslEnabled = sslEnabled;
        this.timeoutSec = timeoutSec;
    }

    /**
     *
     * @return SMTP server address.
     */
    public String getHost() {
        return host;
    }

    /**
     *
     * @return SMTP server port.
     */
    public int getPort() {
        return port;
    }

    /**
     *
     * @return Whether to authenticate on SMTP server.
     */
    public boolean isAuthRequired() {
        return authRequired;
    }

    /**
     *
     * @return Whether to use STARTTLS command during the SMTP server authentication.
     */
    public boolean isStartTlsEnabled() {
        return startTlsEnabled;
    }

    /**
     *
     * @return User name for the SMTP server authentication.
     */
    public String getUser() {
        return user;
    }

    /**
     *
     * @return User password for the SMTP server authentication.
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @return SMTP connection timeout value in seconds.
     */
    public int getConnectionTimeoutSec() {
        return connectionTimeoutSec;
    }

    /**
     *
     * @return If true, SSL is used to connect
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     *
     * @return SMTP I/O timeout value in seconds.
     */
    public int getTimeoutSec() {
        return timeoutSec;
    }
}
