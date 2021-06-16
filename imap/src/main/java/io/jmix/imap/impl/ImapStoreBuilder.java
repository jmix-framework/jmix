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

package io.jmix.imap.impl;

import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailSSLSocketFactory;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.imap.ImapProperties;
import io.jmix.imap.crypto.Encryptor;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.entity.ImapProxy;
import io.jmix.imap.entity.ImapSecureMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

@Component("imap_ImapStoreBuilder")
public class ImapStoreBuilder {

    private final static Logger log = LoggerFactory.getLogger(ImapStoreBuilder.class);

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage fileStorage;

    @Autowired
    protected ImapProperties config;
    @Autowired
    protected Encryptor encryptor;

    IMAPStore build(ImapMailBox mailBox, String password, boolean decryptPassword) throws MessagingException {
        String protocol = mailBox.getSecureMode() == ImapSecureMode.TLS ? "imaps" : "imap";

        Properties props = new Properties(System.getProperties());
        props.setProperty("mail.store.protocol", protocol);
        String timeout = String.valueOf(config.getTimeoutSeconds() * 1000);

        props.setProperty(String.format("mail.%s.connectiontimeout", protocol), timeout);
        props.setProperty(String.format("mail.%s.timeout", protocol), timeout);

        if (mailBox.getSecureMode() == ImapSecureMode.STARTTLS) {
            props.setProperty("mail.imap.starttls.enable", "true");
        }
        props.setProperty("mail.debug", "" + config.isDebug());

        if (mailBox.getSecureMode() != null) {
            MailSSLSocketFactory socketFactory = getMailSSLSocketFactory(mailBox);
            props.put(String.format("mail.%s.ssl.socketFactory", protocol), socketFactory);
        }

        ImapProxy proxy = mailBox.getProxy();
        if (proxy != null) {
            String proxyType = Boolean.TRUE.equals(proxy.getWebProxy()) ? "proxy" : "socks";
            props.put(String.format("mail.%s.%s.host", protocol, proxyType), proxy.getHost());
            props.put(String.format("mail.%s.%s.port", protocol, proxyType), proxy.getPort());
        }

        Session session = Session.getInstance(props, null);
        session.setDebug(config.isDebug());

        IMAPStore store = (IMAPStore) session.getStore(protocol);
        String passwordToConnect = decryptPassword ? decryptedPassword(mailBox, password) : password;
        store.connect(mailBox.getHost(), mailBox.getPort(), mailBox.getAuthentication().getUsername(), passwordToConnect);

        return store;
    }



    protected String decryptedPassword(ImapMailBox mailBox, String persistedPassword) {
        String password = mailBox.getAuthentication().getPassword();
        if (Objects.equals(password, persistedPassword)) {
            password = encryptor.getPlainPassword(mailBox);
        }
        return password;
    }

    protected MailSSLSocketFactory getMailSSLSocketFactory(ImapMailBox box) throws MessagingException {
        MailSSLSocketFactory socketFactory;
        try {
            socketFactory = new MailSSLSocketFactory();
            if (config.isTrustAllCertificates()) {
                log.debug("Configure factory to trust all certificates");
                socketFactory.setTrustAllHosts(true);
            } else {
                socketFactory.setTrustAllHosts(false);
                if (box.getRootCertificate() != null) {
                    log.debug("Configure factory to trust only known certificates and certificated from file#{}",
                            box.getRootCertificate());
                    if (fileStorage == null) {
                        fileStorage = fileStorageLocator.getDefault();
                    }
                    try (InputStream rootCert = fileStorage.openStream(box.getRootCertificate())) {
                        socketFactory.setTrustManagers(new TrustManager[]{new UnifiedTrustManager(rootCert)});
                    } catch (FileStorageException | GeneralSecurityException | IOException e) {
                        throw new RuntimeException("SSL error", e);
                    }
                }
            }
        } catch (GeneralSecurityException e) {
            throw new MessagingException("SSL Socket factory exception", e);
        }
        return socketFactory;
    }

    protected static class UnifiedTrustManager implements X509TrustManager {
        protected X509TrustManager defaultTrustManager;
        protected X509TrustManager localTrustManager;

        UnifiedTrustManager(InputStream rootCertStream) {
            try {
                this.defaultTrustManager = createTrustManager(null);

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca = cf.generateCertificate(rootCertStream);

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("rootCA", ca);

                this.localTrustManager = createTrustManager(keyStore);
            } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
                log.warn("Can't build SSL Trust Manager", e);
            }
        }

        protected X509TrustManager createTrustManager(KeyStore store) throws NoSuchAlgorithmException, KeyStoreException {
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(store);
            TrustManager[] trustManagers = tmf.getTrustManagers();
            return (X509TrustManager) trustManagers[0];
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                defaultTrustManager.checkClientTrusted(chain, authType);
            } catch (CertificateException ce) {
                localTrustManager.checkClientTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] first = defaultTrustManager.getAcceptedIssuers();
            X509Certificate[] second = localTrustManager.getAcceptedIssuers();
            X509Certificate[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }
    }


}
