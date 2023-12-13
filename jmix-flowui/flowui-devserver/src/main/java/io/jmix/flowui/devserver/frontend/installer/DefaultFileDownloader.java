/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.flowui.devserver.frontend.installer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;

public final class DefaultFileDownloader implements FileDownloader {
    public static final String HTTPS_PROTOCOLS = "https.protocols";
    private final ProxyConfig proxyConfig;
    private String userName;
    private String password;

    public DefaultFileDownloader(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public void download(URI downloadURI, File destination, String userName, String password) throws DownloadException {
        this.userName = userName;
        this.password = password;
        String oldProtocols = System.setProperty("https.protocols", "TLSv1.2");

        try {
            if ("file".equalsIgnoreCase(downloadURI.getScheme())) {
                FileUtils.copyFile(new File(downloadURI), destination);
            } else {
                this.downloadFile(destination, downloadURI);
            }
        } catch (IOException var10) {
            throw new DownloadException("Could not download " + downloadURI, var10);
        } finally {
            if (oldProtocols == null) {
                System.clearProperty("https.protocols");
            } else {
                System.setProperty("https.protocols", oldProtocols);
            }

        }

    }

    private void downloadFile(File destination, URI downloadUri) throws IOException, DownloadException {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder().version(Version.HTTP_1_1).followRedirects(Redirect.NORMAL);
        final ProxyConfig.Proxy proxy = this.proxyConfig.getProxyForUrl(downloadUri.toString());
        if (proxy != null) {
            this.getLogger().debug("Downloading via proxy {}", proxy.toString());
            clientBuilder = clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxy.host, proxy.port)));
            clientBuilder = clientBuilder.authenticator(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return this.getRequestorType() == RequestorType.PROXY ? new PasswordAuthentication(proxy.username, proxy.password.toCharArray()) : new PasswordAuthentication(DefaultFileDownloader.this.userName, DefaultFileDownloader.this.password.toCharArray());
                }
            });
        } else {
            this.getLogger().debug("No proxy was configured, downloading directly");
            if (this.userName != null && !this.userName.isEmpty() && this.password != null && !this.password.isEmpty()) {
                this.getLogger().info("Using credentials ({})", this.userName);
                clientBuilder = clientBuilder.authenticator(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(DefaultFileDownloader.this.userName, DefaultFileDownloader.this.password.toCharArray());
                    }
                });
            }
        }

        HttpClient client = clientBuilder.build();
        HttpRequest request = HttpRequest.newBuilder().uri(downloadUri).GET().build();

        try {
            HttpResponse<Path> response = client.send(request, BodyHandlers.ofFile(destination.toPath()));
            if (response.statusCode() != 200) {
                throw new DownloadException("Got error code " + response.statusCode() + " from the server.");
            } else {
                long expected = response.headers().firstValueAsLong("Content-Length").getAsLong();
                if (destination.length() != expected) {
                    throw new DownloadException("Error downloading from " + downloadUri + ". Expected " + expected + " bytes but got " + destination.length());
                }
            }
        } catch (InterruptedException var10) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(var10);
        }
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger("FileDownloader");
    }
}

