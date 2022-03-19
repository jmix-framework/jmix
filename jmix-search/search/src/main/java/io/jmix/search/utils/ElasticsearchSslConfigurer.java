/*
 * Copyright 2021 Haulmont.
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

package io.jmix.search.utils;

import com.google.common.base.Strings;
import io.jmix.core.Resources;
import io.jmix.search.SearchProperties;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

@Component("search_ElasticsearchSslConfigurer")
public class ElasticsearchSslConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSslConfigurer.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected Resources resources;

    @Nullable
    public SSLContext createSslContext() {
        String certificateLocation = searchProperties.getElasticsearchSslCertificateLocation();

        if (Strings.isNullOrEmpty(certificateLocation)) {
            return null;
        } else {
            log.debug("Create SSL Context using CA certificate '{}'", certificateLocation);
            CertificateFactory factory = getCertificateFactory();
            Certificate certificate = createCertificate(factory, certificateLocation);
            KeyStore keyStore = getKeyStore();
            setCertificateToStore(keyStore, searchProperties.getElasticsearchSslCertificateAlias(), certificate);
            return buildSslContext(keyStore);
        }
    }

    protected CertificateFactory getCertificateFactory() {
        try {
            String factoryType = searchProperties.getElasticsearchSslCertificateFactoryType();
            log.debug("Get Certificate Factory '{}'", factoryType);
            return CertificateFactory.getInstance(factoryType);
        } catch (CertificateException e) {
            throw new RuntimeException("Failed to create Certificate Factory", e);
        }
    }

    protected Certificate createCertificate(CertificateFactory factory, String caLocation) {
        try (InputStream is = resources.getResourceAsStream(caLocation)) {
            if (is == null) {
                throw new RuntimeException(String.format("File not found in '%s'", caLocation));
            } else {
                return factory.generateCertificate(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load certificate file", e);
        } catch (CertificateException e) {
            throw new RuntimeException("Unable to generate certificate", e);
        }
    }

    protected KeyStore getKeyStore() {
        try {
            String keyStoreType = searchProperties.getElasticsearchSslKeyStoreType();
            log.debug("Get Key Store '{}'", keyStoreType);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException("Unable to get Key Store", e);
        }
    }

    protected void setCertificateToStore(KeyStore keyStore, String alias, Certificate certificate) {
        try {
            log.debug("Set certificate entry with alias '{}'", alias);
            keyStore.setCertificateEntry(alias, certificate);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Unable to set certificate");
        }
    }

    protected SSLContext buildSslContext(KeyStore keyStore) {
        try {
            return SSLContexts.custom()
                    .loadTrustMaterial(keyStore, null)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException("Unable to build SSL Context");
        }
    }
}
