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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Local stand-in for a PlantUML server. Answers every request with a PNG and records the requested
 * paths, so tests can pin the URLs the engine actually calls. The status and the response delay can
 * be overridden to simulate a server that does not serve the requested path or is too slow.
 */
public class TestPlantUmlServer implements AutoCloseable {

    private static final byte[] PNG_CONTENT = new byte[]{(byte) 0x89, 'P', 'N', 'G'};

    private final HttpServer server;
    private final List<String> requestedPaths = new CopyOnWriteArrayList<>();

    private volatile Duration responseDelay = Duration.ZERO;
    private volatile int forcedStatus;

    private TestPlantUmlServer(HttpServer server) {
        this.server = server;
    }

    public static TestPlantUmlServer start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            TestPlantUmlServer plantUmlServer = new TestPlantUmlServer(server);
            server.createContext("/", plantUmlServer::handle);
            server.start();
            return plantUmlServer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        requestedPaths.add(exchange.getRequestURI().getPath());

        if (!responseDelay.isZero()) {
            try {
                Thread.sleep(responseDelay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int status = forcedStatus != 0 ? forcedStatus : 200;
        boolean bodyExpected = status == 200 && !"HEAD".equals(exchange.getRequestMethod());

        exchange.sendResponseHeaders(status, bodyExpected ? PNG_CONTENT.length : -1);
        if (bodyExpected) {
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(PNG_CONTENT);
            }
        }
        exchange.close();
    }

    public String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    public byte[] pngContent() {
        return PNG_CONTENT.clone();
    }

    public List<String> requestedPaths() {
        return List.copyOf(requestedPaths);
    }

    public String lastRequestedPath() {
        if (requestedPaths.isEmpty()) {
            throw new IllegalStateException("No requests received");
        }
        return requestedPaths.get(requestedPaths.size() - 1);
    }

    /**
     * Makes the server answer with the given status regardless of the requested path.
     */
    public void setForcedStatus(int forcedStatus) {
        this.forcedStatus = forcedStatus;
    }

    public void setResponseDelay(Duration responseDelay) {
        this.responseDelay = responseDelay;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
