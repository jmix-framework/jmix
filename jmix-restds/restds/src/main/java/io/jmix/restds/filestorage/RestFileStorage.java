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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * File storage implementation that complements {@link io.jmix.restds.impl.RestDataStore} by the ability to store and load files
 * using the generic REST /files endpoints.
 * <p>
 * To use RestFileStorage, define the bean as follows:
 * <pre>
 *     &#064;Bean
 *     FileStorage backendFileStorage() {
 *         return new RestFileStorage("backend", "fs");
 *     }
 * </pre>
 * See {@link #RestFileStorage(String, String)} for more information.
 */
public class RestFileStorage implements FileStorage {

    // Using field injection to simplify constructor creating this bean in app projects
    @SuppressWarnings("unused")
    @Autowired
    private RestDataStoreUtils restDataStoreUtils;

    private final String dataStoreName;
    private final String remoteStorageName;

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
    public String getStorageName() {
        return dataStoreName + "-fs";
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
                .uri("/rest/files?name={fileName}", fileName)
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
            Resource resource = restClient.get()
                    .uri("/rest/files?fileRef={fileRef}", fileRef.toString())
                    .retrieve()
                    .body(Resource.class);
            if (resource != null) {
                return resource.getInputStream();
            } else {
                throw new RuntimeException("Cannot download file: response is null");
            }
        } catch (ResourceAccessException e) {
            throw new RestDataStoreAccessException(dataStoreName, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFile(FileRef reference) {
        throw new UnsupportedOperationException("Generic REST does not support removing files");
    }

    @Override
    public boolean fileExists(FileRef reference) {
        throw new UnsupportedOperationException("Generic REST cannot check if a file exists. Use openStream() method.");
    }
}
