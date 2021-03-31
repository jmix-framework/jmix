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

import java.util.*;
import java.util.stream.Collectors;

public class GqlEntityValidationException extends RuntimeException implements GraphQLError  {

    public static final String EXTENSION_CONSTRAINT_VIOLATIONS = "constraintViolations";

    public GqlEntityValidationException(EntityValidationException eve) {
        super(eve.getMessage(), eve);
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
        EntityValidationException eve = (EntityValidationException) getCause();

        List<Map<String, Object>> constraintViolationList = eve.getConstraintViolations().stream()
                .map(constraintViolation -> {
                    Map<String, Object> cv = new HashMap<>();
                    cv.put("messageTemplate", constraintViolation.getMessageTemplate());
                    cv.put("message", constraintViolation.getMessage());
                    cv.put("path", constraintViolation.getPropertyPath().toString());
                    cv.put("invalidValue", constraintViolation.getInvalidValue());
                    return cv;
                }).collect(Collectors.toList());

        return Collections.singletonMap(EXTENSION_CONSTRAINT_VIOLATIONS, constraintViolationList);
    }
}
