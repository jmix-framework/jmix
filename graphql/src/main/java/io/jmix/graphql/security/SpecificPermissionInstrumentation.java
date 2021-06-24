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

package io.jmix.graphql.security;

import graphql.execution.AbortExecutionException;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.graphql.InstrumentationUtils;
import io.jmix.graphql.accesscontext.GraphQLAccessContext;

import static io.jmix.graphql.accesscontext.GraphQLAccessContext.GRAPHQL_ENABLED;

public class SpecificPermissionInstrumentation extends SimpleInstrumentation {

    private final AccessManager accessManager;
    private final Messages messages;

    public SpecificPermissionInstrumentation(AccessManager accessManager, Messages messages) {
        this.accessManager = accessManager;
        this.messages = messages;
    }

    @Override
    public InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
        GraphQLAccessContext accessContext = new GraphQLAccessContext(GRAPHQL_ENABLED);
        accessManager.applyRegisteredConstraints(accessContext);

        if (!InstrumentationUtils.isIntrospectionQuery(parameters.getExecutionInput()) && !accessContext.isPermitted()) {
            throw new AbortExecutionException(messages.getMessage("io.jmix.graphql/gqlApiAccessDenied"));
        }

        return super.createState();
    }
}
