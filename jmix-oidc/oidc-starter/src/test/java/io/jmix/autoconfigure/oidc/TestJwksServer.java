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

package io.jmix.autoconfigure.oidc;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local JWK Set endpoint responding with a configurable delay. Signs test tokens with the RSA key
 * that the endpoint publishes.
 */
class TestJwksServer implements AutoCloseable {

    private final HttpServer server;
    private final RSAKey rsaKey;
    private final AtomicInteger requestCount = new AtomicInteger();

    private TestJwksServer(HttpServer server, RSAKey rsaKey) {
        this.server = server;
        this.rsaKey = rsaKey;
    }

    static TestJwksServer start(Duration responseDelay) {
        try {
            RSAKey rsaKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            TestJwksServer jwksServer = new TestJwksServer(server, rsaKey);
            byte[] jwks = new JWKSet(rsaKey.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
            server.createContext("/jwks", exchange -> {
                jwksServer.requestCount.incrementAndGet();
                try {
                    Thread.sleep(responseDelay.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jwks.length);
                try (OutputStream body = exchange.getResponseBody()) {
                    body.write(jwks);
                }
            });
            server.start();
            return jwksServer;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start test JWKS server", e);
        }
    }

    String getJwkSetUri() {
        return "http://localhost:" + server.getAddress().getPort() + "/jwks";
    }

    int getRequestCount() {
        return requestCount.get();
    }

    String signIdToken(String clientId) {
        return sign(new JWTClaimsSet.Builder()
                .issuer("https://provider.example.com")
                .subject("user1")
                .audience(clientId)
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .issueTime(Date.from(Instant.now().minusSeconds(10)))
                .build());
    }

    String signAccessToken() {
        return sign(new JWTClaimsSet.Builder()
                .subject("user1")
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .issueTime(Date.from(Instant.now().minusSeconds(10)))
                .build());
    }

    private String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claims);
            jwt.sign(new RSASSASigner(rsaKey));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign test token", e);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
