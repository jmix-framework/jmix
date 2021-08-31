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

package io.jmix.graphql.service;

import io.jmix.core.AccessManager;
import io.jmix.core.accesscontext.SpecificOperationAccessContext;
import io.jmix.graphql.accesscontext.GraphQLAccessContext;
import io.jmix.graphql.controller.GraphQLControllerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service("gql_FilePermissionService")
public class FilePermissionService {

    @Autowired
    protected AccessManager accessManager;

    public void checkFileUploadPermission() {
        GraphQLAccessContext uploadContext = new GraphQLAccessContext(GraphQLAccessContext.GRAPHQL_FILE_UPLOAD_ENABLED);
        accessManager.applyRegisteredConstraints(uploadContext);

        if (!uploadContext.isPermitted()) {
            throw new AccessDeniedException("File upload failed. File upload is not permitted");
        }
    }

    public void checkFileDownloadPermission() {
        GraphQLAccessContext downloadContext =
                new GraphQLAccessContext(GraphQLAccessContext.GRAPHQL_FILE_DOWNLOAD_ENABLED);
        accessManager.applyRegisteredConstraints(downloadContext);

        if (!downloadContext.isPermitted()) {
            throw new GraphQLControllerException("File download failed", "File download is not permitted", HttpStatus.FORBIDDEN);
        }
    }
}
