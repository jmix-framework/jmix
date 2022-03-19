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

package io.jmix.samples.rest.service;

import org.springframework.stereotype.Service;

/**
 * Service is used in functional tests to check public methods accessibility
 * of the bean that does not implement any interfaces.
 */
@Service("jmix_RestTestServiceWithoutInterfaces")
public class RestTestServiceWithoutInterfaces {

    /* Variable NAME is introduced for testing purposes. The actual bean name
     is taken from configuration resources.
     */
    public static final String NAME = "jmix_RestTestServiceWithoutInterfaces";

    public void emptyMethod() {

    }
}
