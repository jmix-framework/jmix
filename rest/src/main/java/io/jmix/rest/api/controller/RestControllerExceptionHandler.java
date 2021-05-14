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

package io.jmix.rest.api.controller;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.serialization.EntityTokenException;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.validation.CustomValidationException;
import io.jmix.rest.api.exception.ConstraintViolationInfo;
import io.jmix.rest.api.exception.ErrorInfo;
import io.jmix.rest.api.exception.RestAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;

@ControllerAdvice("io.jmix.rest.api.controller")
public class RestControllerExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestControllerExceptionHandler.class);

    protected static final Collection<Class> SERIALIZABLE_INVALID_VALUE_TYPES =
            Collections.unmodifiableList(
                    Arrays.asList(
                            String.class, Date.class, LocalDate.class,  LocalDateTime.class, OffsetDateTime.class,
                            LocalTime.class, OffsetTime.class, Number.class, Enum.class, UUID.class
                    ));

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @ExceptionHandler(RestAPIException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleRestAPIException(RestAPIException e) {
        if (e.getCause() == null) {
            log.info("RestAPIException: {}, {}", e.getMessage(), e.getDetails());
        } else {
            log.error("RestAPIException: {}, {}", e.getMessage(), e.getDetails(), e.getCause());
        }
        ErrorInfo errorInfo = new ErrorInfo(e.getMessage(), e.getDetails());
        return new ResponseEntity<>(errorInfo, e.getHttpStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<List<ConstraintViolationInfo>> handleConstraintViolation(ConstraintViolationException e) {
        log.debug("ConstraintViolationException: {}, violations:\n{}", e.getMessage(), e.getConstraintViolations());

        List<ConstraintViolationInfo> violationInfos = getConstraintViolationInfos(e.getConstraintViolations());

        return new ResponseEntity<>(violationInfos, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomValidationException.class)
    @ResponseBody
    public ResponseEntity<List<ConstraintViolationInfo>> handleCustomValidationException(CustomValidationException e) {
        log.debug("CustomValidationException: {}", e.getMessage());

        ConstraintViolationInfo errorInfo = new ConstraintViolationInfo();
        errorInfo.setPath("");
        errorInfo.setInvalidValue(null);
        errorInfo.setMessage(e.getLocalizedMessage());
        errorInfo.setMessageTemplate("{io.jmix.core.validation.CustomValidationException}");

        return new ResponseEntity<>(Collections.singletonList(errorInfo), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleValidationException(ValidationException e) {
        log.error("ValidationException in service", e);
        ErrorInfo errorInfo = new ErrorInfo("Server error", "");
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleRowLevelSecurityException(AccessDeniedException e) {
        log.error("AccessDeniedException in service", e);
        ErrorInfo errorInfo = new ErrorInfo("Forbidden", e.getMessage());
        return new ResponseEntity<>(errorInfo, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityTokenException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleEntityTokenException(EntityTokenException e) {
        log.error("RowLevelSecurityException in service", e);
        ErrorInfo errorInfo = new ErrorInfo("Forbidden", e.getMessage());
        return new ResponseEntity<>(errorInfo, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleOptimisticLockException(OptimisticLockException e) {
        log.error("Optimistic lock", e);
        ErrorInfo errorInfo = new ErrorInfo("Optimistic lock", e.getMessage());
        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("Exception in REST controller", e);
        ErrorInfo errorInfo = new ErrorInfo("Server error", "");
        return new ResponseEntity<>(errorInfo, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected List<ConstraintViolationInfo> getConstraintViolationInfos(Set<ConstraintViolation<?>> violations) {
        List<ConstraintViolationInfo> violationInfos = new ArrayList<>();

        for (ConstraintViolation<?> violation : violations) {
            ConstraintViolationInfo info = new ConstraintViolationInfo();

            info.setMessage(violation.getMessage());
            info.setMessageTemplate(violation.getMessageTemplate());

            Object invalidValue = violation.getInvalidValue();
            if (invalidValue != null) {
                Class<?> invalidValueClass = invalidValue.getClass();

                boolean serializable = false;
                for (Class serializableType : SERIALIZABLE_INVALID_VALUE_TYPES) {
                    //noinspection unchecked
                    if (serializableType.isAssignableFrom(invalidValueClass)) {
                        serializable = true;
                        break;
                    }
                }
                if (serializable) {
                    if (invalidValue instanceof Temporal || invalidValue instanceof Date) {
                        Datatype datatype = getDatatype(violation);
                        info.setInvalidValue(datatype.format(invalidValue, currentAuthentication.getLocale()));
                    } else {
                        info.setInvalidValue(invalidValue);
                    }
                } else {
                    info.setInvalidValue(null);
                }
            }

            if (violation.getPropertyPath() != null) {
                info.setPath(violation.getPropertyPath().toString());
            }

            violationInfos.add(info);
        }
        return violationInfos;
    }

    protected Datatype getDatatype(ConstraintViolation<?> violation) {
        MetaClass metaClass = metadata.getClass(violation.getRootBeanClass());
        String propertyPath = violation.getPropertyPath().toString();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metaClass, propertyPath);
        return metaPropertyPath == null
                ? datatypeRegistry.find(Date.class)
                : metaPropertyPath.getRange().asDatatype();
    }
}
