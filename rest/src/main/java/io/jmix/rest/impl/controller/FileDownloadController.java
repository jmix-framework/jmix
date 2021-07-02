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

import io.jmix.core.*;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.rest.accesscontext.RestFileDownloadContext;
import io.jmix.rest.exception.RestAPIException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * REST API controller that is used for downloading files
 */
@RestController("rest_FileDownloadController")
@RequestMapping("/rest/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected AccessManager accessManager;

    @GetMapping
    public void downloadFile(@RequestParam String fileRef,
                             @RequestParam(required = false) Boolean attachment,
                             HttpServletResponse response) {

        checkFileDownloadPermission();
        FileRef fileReference;
        try {
            fileReference = FileRef.fromString(fileRef);
        } catch (IllegalArgumentException e) {
            throw new RestAPIException("Invalid file reference",
                    String.format("Cannot convert '%s' into valid file reference", fileRef),
                    HttpStatus.BAD_REQUEST,
                    e);
        }

        FileStorage fileStorage;
        try {
            fileStorage = fileStorageLocator.getByName(fileReference.getStorageName());
        } catch (IllegalArgumentException e) {
            throw new RestAPIException("Invalid file reference",
                    String.format("Cannot find FileStorage for the given FileRef: '%s'", fileRef),
                    HttpStatus.BAD_REQUEST,
                    e);
        }

        //check if a file by the given reference exists
        if (!fileStorage.fileExists(fileReference)) {
            throw new RestAPIException("File not found", "File not found. File reference: " +
                    fileRef, HttpStatus.NOT_FOUND);
        }

        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(fileReference));

            String filename = fileReference.getFileName();
            String contentDisposition = BooleanUtils.isTrue(attachment) ? "attachment" : "inline";
            if (StringUtils.isNotEmpty(filename)) {
                contentDisposition += "; filename=\"" + URLEncodeUtils.encodeUtf8(filename) + "\"";
            }
            response.setHeader("Content-Disposition", contentDisposition);

            downloadAndWriteResponse(fileStorage, fileReference, response);
        } catch (Exception e) {
            log.error("Error on downloading the file {}", fileRef, e);
            throw new RestAPIException("Error on downloading the file", "", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void downloadAndWriteResponse(FileStorage fileStorage, FileRef fileRef,
                                            HttpServletResponse response) throws IOException {
        ServletOutputStream os = response.getOutputStream();
        try (InputStream is = fileStorage.openStream(fileRef)) {
            IOUtils.copy(is, os);
            os.flush();
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to download file from FileStorage",
                    "Unable to download file from FileStorage: " + fileRef.toString(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    protected String getContentType(FileRef fileRef) {
        String extension = FilenameUtils.getExtension(fileRef.getFileName());
        if (StringUtils.isEmpty(extension)) {
            return FileTypesHelper.DEFAULT_MIME_TYPE;
        }
        return FileTypesHelper.getMIMEType("." + extension.toLowerCase());
    }

    protected void checkFileDownloadPermission() {
        RestFileDownloadContext downloadContext = new RestFileDownloadContext();
        accessManager.applyRegisteredConstraints(downloadContext);

        if (!downloadContext.isPermitted()) {
            throw new RestAPIException("File download failed", "File download is not permitted", HttpStatus.FORBIDDEN);
        }
    }
}
