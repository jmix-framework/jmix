/*
 * Copyright 2026 Haulmont.
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

package io.jmix.samples.rest.service;

import io.jmix.rest.annotation.RestMethod;
import io.jmix.rest.annotation.RestParam;
import io.jmix.rest.annotation.RestService;

@RestService(RestAnnotatedParamsTestService.NAME)
public class RestAnnotatedParamsTestService {

    public static final String NAME = "jmix_RestAnnotatedParamsTestService";

    @RestMethod
    public Integer sum(@RestParam("first") int number1, @RestParam("second") int number2) {
        return number1 + number2;
    }

    @RestMethod
    public String noParamsMethod() {
        return "ok";
    }

    @RestMethod
    public Integer methodWithBlankParam(@RestParam("   ") int value) {
        return value;
    }

    @RestMethod
    public Integer methodWithDuplicateParams(@RestParam("dup") int first, @RestParam("dup") int second) {
        return first + second;
    }
}
