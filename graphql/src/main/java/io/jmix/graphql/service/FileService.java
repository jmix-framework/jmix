/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.service;

import com.google.common.base.Strings;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;

@Service("gql_FileService")
public class FileService {

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    public FileRef saveFileIntoStorage(MultipartFile multipartFile, FileStorage storage) throws IOException {
        return storage.saveStream(multipartFile.getName(), multipartFile.getInputStream());
    }

    public FileStorage getFileStorage(@Nullable String storageName) throws Exception {
        if (Strings.isNullOrEmpty(storageName)) {
            return fileStorageLocator.getDefault();
        } else {
            try {
                return fileStorageLocator.getByName(storageName);
            } catch (IllegalArgumentException e) {
                throw new Exception( String.format("Cannot find FileStorage with the given name: '%s'", storageName));
            }
        }
    }



}
