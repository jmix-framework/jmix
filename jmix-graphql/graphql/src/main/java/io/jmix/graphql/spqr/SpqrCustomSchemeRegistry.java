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

package io.jmix.graphql.spqr;

import graphql.schema.GraphQLFieldDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Component that stores a list of custom graphQL operation names. Used to implement a protection mechanism.
 */
@Component("ggc_SpqrCustomSchemeRegistry")
public class SpqrCustomSchemeRegistry {
    private List<GraphQLFieldDefinition> operations = new CopyOnWriteArrayList<>();

    public void addOperations(List<GraphQLFieldDefinition> operations) {
        this.operations.addAll(operations);
    }

    public boolean isCustomOperation(String operationName) {
        for (GraphQLFieldDefinition operation : operations) {
            if (operation.getName().equals(operationName)) {
                return true;
            }
        }
        return false;
    }
}
