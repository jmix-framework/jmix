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

package data_model_diagram;

import io.jmix.core.CoreProperties;
import io.jmix.core.Metadata;
import io.jmix.datatools.DatatoolsProperties;
import io.jmix.datatools.datamodel.EngineType;
import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.engine.plantuml.impl.PlantUmlDiagramEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test_support.TestPlantUmlServer;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlantUmlDiagramEngineTest {

    static final String DEFAULT_PATH = "plantuml";
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10);

    TestPlantUmlServer server;

    @BeforeEach
    void setUp() {
        server = TestPlantUmlServer.start();
    }

    @AfterEach
    void tearDown() {
        server.close();
    }

    @Test
    void pingService_respondsWithNotFound_returnsTrue() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);
        server.setForcedStatus(404);

        assertTrue(engine.pingService());
    }

    @Test
    void pingService_respondsWithNotAcceptable_returnsTrue() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);
        server.setForcedStatus(406);

        assertTrue(engine.pingService());
    }

    @Test
    void pingService_serverUnreachable_returnsFalse() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);
        server.close();

        assertFalse(engine.pingService());
    }

    @Test
    void pingService_slowerThanReadTimeout_returnsFalse() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH, Duration.ofMillis(200));
        server.setResponseDelay(Duration.ofMillis(800));

        assertFalse(engine.pingService());
    }

    @Test
    void pingService_defaultPath_pingsPathPrefix() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);

        engine.pingService();

        assertEquals("/plantuml/", server.lastRequestedPath());
    }

    @Test
    void pingService_emptyPath_pingsHostRoot() {
        DiagramEngine engine = createEngine(server.baseUrl(), "");

        engine.pingService();

        assertEquals("/", server.lastRequestedPath());
    }

    @Test
    void generateDiagram_defaultPath_requestsPngEndpointUnderPathPrefix() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);

        engine.generateDiagram("entity Customer {\n}\n", "");

        assertTrue(server.lastRequestedPath().startsWith("/plantuml/png/"),
                "Diagram requested from " + server.lastRequestedPath());
    }

    @Test
    void generateDiagram_emptyPath_requestsPngEndpointAtHostRoot() {
        DiagramEngine engine = createEngine(server.baseUrl(), "");

        engine.generateDiagram("entity Customer {\n}\n", "");

        assertTrue(server.lastRequestedPath().startsWith("/png/"),
                "Diagram requested from " + server.lastRequestedPath());
    }

    @Test
    void generateDiagram_hostWithPathAndEmptyPath_requestsPngEndpointUnderHostPath() {
        DiagramEngine engine = createEngine(server.baseUrl() + "/plantuml", "");

        engine.generateDiagram("entity Customer {\n}\n", "");

        assertTrue(server.lastRequestedPath().startsWith("/plantuml/png/"),
                "Diagram requested from " + server.lastRequestedPath());
    }

    @Test
    void generateDiagram_hostWithPathAndDefaultPath_appendsPathPrefixToHostPath() {
        DiagramEngine engine = createEngine(server.baseUrl() + "/plantuml", DEFAULT_PATH);

        engine.generateDiagram("entity Customer {\n}\n", "");

        assertTrue(server.lastRequestedPath().startsWith("/plantuml/plantuml/png/"),
                "Diagram requested from " + server.lastRequestedPath());
    }

    @Test
    void generateDiagram_pathWithSurroundingSlashes_normalizesPathPrefix() {
        DiagramEngine engine = createEngine(server.baseUrl(), "/plantuml/");

        engine.generateDiagram("entity Customer {\n}\n", "");

        assertTrue(server.lastRequestedPath().startsWith("/plantuml/png/"),
                "Diagram requested from " + server.lastRequestedPath());
    }

    @Test
    void generateDiagram_serviceRespondsWithImage_returnsReceivedImage() {
        DiagramEngine engine = createEngine(server.baseUrl(), DEFAULT_PATH);

        byte[] diagram = engine.generateDiagram("entity Customer {\n}\n", "");

        assertArrayEquals(server.pngContent(), diagram);
    }

    DiagramEngine createEngine(String host, String path) {
        return createEngine(host, path, DEFAULT_READ_TIMEOUT);
    }

    DiagramEngine createEngine(String host, String path, Duration readTimeout) {
        Metadata metadata = mock(Metadata.class);
        when(metadata.getClasses()).thenReturn(List.of());

        DatatoolsProperties.DataModelDiagram dataModelDiagram = new DatatoolsProperties.DataModelDiagram(
                host, path, EngineType.PLANTUML, true, readTimeout);

        return new PlantUmlDiagramEngine(new DatatoolsProperties(dataModelDiagram), metadata,
                mock(CoreProperties.class));
    }
}
