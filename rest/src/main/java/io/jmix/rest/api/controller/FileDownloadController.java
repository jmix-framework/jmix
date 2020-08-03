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

package io.jmix.rest.api.controller;

import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.FileTypesHelper;
import io.jmix.core.common.util.URLEncodeUtils;
import io.jmix.rest.api.exception.RestAPIException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST API controller that is used for downloading files
 */
@RestController("rest_FileDownloadController")
@RequestMapping("/rest/files")
public class FileDownloadController {

    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage<URI, String> fileStorage;

    @GetMapping("/**")
    public void downloadFile(@RequestParam(required = false) Boolean attachment,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }

        //parse file reference
        ResourceUrlProvider urlProvider = (ResourceUrlProvider) request
                .getAttribute(ResourceUrlProvider.class.getCanonicalName());
        String fileReferenceString = urlProvider.getPathMatcher().extractPathWithinPattern(
                String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)),
                String.valueOf(request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)));

        URI fileReference;
        try {
            fileReference = new URI(fileReferenceString);

            //check if a file by the given reference exists
            if (!fileStorage.fileExists(fileReference)) {
                throw new RestAPIException("File not found", "File not found. File reference: " +
                        fileReferenceString, HttpStatus.NOT_FOUND);
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new RestAPIException("Invalid file reference",
                    String.format("Cannot convert '%s' into valid file reference", fileReferenceString),
                    HttpStatus.BAD_REQUEST,
                    e);
        }

        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(fileReference));
            response.setHeader("Content-Type", "image/jpeg");

            String filename = fileStorage.getFileInfo(fileReference);
            String contentDisposition = BooleanUtils.isTrue(attachment) ? "attachment" : "inline";
            if (StringUtils.isNotEmpty(filename)) {
                contentDisposition += "; filename=\"" + URLEncodeUtils.encodeUtf8(filename) + "\"";
            }
            response.setHeader("Content-Disposition", contentDisposition);

            downloadAndWriteResponse(fileReference, response);
        } catch (Exception e) {
            log.error("Error on downloading the file {}", fileReferenceString, e);
            throw new RestAPIException("Error on downloading the file", "", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected void downloadAndWriteResponse(URI fileReference, HttpServletResponse response) throws IOException {
        ServletOutputStream os = response.getOutputStream();
        try (InputStream is = fileStorage.openStream(fileReference)) {
            IOUtils.copy(is, os);
            os.flush();
        } catch (FileStorageException e) {
            throw new RestAPIException("Unable to download file from FileStorage",
                    "Unable to download file from FileStorage: " + fileReference.toString(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    protected String getContentType(URI fileReference) {
        String fileName = fileStorage.getFileInfo(fileReference);
        String extension = FilenameUtils.getExtension(fileName);
        if (StringUtils.isEmpty(extension)) {
            return FileTypesHelper.DEFAULT_MIME_TYPE;
        }
        return FileTypesHelper.getMIMEType("." + extension.toLowerCase());
    }
}
