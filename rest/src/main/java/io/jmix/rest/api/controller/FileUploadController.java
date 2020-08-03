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

package io.jmix.rest.api.controller;

import com.google.common.base.Strings;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Metadata;
import io.jmix.rest.api.exception.RestAPIException;
import io.jmix.rest.api.service.filter.data.FileInfo;
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
import java.net.URISyntaxException;
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

    protected FileStorage<URI, String> fileStorage;

//    protected static final String FILE_UPLOAD_PERMISSION_NAME = "cuba.restApi.fileUpload.enabled";

    /**
     * Method for simple file upload. File contents are placed in the request body. Optional file name parameter is
     * passed as a query param.
     */
    @PostMapping(consumes = "!multipart/form-data")
    public ResponseEntity<FileInfo> uploadFile(HttpServletRequest request,
                                               @RequestParam(required = false) String ref,
                                               @RequestParam(required = false) String name) {
        checkFileUploadPermission();
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        checkFileExists(ref);
        try {
            String contentLength = request.getHeader("Content-Length");

            long size = 0;
            try {
                size = Long.parseLong(contentLength);
            } catch (NumberFormatException ignored) {
            }

            ServletInputStream is = request.getInputStream();
            URI fileReference = fileStorage.createReference(name);
            uploadToFileStorage(is, fileReference);

            return createFileInfoResponseEntity(request, fileReference, name, size);
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
                                               @RequestParam(required = false) String ref,
                                               @RequestParam(required = false) String name,
                                               HttpServletRequest request) {
        checkFileUploadPermission();
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        checkFileExists(ref);
        try {
            if (Strings.isNullOrEmpty(name)) {
                name = file.getOriginalFilename();
            }
            name = Objects.toString(name, "");

            long size = file.getSize();

            InputStream is = file.getInputStream();
            URI fileReference = fileStorage.createReference(name);
            uploadToFileStorage(is, fileReference);

            return createFileInfoResponseEntity(request, fileReference, name, size);
        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RestAPIException("File upload failed", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    //todo security
    protected void checkFileUploadPermission() {
//        UserSession userSession = userSessionSource.getUserSession();
//        if (!userSession.isSpecificPermitted(FILE_UPLOAD_PERMISSION_NAME)) {
//            log.warn(FILE_UPLOAD_PERMISSION_NAME + " is not permitted for user " + userSession.getUser().getLogin());
//            throw new RestAPIException("File upload failed", "File upload is not permitted", HttpStatus.FORBIDDEN);
//        }
    }
    protected void checkFileExists(@Nullable String ref) {
        if (Strings.isNullOrEmpty(ref)) {
            return;
        }
        URI fileReference;
        try {
            fileReference = new URI(ref);
            if (fileStorage.fileExists(fileReference)) {
                log.error("File by the ref: '{}' already exists", ref);
                throw new RestAPIException("File already exists",
                        String.format("File by the ref: '%s' already exists", ref),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            log.error("Provided file reference is not valid: {}", ref);
            throw new RestAPIException("Invalid file reference",
                    String.format("Cannot convert '%s' into valid file reference", ref),
                    HttpStatus.BAD_REQUEST);
        }
    }

    protected ResponseEntity<FileInfo> createFileInfoResponseEntity(HttpServletRequest request,
                                                                    URI fileReference, String filename, long size) {
        FileInfo fileInfo = new FileInfo(fileReference, filename, size);

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString())
                .path("/{fileReference}")
                .buildAndExpand(fileReference.toString());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(fileInfo, httpHeaders, HttpStatus.CREATED);
    }

    protected void uploadToFileStorage(InputStream is, URI fileReference) throws FileStorageException {
        try {
            fileStorage.saveStream(fileReference, is);
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to upload file to FileStorage",
                    "Unable to upload file to FileStorage: " + fileReference.toString(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }
}
