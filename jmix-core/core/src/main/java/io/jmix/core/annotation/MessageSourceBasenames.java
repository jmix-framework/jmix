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

package io.jmix.core.annotation;

import io.jmix.core.impl.MessageSourceConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Defines additional basenames for Jmix's {@link MessageSource}.
 * <p>
 * This annotation should be placed on the {@code @SpringBootApplication} class.
 * In the following example, messages will be loaded from the main {@code com/company/demo/messages.properties} bundle
 * and from the additional {@code com/company/demo/othermessages.properties} bundle.
 * <pre>
 * package com.company.demo;
 *
 * &#064;SpringBootApplication
 * &#064;MessageSourceBasenames({"com/company/demo/othermessages"})
 * public class DemoApplication { }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MessageSourceConfiguration.class)
public @interface MessageSourceBasenames {

    String[] value() default "";
}
