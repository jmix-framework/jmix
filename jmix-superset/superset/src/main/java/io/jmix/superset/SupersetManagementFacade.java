/*
 * Copyright 2024 Haulmont.
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

package io.jmix.superset;

import io.jmix.superset.schedule.SupersetTokenManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@ManagedResource(description = "Manages Superset tokens", objectName = "jmix.superset:type=SupersetTokenManager")
@Component("superset_SupersetManagementFacade")
public class SupersetManagementFacade {

    private final SupersetTokenManager tokenManager;

    public SupersetManagementFacade(SupersetTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @ManagedOperation(description = "Refreshes an access token")
    public String refreshAccessToken() {
        tokenManager.refreshAccessToken();
        return "Refreshing access token is successfully invoked";
    }
}
