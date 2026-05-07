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

package io.jmix.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component("core_ApplicationInfoProvider")
@ConditionalOnClass(WebServerApplicationContext.class)
public class ApplicationInfoProvider {

    private static final Logger log = getLogger(ApplicationInfoProvider.class);

    protected static final String APP_INFO_DIR_NAME = "/app-info";
    protected static final String APP_INFO_FILE_NAME = "app-info.json";

    @Autowired
    protected Environment environment;
    @Autowired
    protected CoreProperties coreProperties;

    protected String host;
    protected int port;
    protected String contextPath;
    protected String protocol;
    protected String fullUrl;
    protected List<String> activeProfiles;
    protected OffsetDateTime startupTime;

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        initValues(event.getApplicationContext());
        createAppInfoFile();
        log.debug("ApplicationInfoProvider initialization is completed");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }

    public OffsetDateTime getStartupTime() {
        return startupTime;
    }

    protected void initValues(ConfigurableApplicationContext context) {
        this.protocol = resolveProtocol();
        this.host = resolveHost();
        this.port = resolvePort(context);
        this.contextPath = resolveContextPath();
        this.fullUrl = resolveFullUrl(protocol, host, port, contextPath);
        this.activeProfiles = resolveActiveProfiles();
        this.startupTime = OffsetDateTime.now();
    }

    protected String resolveProtocol() {
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        return sslEnabled ? "https" : "http";
    }

    protected String resolveHost() {
        return environment.getProperty("server.address", "localhost");
    }

    protected int resolvePort(ConfigurableApplicationContext context) {
        int serverPort = environment.getProperty("server.port", Integer.class, 8080);

        if (serverPort == 0) {
            log.debug("Random port detected, retrieving from web server context");
            if (context instanceof WebServerApplicationContext webServerApplicationContext) {
                WebServer webServer = webServerApplicationContext.getWebServer();
                if (webServer != null) {
                    serverPort = webServer.getPort();
                } else {
                    log.warn("Web server is null - unable to retrieve port from it.");
                }
            } else {
                log.warn("ApplicationContext is not a WebServerApplicationContext, cannot retrieve port from it");
            }
        }

        return serverPort;
    }

    protected String resolveContextPath() {
        return environment.getProperty("server.servlet.context-path", "");
    }

    protected List<String> resolveActiveProfiles() {
        return List.of(environment.getActiveProfiles());
    }

    protected String resolveFullUrl(String protocol, String host, int port, String contextPath) {
        return String.format("%s://%s:%s%s", protocol, host, port, contextPath);
    }

    protected void createAppInfoFile() {
        JsonObject content = createJson();
        log.debug("Application info: {}", content);

        Path fileLocation = resolveAppInfoFileLocation();
        writeFile(fileLocation, content);
    }

    protected JsonObject createJson() {
        JsonObject general = new JsonObject();

        general.addProperty("host", host);
        general.addProperty("port", port);
        general.addProperty("contextPath", contextPath);
        general.addProperty("protocol", protocol);
        general.addProperty("fullUrl", fullUrl);
        general.add("activeProfiles", createActiveProfilesJsonArray());
        general.addProperty("startupTime", startupTime != null ? startupTime.toString() : null);

        JsonObject root = new JsonObject();
        root.add("general", general);

        return root;
    }

    protected void writeFile(Path fileLocation, JsonObject json) {
        try {
            Path parent = fileLocation.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            Files.writeString(
                    fileLocation,
                    gson.toJson(json),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            log.info("Application info stored to file: {}", fileLocation.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to store application info to file: {}", fileLocation.toAbsolutePath(), e);
        }
    }

    protected Path resolveAppInfoFileLocation() {
        String confDir = coreProperties.getConfDir();
        return Path.of(confDir, APP_INFO_DIR_NAME, APP_INFO_FILE_NAME);
    }

    protected JsonArray createActiveProfilesJsonArray() {
        JsonArray activeProfilesJsonArray = new JsonArray();
        if (activeProfiles != null) {
            activeProfiles.forEach(activeProfilesJsonArray::add);
        }
        return activeProfilesJsonArray;
    }

    protected void shutdownHookOperation() {
        log.debug("ApplicationInfoProvider shutdown hook executed");
        removeAppInfoFile();
    }

    protected void removeAppInfoFile() {
        Path fileLocation = resolveAppInfoFileLocation();

        try {
            Files.deleteIfExists(fileLocation);
            log.debug("Application info file removed: {}", fileLocation.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to remove application info file: {}", fileLocation.toAbsolutePath(), e);
        }
    }

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> shutdownHookOperation(), "application-info-provider-shutdown-hook"));
    }
}
