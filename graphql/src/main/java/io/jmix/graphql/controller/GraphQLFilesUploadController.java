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

package io.jmix.graphql.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import graphql.GraphQL;
import io.jmix.core.AccessManager;
import io.jmix.core.FileClientManager;
import io.jmix.core.FileInfoResponse;
import io.jmix.graphql.accesscontext.GraphQLAccessContext;
import io.jmix.graphql.service.FilePermissionService;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;
import io.leangen.graphql.spqr.spring.web.mvc.GraphQLMvcExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

@RestController("graphql_FilesController")
@CrossOrigin
public class GraphQLFilesUploadController extends GraphQLController<NativeWebRequest> {

    @Autowired
    public GraphQLFilesUploadController(GraphQL graphQL, GraphQLMvcExecutor executor) {
        super(graphQL, executor);
    }

    @Autowired
    protected FilePermissionService filePermissionService;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected FileClientManager fileClientManager;

    private static final Logger log = LoggerFactory.getLogger(GraphQLFilesUploadController.class);

    /**
     * For Requests that follow the GraphQL Multipart Request Spec from: https://github.com/jaydenseric/graphql-multipart-request-spec
     * <p>
     * The Request contains the following parts:
     * operations: JSON String with the GQL Query
     * map: Maps the multipart files to the variables of the GQL Query
     */
    @PostMapping(
            value = "${graphql.spqr.http.endpoint:/graphql}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    @ResponseBody
    public Object executeMultipartPost(@RequestPart("operations") String operations,
                                       @RequestPart("map") String map,
                                       @RequestPart(value = "storageName", required = false) String storageName,
                                       MultipartHttpServletRequest multiPartRequest,
                                       NativeWebRequest webRequest) throws Exception {
        checkFileUploadPermission();

        GraphQLRequest graphQLRequest = new ObjectMapper().readerFor(GraphQLRequest.class).readValue(operations);
        Map<String, ArrayList<String>> fileMap = new ObjectMapper().readerFor(Map.class).readValue(map);

        mapRequestFilesToVariables(multiPartRequest, graphQLRequest, fileMap, storageName);
        return this.executeJsonPost(graphQLRequest, new GraphQLRequest(null, null, null, null), webRequest);
    }

    /**
     * Maps the files that were sent in a Multipart Request to the corresponding variables of a {@link GraphQLRequest}.
     * This makes it possible to use a file input like a normal parameter in a GraphQLApi Method.
     */
    private void mapRequestFilesToVariables(MultipartHttpServletRequest multiPartRequest, GraphQLRequest graphQLRequest,
                                            Map<String, ArrayList<String>> fileMap, String storage) {
        for (Map.Entry<String, ArrayList<String>> pair : fileMap.entrySet()) {
            String targetVariable = pair.getValue().get(0);//.replace("variables.", "");
            try {
                JsonPath.read(multiPartRequest.getParameter("operations"), "$."+targetVariable);
                MultipartFile correspondingFile = multiPartRequest.getFileMap().get(pair.getKey());
                if (correspondingFile == null) {
                    throw new GraphQLControllerException("File upload failed",  "Field with name " + pair.getKey() +
                            " of multipart request is absent",
                            HttpStatus.BAD_REQUEST);
                }
                putValueIntoNestedMap(graphQLRequest.getVariables(), targetVariable.replace("variables.", ""),
                        new AbstractMap.SimpleEntry<>(storage, correspondingFile));
            } catch (PathNotFoundException e) {
                throw new GraphQLControllerException("Target file-variable not found", "Variable " + targetVariable +" not found",
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void putValueIntoNestedMap(Map map, String complexKey, Object value) {
        String key = complexKey.substring(0, complexKey.contains(".") ? complexKey.indexOf(".") : complexKey.length());
        String lastPart = complexKey.substring(complexKey.indexOf(".") + 1);

        if( !map.containsKey(key)) {
            throw new PathNotFoundException();
        }
        Object valueMap = map.get(key);
        if (valueMap instanceof Map){
            putValueIntoNestedMap((Map) valueMap, lastPart, value);
        } else {
            map.put(key, value);
        }
    }

    public void checkFileUploadPermission() {
        GraphQLAccessContext uploadContext = new GraphQLAccessContext(GraphQLAccessContext.GRAPHQL_FILE_UPLOAD_ENABLED);
        accessManager.applyRegisteredConstraints(uploadContext);

        if (!uploadContext.isPermitted()) {
            throw new AccessDeniedException("File upload failed. File upload is not permitted");
        }
    }


    /**
     * Method for simple file upload. File contents are placed in the request body. Optional file name parameter is
     * passed as a query param.
     */
    @PostMapping(path = "${graphql.spqr.http.endpoint:/graphql/files}", consumes = "!multipart/form-data")
    public ResponseEntity<FileInfoResponse> uploadFile(HttpServletRequest request,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String storageName) {
        filePermissionService.checkFileUploadPermission();
        return fileClientManager.fileUpload(name, storageName, request);
    }

    /**
     * Method for multipart file upload. It expects the file contents to be passed in the part called 'file'.
     */
    @PostMapping(path = "${graphql.spqr.http.endpoint:/graphql/files}", consumes = "multipart/form-data")
    public ResponseEntity<FileInfoResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String storageName,
                                               HttpServletRequest request) {
        filePermissionService.checkFileUploadPermission();
        return  fileClientManager.multipartFileUpload(file, name, storageName, request);
    }

}
