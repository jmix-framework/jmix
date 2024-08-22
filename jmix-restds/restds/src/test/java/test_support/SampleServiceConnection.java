/*
 * Copyright 2024 Haulmont.
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
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

public class SampleServiceConnection {

    public static final String CLIENT_ID = "myclient";
    public static final String CLIENT_SECRET = "mysecret";

    private static final SampleServiceConnection INSTANCE = new SampleServiceConnection();

    private final boolean useStandaloneService;

    private GenericContainer container;

    public static SampleServiceConnection getInstance() {
        return INSTANCE;
    }

    public SampleServiceConnection() {
        Logger log = LoggerFactory.getLogger(SampleServiceConnection.class);
        useStandaloneService = Boolean.parseBoolean(System.getProperty("useStandaloneServiceForRestDsTests"));
        if (useStandaloneService) {
            log.info("Expecting sample-rest-service running on localhost:18080");
        } else {
            log.info("Using Testcontainers. Current dir: " + System.getProperty("user.dir"));
            container = new GenericContainer(
                        new ImageFromDockerfile("jmix-rest-ds/sample-rest-service", true)
                                .withDockerfile(Path.of("../sample-rest-service/Dockerfile"))
                    )
                    .withExposedPorts(18080)
                    .waitingFor(Wait.forLogMessage(".*Ready for testing.*\\n", 1));
            container.start();
        }
    }

    public String getHost() {
        return useStandaloneService ? "localhost" : container.getHost();
    }

    public int getPort() {
        return useStandaloneService ? 18080 : container.getFirstMappedPort();
    }

    public String getBaseUrl() {
        return "http://" + getHost() + ":" + getPort();
    }
}
