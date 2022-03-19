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

package com.haulmont.cuba.rest.controller;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.rest.data.CubaFileInfo;
import io.jmix.core.AccessManager;
import io.jmix.core.TimeSource;
import io.jmix.core.security.EntityOp;
import io.jmix.rest.accesscontext.RestFileUploadContext;
import io.jmix.rest.exception.RestAPIException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
import java.util.Collections;
import java.util.UUID;

/**
 * Legacy REST API controller that is used for uploading files by means of the {@link FileDescriptor} identifier
 */
@RestController("rest_CubaFileUploadController")
@RequestMapping("rest/cuba/files")
@ConditionalOnClass(RestAPIException.class)
public class CubaFileUploadController {

    private static final Logger log = LoggerFactory.getLogger(CubaFileUploadController.class);

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected FileLoader fileLoader;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Security security;

    /**
     * Method for simple file upload. File contents are placed in the request body. Optional file name parameter is
     * passed as a query param.
     */
    @PostMapping(consumes = "!multipart/form-data")
    public ResponseEntity<CubaFileInfo> uploadFile(HttpServletRequest request,
                                                   @RequestParam(required = false) String id,
                                                   @RequestParam(required = false) String name) {
        checkFileUploadPermission();
        try {
            checkFileExists(id);
            String contentLength = request.getHeader("Content-Length");

            long size = 0;
            try {
                size = Long.parseLong(contentLength);
            } catch (NumberFormatException ignored) {
            }
            FileDescriptor fd = createFileDescriptor(id, name, size);

            ServletInputStream is = request.getInputStream();
            uploadToMiddleware(is, fd);
            saveFileDescriptor(fd);

            return createFileInfoResponseEntity(request, fd);
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RestAPIException("File upload failed", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void checkFileExists(@Nullable String id) {
        if (Strings.isNullOrEmpty(id)) {
            return;
        }
        LoadContext<FileDescriptor> ctx = new LoadContext<>(FileDescriptor.class)
                .setId(UUID.fromString(id));
        FileDescriptor fileDescriptor = dataManager.load(ctx);

        if (fileDescriptor != null) {
            log.error("File with id = {} already exists", id);
            throw new RestAPIException("File already exists",
                    String.format("File with id = %s already exists", id),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Method for multipart file upload. It expects the file contents to be passed in the part called 'file'
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CubaFileInfo> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String id,
                                               @RequestParam(required = false) String name,
                                               HttpServletRequest request) {
        checkFileUploadPermission();
        try {
            checkFileExists(id);
            if (Strings.isNullOrEmpty(name)) {
                name = file.getOriginalFilename();
            }

            long size = file.getSize();
            FileDescriptor fd = createFileDescriptor(id, name, size);

            InputStream is = file.getInputStream();
            uploadToMiddleware(is, fd);
            saveFileDescriptor(fd);

            return createFileInfoResponseEntity(request, fd);
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

        if (!security.isEntityOpPermitted(FileDescriptor.class, EntityOp.CREATE)) {
            throw new RestAPIException("Creating forbidden",
                    "Creating of the sys$FileDescriptor is forbidden",
                    HttpStatus.FORBIDDEN);
        }
    }

    protected ResponseEntity<CubaFileInfo> createFileInfoResponseEntity(HttpServletRequest request, FileDescriptor fd) {
        CubaFileInfo fileInfo = new CubaFileInfo(fd.getId(), fd.getName(), fd.getSize());

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .path("/{id}")
                .buildAndExpand(fd.getId().toString());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(fileInfo, httpHeaders, HttpStatus.CREATED);
    }

    protected void saveFileDescriptor(FileDescriptor fd) {
        CommitContext commitContext = new CommitContext(Collections.singleton(fd));
        dataManager.commit(commitContext);
    }

    protected FileDescriptor createFileDescriptor(@Nullable String id, @Nullable String fileName, long size) {
        FileDescriptor fd = metadata.create(FileDescriptor.class);
        if (!Strings.isNullOrEmpty(id)) {
            fd.setId(UUID.fromString(id));
        }
        if (Strings.isNullOrEmpty(fileName)) {
            fileName = fd.getId().toString();
        }
        fd.setName(fileName);
        fd.setExtension(FilenameUtils.getExtension(fileName));
        fd.setSize(size);
        fd.setCreateDate(timeSource.currentTimestamp());
        return fd;
    }

    protected void uploadToMiddleware(InputStream is, FileDescriptor fd) throws FileStorageException {
        try {
            fileLoader.saveStream(fd, new FileLoader.SingleInputStreamSupplier(is));
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to upload file to FileStorage",
                    "Unable to upload file to FileStorage: " + fd.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }
}
