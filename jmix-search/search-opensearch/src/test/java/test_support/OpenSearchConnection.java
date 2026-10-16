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

package test_support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * The OpenSearch the engine tests talk to.
 * <p>
 * By default it is a container started once for the whole test run. Pass {@code -DsearchEngineUrl=...} to use an
 * engine that is already running instead — handy while writing a test, and the only way to run these tests where
 * Docker is not available.
 */
public class OpenSearchConnection {

    /**
     * Kept in step with the opensearch-java client of the BOM: a client talks only to a server of its own major
     * version.
     */
    protected static final String IMAGE = "opensearchproject/opensearch:3.2.0";

    protected static final int PORT = 9200;

    private static final Logger log = LoggerFactory.getLogger(OpenSearchConnection.class);

    private static final OpenSearchConnection INSTANCE = new OpenSearchConnection();

    protected final String url;

    @SuppressWarnings("resource")
    protected OpenSearchConnection() {
        String configuredUrl = System.getProperty("searchEngineUrl");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            log.info("Using the search engine running at {}", configuredUrl);
            url = configuredUrl;
        } else {
            GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(IMAGE))
                    .withExposedPorts(PORT)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                    .withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
                    .withEnv("OPENSEARCH_INITIAL_ADMIN_PASSWORD", "Str0ng!Passw0rd")
                    .waitingFor(Wait.forHttp("/").forPort(PORT).forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)));
            container.start();
            url = "http://" + container.getHost() + ":" + container.getMappedPort(PORT);
            log.info("Started the search engine container at {}", url);
        }
    }

    public static String getUrl() {
        return INSTANCE.url;
    }
}
