/*
 * Copyright 2019 Haulmont.
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

package io.jmix.samples.rest.controller;

import io.jmix.rest.api.exception.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice("io.jmix.rest.api.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomRestControllerExceptionHandler {

    private Logger log = LoggerFactory.getLogger(CustomRestControllerExceptionHandler.class);

    @ExceptionHandler(CustomHttpClientErrorException.class)
    @ResponseBody
    public ResponseEntity<ErrorInfo> handleException(Exception e) {
        log.error("Exception in REST controller", e);
        if (e instanceof CustomHttpClientErrorException) {
            CustomHttpClientErrorException ex = (CustomHttpClientErrorException) e;
            ErrorInfo errorInfo = new ErrorInfo(ex.getStatusCode().getReasonPhrase(), ex.getStatusText());
            return new ResponseEntity<>(errorInfo, ex.getStatusCode());
        }
        return null;
    }
}
