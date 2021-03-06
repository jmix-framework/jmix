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
package io.jmix.ui.component.formatter;

import io.jmix.core.BeanLocator;
import io.jmix.core.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Class name formatter to be used in screen descriptors and controllers.
 * <p>
 * The formatter formats the string that contains the class path, where the packages are separated by dots,
 * into a string that is the class name.
 * <p>
 * Use {@link BeanLocator} when creating the formatter programmatically.
 */
@Component(ClassNameFormatter.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClassNameFormatter implements Formatter<String> {

    public static final String NAME = "ui_ClassNameFormatter";

    @Autowired
    protected Messages messages;

    @Nullable
    @Override
    public String apply(@Nullable String value) {
        if (value == null) {
            return null;
        }

        int i = value.lastIndexOf(".");
        if (i < 0) {
            return value;
        } else {
            return messages.getMessage(value.substring(0, i), value.substring(i + 1));
        }
    }
}
