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

import io.jmix.core.entity.FileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller that is used for downloading files by the {@link FileDescriptor} identifier
 */
@RestController("rest_FileDownloadController")
@RequestMapping("/rest/files")
public class FileDownloadController {
    //todo file storage
    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    // Using injection by name here, because an application project can define several instances
    // of ServerSelector type to work with different middleware blocks
//    @Resource(name = ServerSelector.NAME)
//    protected ServerSelector serverSelector;
//
//    @Autowired
//    protected UserSessionSource userSessionSource;
//
//    @Autowired
//    protected DataManager dataService;
//
//    @Autowired
//    protected FileLoader fileLoader;
//
//    @GetMapping("/{fileDescriptorId}")
//    public void downloadFile(@PathVariable String fileDescriptorId,
//                             @RequestParam(required = false) Boolean attachment,
//                             HttpServletResponse response) {
//        UUID uuid;
//        try {
//            uuid = UUID.fromString(fileDescriptorId);
//        } catch (IllegalArgumentException e) {
//            throw new RestAPIException("Invalid entity ID",
//                    String.format("Cannot convert %s into valid entity ID", fileDescriptorId),
//                    HttpStatus.BAD_REQUEST,
//                    e);
//        }
//        LoadContext<FileDescriptor> ctx = LoadContext.create(FileDescriptor.class).setId(uuid);
//        FileDescriptor fd = dataService.load(ctx);
//        if (fd == null) {
//            throw new RestAPIException("File not found", "File not found. Id: " + fileDescriptorId, HttpStatus.NOT_FOUND);
//        }
//
//        try {
//            response.setHeader("Cache-Control", "no-cache");
//            response.setHeader("Pragma", "no-cache");
//            response.setDateHeader("Expires", 0);
//            response.setHeader("Content-Type", getContentType(fd));
//            response.setHeader("Content-Disposition", (BooleanUtils.isTrue(attachment) ? "attachment" : "inline")
//                    + "; filename=\"" + fd.getName() + "\"");
//
//            downloadFromMiddlewareAndWriteResponse(fd, response);
//        } catch (Exception e) {
//            log.error("Error on downloading the file {}", fileDescriptorId, e);
//            throw new RestAPIException("Error on downloading the file", "", HttpStatus.INTERNAL_SERVER_ERROR, e);
//        }
//    }
//
//    protected void downloadFromMiddlewareAndWriteResponse(FileDescriptor fd, HttpServletResponse response) throws IOException {
//        ServletOutputStream os = response.getOutputStream();
//        try (InputStream is = fileLoader.openStream(fd)) {
//            IOUtils.copy(is, os);
//            os.flush();
//        } catch (FileStorageException e) {
//            throw new RestAPIException("Unable to download file from FileStorage",
//                    "Unable to download file from FileStorage: " + fd.getId(),
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    e);
//        }
//    }
//
//    protected String getContentType(FileDescriptor fd) {
//        if (StringUtils.isEmpty(fd.getExtension())) {
//            return FileTypesHelper.DEFAULT_MIME_TYPE;
//        }
//
//        return FileTypesHelper.getMIMEType("." + fd.getExtension().toLowerCase());
//    }
}
