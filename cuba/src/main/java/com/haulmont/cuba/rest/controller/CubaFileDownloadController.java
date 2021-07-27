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

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import io.jmix.core.AccessManager;
import io.jmix.core.FileTypesHelper;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.core.security.EntityOp;
import io.jmix.rest.accesscontext.RestFileDownloadContext;
import io.jmix.rest.exception.RestAPIException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Legacy REST API controller that is used for downloading files by the {@link FileDescriptor} identifier
 */
@RestController("rest_CubaFileDownloadController")
@ConditionalOnClass(RestAPIException.class)
@RequestMapping("rest/cuba/files")
public class CubaFileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(CubaFileDownloadController.class);

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected DataManager dataService;

    @Autowired
    protected FileLoader fileLoader;

    @Autowired
    protected Security security;

    @GetMapping("/{fileDescriptorId}")
    public void downloadFile(@PathVariable String fileDescriptorId,
                             @RequestParam(required = false) Boolean attachment,
                             HttpServletResponse response) {

        checkFileDownloadPermission();

        UUID uuid;
        try {
            uuid = UUID.fromString(fileDescriptorId);
        } catch (IllegalArgumentException e) {
            throw new RestAPIException("Invalid entity ID",
                    String.format("Cannot convert %s into valid entity ID", fileDescriptorId),
                    HttpStatus.BAD_REQUEST,
                    e);
        }
        LoadContext<FileDescriptor> ctx = LoadContext
                .create(FileDescriptor.class).setId(uuid);
        FileDescriptor fd = dataService.load(ctx);
        if (fd == null) {
            throw new RestAPIException("File not found", "File not found. Id: " + fileDescriptorId, HttpStatus.NOT_FOUND);
        }

        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(fd));
            response.setHeader("Content-Disposition", (BooleanUtils.isTrue(attachment) ? "attachment" : "inline")
                    + "; filename=\"" + URLEncodeUtils.encodeUtf8(fd.getName()) + "\"");

            downloadFromMiddlewareAndWriteResponse(fd, response);
        } catch (Exception e) {
            log.error("Error on downloading the file {}", fileDescriptorId, e);
            throw new RestAPIException("Error on downloading the file", "", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void downloadFromMiddlewareAndWriteResponse(FileDescriptor fd, HttpServletResponse response) throws IOException {
        ServletOutputStream os = response.getOutputStream();
        try (InputStream is = fileLoader.openStream(fd)) {
            IOUtils.copy(is, os);
            os.flush();
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to download file from FileStorage",
                    "Unable to download file from FileStorage: " + fd.getId(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    protected String getContentType(FileDescriptor fd) {
        if (StringUtils.isEmpty(fd.getExtension())) {
            return FileTypesHelper.DEFAULT_MIME_TYPE;
        }

        return FileTypesHelper.getMIMEType("." + fd.getExtension().toLowerCase());
    }


    protected void checkFileDownloadPermission() {
        RestFileDownloadContext downloadContext = new RestFileDownloadContext();
        accessManager.applyRegisteredConstraints(downloadContext);

        if (!downloadContext.isPermitted()) {
            throw new RestAPIException("File download failed", "File download is not permitted", HttpStatus.FORBIDDEN);
        }

        if (!security.isEntityOpPermitted(FileDescriptor.class, EntityOp.READ)) {
            throw new RestAPIException("Reading forbidden",
                    "Reading of the sys$FileDescriptor is forbidden",
                    HttpStatus.FORBIDDEN);
        }
    }
}
