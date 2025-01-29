/*
 * Copyright 2025 Haulmont.
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

package io.jmix.restds.filestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.restds.exception.RestDataStoreAccessException;
import io.jmix.restds.util.RestDataStoreUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * File storage implementation that complements {@link io.jmix.restds.impl.RestDataStore} by the ability to store and load files
 * using the generic REST /files endpoints.
 * <p>
 * To use RestFileStorage, define the following bean in your application:
 * <pre>
 *     &#064;Bean
 *     FileStorage backendFileStorage() {
 *         return new RestFileStorage("backend", "fs");
 *     }
 * </pre>
 * In this example, 'backend' is the name of REST DataStore representing the remote application and 'fs' is the name of
 * the corresponding file storage in the remote application.
 * <p>
 * See {@link #RestFileStorage(String, String)} for more information.
 */
public class RestFileStorage implements FileStorage, InitializingBean {

    // Using field injection to simplify constructor for creating this bean in app projects
    @Autowired
    @SuppressWarnings("unused")
    private RestDataStoreUtils restDataStoreUtils;
    @Autowired
    @SuppressWarnings("unused")
    private Environment environment;

    private final String dataStoreName;
    private final String remoteStorageName;

    private String basePath;
    private String filesPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor.
     *
     * @param restDataStoreName name of the REST DataStore that represents the remote application
     * @param remoteStorageName name of the corresponding file storage in the remote application,
     *                          e.g. 'fs' for local file storage
     */
    public RestFileStorage(String restDataStoreName, String remoteStorageName) {
        this.dataStoreName = restDataStoreName;
        this.remoteStorageName = remoteStorageName;
    }

    @Override
    public void afterPropertiesSet() {
        basePath = environment.getProperty(dataStoreName + ".basePath", "/rest");
        filesPath = environment.getProperty(dataStoreName + ".filesPath", "/files");
    }

    @Override
    public String getStorageName() {
        return dataStoreName + "-" + remoteStorageName;
    }

    /**
     * @return name of the corresponding file storage in the remote application,
     * e.g. 'fs' for local file storage
     */
    public String getRemoteStorageName() {
        return remoteStorageName;
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream, Map<String, Object> parameters) {
        RestClient restClient = restDataStoreUtils.getRestClient(dataStoreName);

        String json = restClient.post()
                .uri(basePath + filesPath + "?name={fileName}", fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream))
                .retrieve()
                .body(String.class);

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
        JsonNode fileRefNode = rootNode.get("fileRef");
        if (fileRefNode == null)
            throw new IllegalStateException("fileRef property is not found in returned JSON: " + json);

        FileRef remoteFileRef = FileRef.fromString(fileRefNode.asText());
        return new FileRef(getStorageName(), remoteFileRef.getPath(), remoteFileRef.getFileName(), remoteFileRef.getParameters());
    }

    @Override
    public InputStream openStream(FileRef reference) {
        FileRef fileRef = new FileRef(getRemoteStorageName(), reference.getPath(), reference.getFileName());
        RestClient restClient = restDataStoreUtils.getRestClient(dataStoreName);
        try {
            return restClient.get()
                    .uri(basePath + filesPath + "?fileRef={fileRef}", fileRef.toString())
                    .exchange((request, response) -> {
                        if (response.getStatusCode().is2xxSuccessful()) {
                            return new ResponseInputStream(response, response.getBody());
                        } else {
                            throw new RuntimeException("Cannot download file '" + fileRef + "': " + response.getStatusCode());
                        }
                    }, false);
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    @Override
    public void removeFile(FileRef reference) {
        throw new UnsupportedOperationException("Generic REST does not support removing files");
    }

    @Override
    public boolean fileExists(FileRef reference) {
        FileRef fileRef = new FileRef(getRemoteStorageName(), reference.getPath(), reference.getFileName());
        RestClient restClient = restDataStoreUtils.getRestClient(dataStoreName);
        try {
            return restClient.get()
                    .uri(basePath + filesPath + "?fileRef={fileRef}", fileRef.toString())
                    .exchange((request, response) ->
                            response.getStatusCode().is2xxSuccessful());
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        }
    }

    public static class ResponseInputStream extends InputStream {

        private final InputStream inputStream;
        private final ClientHttpResponse response;

        public ResponseInputStream(ClientHttpResponse response, InputStream inputStream) {
            this.response = response;
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public void close() throws IOException {
            try {
                inputStream.close();
            } finally {
                response.close();
            }
        }
    }
}
