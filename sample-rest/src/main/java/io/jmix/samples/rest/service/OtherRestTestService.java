/*
 * Copyright 2020 Haulmont.
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

import io.jmix.samples.rest.entity.driver.Car;
import io.jmix.samples.rest.entity.driver.NotPersistentStringIdEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service is used in functional tests
 */
public interface OtherRestTestService {

    String NAME = "jmix_OtherRestTestService";

    void testMethod();
}
