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

import io.jmix.core.AccessManager;
import io.jmix.core.FileTransferService;
import io.jmix.core.FileInfoResponse;
import io.jmix.core.Metadata;
import io.jmix.rest.accesscontext.RestFileUploadContext;
import io.jmix.rest.exception.RestAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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
    protected FileTransferService fileTransferService;

    @Autowired
    protected AccessManager accessManager;
    /**
     * Method for simple file upload. File contents are placed in the request body. Optional file name parameter is
     * passed as a query param.
     */
    @PostMapping(consumes = "!multipart/form-data")
    public ResponseEntity<FileInfoResponse> uploadFile(HttpServletRequest request,
                                                       @RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String storageName) {
        checkFileUploadPermission();
        return fileTransferService.fileUpload(name, storageName, request);
    }

    /**
     * Method for multipart file upload. It expects the file contents to be passed in the part called 'file'.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileInfoResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String storageName,
                                                       HttpServletRequest request) {
        checkFileUploadPermission();
        return fileTransferService.multipartFileUpload(file, name, storageName, request);
    }

    protected void checkFileUploadPermission() {
        RestFileUploadContext uploadContext = new RestFileUploadContext();
        accessManager.applyRegisteredConstraints(uploadContext);

        if (!uploadContext.isPermitted()) {
            throw new RestAPIException("File upload failed", "File upload is not permitted", HttpStatus.FORBIDDEN);
        }
    }
}
