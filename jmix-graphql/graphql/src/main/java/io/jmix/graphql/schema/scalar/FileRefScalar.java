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

package io.jmix.graphql.schema.scalar;

import graphql.language.ScalarTypeDefinition;
import graphql.schema.Coercing;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.DatatypeCoercing;
import io.jmix.graphql.schema.scalar.coercing.FileRefCoercing;
import io.jmix.graphql.service.FileService;

import java.time.LocalTime;
import java.util.List;

public class FileRefScalar extends GraphQLScalarType {

    public FileRefScalar(FileService fileService) {
        super("Upload", "Type for file uploading",
                new FileRefCoercing(fileService));
    }

}
