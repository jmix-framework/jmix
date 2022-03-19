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

package io.jmix.rest.impl.controller;

import io.jmix.core.AccessManager;
import io.jmix.core.FileTransferService;
import io.jmix.core.FileRef;
import io.jmix.core.Metadata;
import io.jmix.rest.accesscontext.RestFileDownloadContext;
import io.jmix.rest.exception.RestAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * REST API controller that is used for downloading files
 */
@RestController("rest_FileDownloadController")
@RequestMapping("/rest/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected FileTransferService fileTransferService;

    @GetMapping
    public void downloadFile(@RequestParam String fileRef,
                             @RequestParam(required = false) Boolean attachment,
                             HttpServletResponse response) {

        checkFileDownloadPermission();
        try {
            FileRef fileReference;
            fileReference = FileRef.fromString(fileRef);
            fileTransferService.downloadAndWriteResponse(fileReference, fileReference.getStorageName(), attachment, response);
        } catch (IllegalArgumentException e) {
            throw new RestAPIException("Invalid file reference",
                    String.format("Cannot convert '%s' into valid file reference", fileRef),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
    }

    protected void checkFileDownloadPermission() {
        RestFileDownloadContext downloadContext = new RestFileDownloadContext();
        accessManager.applyRegisteredConstraints(downloadContext);

        if (!downloadContext.isPermitted()) {
            throw new RestAPIException("File download failed", "File download is not permitted", HttpStatus.FORBIDDEN);
        }
    }
}
