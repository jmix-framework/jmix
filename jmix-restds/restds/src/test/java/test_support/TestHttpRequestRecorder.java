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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Records the HTTP requests sent by REST clients built from the {@link RestClient.Builder} bean.
 * <p>
 * Declare it as a {@code static @Bean} in a test configuration, so it is registered before the builder is created.
 */
@NullMarked
public class TestHttpRequestRecorder implements BeanPostProcessor, ClientHttpRequestInterceptor {

    private final List<RecordedRequest> requests = new CopyOnWriteArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RestClient.Builder builder) {
            builder.requestInterceptor(this);
        }
        return bean;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        requests.add(new RecordedRequest(request.getMethod(), request.getURI(),
                new String(body, StandardCharsets.UTF_8)));
        return execution.execute(request, body);
    }

    /**
     * Returns the recorded requests to the entities endpoint, oldest first.
     */
    public List<RecordedRequest> getEntityRequests() {
        return requests.stream()
                .filter(request -> request.uri().getPath().contains("/entities/"))
                .toList();
    }

    public RecordedRequest getLastEntityRequest() {
        List<RecordedRequest> entityRequests = getEntityRequests();
        if (entityRequests.isEmpty()) {
            throw new IllegalStateException("No requests to the entities endpoint were recorded");
        }
        return entityRequests.get(entityRequests.size() - 1);
    }

    public void clear() {
        requests.clear();
    }

    public record RecordedRequest(HttpMethod method, URI uri, String body) {

        /**
         * Returns the decoded value of a query parameter, or null if the URI does not have it.
         */
        @Nullable
        public String queryParam(String name) {
            String value = UriComponentsBuilder.fromUri(uri).build(true).getQueryParams().getFirst(name);
            return value == null ? null : UriUtils.decode(value, StandardCharsets.UTF_8);
        }
    }
}
