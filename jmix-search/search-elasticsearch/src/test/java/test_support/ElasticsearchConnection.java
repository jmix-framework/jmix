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
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The Elasticsearch the engine tests talk to.
 * <p>
 * By default it is a container started once for the whole test run. Pass {@code -DsearchEngineUrl=...} to use an
 * engine that is already running instead — handy while writing a test, and the only way to run these tests where
 * Docker is not available.
 */
public class ElasticsearchConnection {

    /**
     * Kept in step with the elasticsearch-java client of the BOM: a client talks only to a server of its own
     * major version, an older one answers its requests with 400.
     */
    protected static final String IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:9.4.1";

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchConnection.class);

    private static final ElasticsearchConnection INSTANCE = new ElasticsearchConnection();

    protected final String url;

    protected ElasticsearchConnection() {
        String configuredUrl = System.getProperty("searchEngineUrl");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            log.info("Using the search engine running at {}", configuredUrl);
            url = configuredUrl;
        } else {
            ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(IMAGE))
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node");
            container.start();
            url = "http://" + container.getHttpHostAddress();
            log.info("Started the search engine container at {}", url);
        }
    }

    public static String getUrl() {
        return INSTANCE.url;
    }
}
