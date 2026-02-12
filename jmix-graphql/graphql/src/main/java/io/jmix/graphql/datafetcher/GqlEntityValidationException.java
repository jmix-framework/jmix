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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.validation.EntityValidationException;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GqlEntityValidationException extends RuntimeException implements GraphQLError {

    public static final String EXTENSION_CONSTRAINT_VIOLATIONS = "constraintViolations";

    public static final String EXTENSION_PERSISTENCE_ERROR_NAME = "persistenceError";

    private String clientMessage;
    private Object parentEntity;
    private MetaClass parentClass;

    public GqlEntityValidationException(EntityValidationException ex, Object parentEntity, MetaClass parentClass) {
        super(ex.getMessage(), ex);
        this.parentEntity = parentEntity;
        this.parentClass = parentClass;
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
        if (cause == null) return Collections.emptyMap();

        Class<? extends Throwable> causeClass = cause.getClass();

        // bean validation
        if (causeClass.isAssignableFrom(EntityValidationException.class)) {

            EntityValidationException eve = (EntityValidationException) cause;
            List<Map<String, Object>> constraintViolationList = eve.getConstraintViolations().stream()
                    .map(this::composeErrorExtension)
                    .collect(Collectors.toList());

            return Collections.singletonMap(EXTENSION_CONSTRAINT_VIOLATIONS, constraintViolationList);
        }

        // other validation such `integrity constraint violation: NOT NULL check constraint`
        if (clientMessage != null) {
            return Collections.singletonMap(EXTENSION_PERSISTENCE_ERROR_NAME, this.clientMessage);
        }

        return Collections.emptyMap();
    }

    protected Map<String, Object> composeErrorExtension(ConstraintViolation<?> cv) {
        String pathStr = buildFullPath(parentEntity, parentClass, cv, "", new HashSet<>())
                .findAny()
                .orElseGet(() -> "" + cv.getPropertyPath());

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("messageTemplate", cv.getMessageTemplate());
        errorMap.put("message", cv.getMessage());
        errorMap.put("path", pathStr);
        errorMap.put("invalidValue", "" + cv.getInvalidValue());
        return errorMap;
    }

    /**
     * By default constraint violation (CV) contains only direct property path. For example when validation error relates to
     * "name" in "car.garage.name" query - CV will contains "name" property only.
     * For better user experience we need to define and return full path of validated property. In example above it's mean
     * that method will return "car.garage.name" instead of "name".
     *
     * @param entity    - validated entity
     * @param metaClass - validated entity meta class
     * @param cv        - current constraint violation
     * @param path      already composed part of path
     * @param visited   entities in object graph that already visited due to recursion
     * @return full property path
     */
    protected Stream<String> buildFullPath(Object entity, MetaClass metaClass, ConstraintViolation<?> cv, String path, Set<Object> visited) {
        if (metaClass == null || visited.contains(entity) || entity == null) {
            return Stream.empty();
        }
        String finalProperty = "" + cv.getPropertyPath();
        Object finalEntity = cv.getRootBean();
        Class<?> rootBeanClass = cv.getRootBeanClass();
        visited.add(entity);

        return metaClass.getProperties().stream()
                .flatMap(property -> {
                    if (property.getDomain().getJavaClass().equals(rootBeanClass)
                            && finalProperty.equals(property.getName())
                            && entity.equals(finalEntity)) {
                        return Stream.of(path.isEmpty() ? property.getName() : path + "." + property.getName());
                    }

                    if (MetaProperty.Type.COMPOSITION.equals(property.getType())) {
                        Object propertyValue = EntityValues.getValue(entity, property.getName());
                        Collection<Object> propertyEntities = propertyValue instanceof Collection
                                ? ((Collection) propertyValue)
                                : Collections.singletonList(propertyValue);

                        return propertyEntities.stream()
                                .flatMap(propertyEntity -> buildFullPath(
                                        propertyEntity,
                                        property.getRange().asClass(),
                                        cv,
                                        path.isEmpty() ? property.getName() : path + "." + property.getName(),
                                        visited
                                ));
                    }
                    return Stream.empty();
                });
    }

}
