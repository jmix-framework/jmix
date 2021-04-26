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

package io.jmix.graphql.datafetcher;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import io.jmix.core.validation.EntityValidationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GqlEntityValidationException extends RuntimeException implements GraphQLError  {

    public static final String EXTENSION_CONSTRAINT_VIOLATIONS = "constraintViolations";

    public static final String EXTENSION_PERSISTENCE_ERROR_NAME = "persistenceError";

    private String clientMessage;

    public GqlEntityValidationException(EntityValidationException ex) {
        super(ex.getMessage(), ex);
    }

    public GqlEntityValidationException(String clientMessage) {
        super(clientMessage);
        this.clientMessage = clientMessage;
    }

    // used when we should not pass exception persistence details to client, so client message need to be changed
    public GqlEntityValidationException(Throwable ex, String clientMessage) {
        super(clientMessage, ex);
        this.clientMessage = clientMessage;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ValidationError;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Throwable cause = getCause();
        if (cause == null) return  Collections.emptyMap();

        Class<? extends Throwable> causeClass = cause.getClass();

        // bean validation
        if (causeClass.isAssignableFrom(EntityValidationException.class)) {

            EntityValidationException eve = (EntityValidationException) cause;
            List<Map<String, Object>> constraintViolationList = eve.getConstraintViolations().stream()
                    .map(cv -> composeErrorExtension(
                            cv.getMessageTemplate(), cv.getMessage(), "" + cv.getPropertyPath(), "" + cv.getInvalidValue())
                    ).collect(Collectors.toList());

            return Collections.singletonMap(EXTENSION_CONSTRAINT_VIOLATIONS, constraintViolationList);
        }

        // other validation such `integrity constraint violation: NOT NULL check constraint`
        if (clientMessage != null) {
            return Collections.singletonMap(EXTENSION_PERSISTENCE_ERROR_NAME, this.clientMessage);
        }

        return Collections.emptyMap();
    }


    protected Map<String, Object> composeErrorExtension(String messageTemplate, String message, String path, String invalidValue) {
        Map<String, Object> cv = new HashMap<>();
        cv.put("messageTemplate", messageTemplate);
        cv.put("message", message);
        cv.put("path", path);
        cv.put("invalidValue", invalidValue);
        return cv;
    }
}
