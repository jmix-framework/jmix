/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.controller;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.rest.accesscontext.RestFileUploadContext;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.service.filter.data.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

/**
 * REST API controller that is used for uploading files
 */
@RestController("rest_FileUploadController")
@RequestMapping(path = "/rest/files")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @Autowired
    protected AccessManager accessManager;

    /**
     * Method for simple file upload. File contents are placed in the request body. Optional file name parameter is
     * passed as a query param.
     */
    @PostMapping(consumes = "!multipart/form-data")
    public ResponseEntity<FileInfo> uploadFile(HttpServletRequest request,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String storageName) {
        checkFileUploadPermission();

        FileStorage fileStorage = getFileStorage(storageName);
        try {
            String contentLength = request.getHeader("Content-Length");

            long size = 0;
            try {
                size = Long.parseLong(contentLength);
            } catch (NumberFormatException ignored) {
            }

            ServletInputStream is = request.getInputStream();
            name = Objects.toString(name, "");
            FileRef fileRef = uploadToFileStorage(fileStorage, is, name);

            return createFileInfoResponseEntity(request, fileRef, name, size);
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RestAPIException("File upload failed", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * Method for multipart file upload. It expects the file contents to be passed in the part called 'file'.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String storageName,
                                               HttpServletRequest request) {
        checkFileUploadPermission();

        FileStorage fileStorage = getFileStorage(storageName);
        try {
            if (Strings.isNullOrEmpty(name)) {
                name = file.getOriginalFilename();
            }
            name = Objects.toString(name, "");

            long size = file.getSize();

            InputStream is = file.getInputStream();
            FileRef fileRef = uploadToFileStorage(fileStorage, is, name);

            return createFileInfoResponseEntity(request, fileRef, name, size);
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RestAPIException("File upload failed", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void checkFileUploadPermission() {
        RestFileUploadContext uploadContext = new RestFileUploadContext();
        accessManager.applyRegisteredConstraints(uploadContext);

        if (!uploadContext.isPermitted()) {
            throw new RestAPIException("File upload failed", "File upload is not permitted", HttpStatus.FORBIDDEN);
        }
    }

    protected FileStorage getFileStorage(@Nullable String storageName) {
        if (Strings.isNullOrEmpty(storageName)) {
            return fileStorageLocator.getDefault();
        } else {
            try {
                return fileStorageLocator.getByName(storageName);
            } catch (IllegalArgumentException e) {
                throw new RestAPIException("Invalid storage name",
                        String.format("Cannot find FileStorage with the given name: '%s'", storageName),
                        HttpStatus.BAD_REQUEST,
                        e);
            }
        }
    }

    protected ResponseEntity<FileInfo> createFileInfoResponseEntity(HttpServletRequest request,
                                                                    FileRef fileRef, String filename, long size) {
        FileInfo fileInfo = new FileInfo(fileRef.toString(), filename, size);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .queryParam("fileRef", URLEncodeUtils.encodeUtf8(fileRef.toString()))
                .buildAndExpand();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(uriComponents.toUriString()));
        return new ResponseEntity<>(fileInfo, httpHeaders, HttpStatus.CREATED);
    }

    protected FileRef uploadToFileStorage(FileStorage fileStorage, InputStream is, String fileName)
            throws FileStorageException {
        try {
            return fileStorage.saveStream(fileName, is);
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to upload file to FileStorage",
                    "Unable to upload file to FileStorage: " + fileName,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }
}
