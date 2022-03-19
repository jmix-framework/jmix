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

package io.jmix.rest.impl.openapi;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * This bean generates OpenAPI documentation according to the 3.1 specification.
 * <p>
 * Generated documentation includes operations with entities, predefined REST queries and exposed services.
 */
public interface OpenAPIGenerator {

    /**
     * @return a {@code OpenAPI} object that can be transformed to JSON or YAML version of documentation
     */
    OpenAPI generateOpenAPI();
}
