/*
 * Copyright 2020 Haulmont.
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

import io.jmix.core.common.util.URLEncodeUtils;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A reference pointing to a file located in {@link FileStorage}.
 */
public class FileRef implements Serializable {
    private final String storageName;
    private final String path;
    private final String fileName;

    private Map<String, String> parameters;

    public static FileRef create(String storageName, String path, String fileName) {
        return new FileRef(storageName, path, fileName);
    }

    public FileRef(String storageName, String path, String fileName, Map<String, String> parameters) {
        this(storageName, path, fileName);
        this.parameters = new LinkedHashMap<>(parameters);
    }

    public FileRef(String storageName, String path, String fileName) {
        this.storageName = storageName;
        this.path = path;
        this.fileName = fileName;
    }

    public String getStorageName() {
        return storageName;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public Map<String, String> getParameters() {
        if (parameters == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(parameters);
        }
    }

    public void addParameter(String key, String value) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        parameters.put(key, value);
    }

    public static FileRef fromString(String fileRefString) {
        URI fileRefUri;
        try {
            fileRefUri = new URI(fileRefString);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot convert " + fileRefString + " to FileRef", e);
        }
        String storageName = fileRefUri.getScheme();
        String path = fileRefUri.getHost() + fileRefUri.getPath();
        String[] params = fileRefUri.getQuery().split("&");
        String fileName = URLEncodeUtils.decodeUtf8(params[0].split("=", -1)[1]);
        if (params.length > 1) {
            Map<String, String> paramsMap = new LinkedHashMap<>();
            for (int i = 1; i < params.length; i++) {
                String[] paramPair = params[i].split("=", -1);
                paramsMap.put(paramPair[0], URLEncodeUtils.decodeUtf8(paramPair[1]));
            }
            return new FileRef(storageName, path, fileName, paramsMap);
        }
        return new FileRef(storageName, path, fileName);
    }

    @Override
    public String toString() {
        StringBuilder uriStringBuilder = new StringBuilder();
        uriStringBuilder.append(storageName)
                .append("://")
                .append(path)
                .append("?name=")
                .append(URLEncodeUtils.encodeUtf8(fileName));
        if (parameters != null) {
            parameters.forEach((key, value) ->
                    uriStringBuilder.append("&").append(key).append("=").append(URLEncodeUtils.encodeUtf8(value)));
        }
        return uriStringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRef fileRef = (FileRef) o;
        return Objects.equals(storageName, fileRef.storageName) &&
                Objects.equals(path, fileRef.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageName, path);
    }
}
