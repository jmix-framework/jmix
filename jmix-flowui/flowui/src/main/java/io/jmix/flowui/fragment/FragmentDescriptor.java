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

package io.jmix.flowui.fragment;

import java.lang.annotation.*;

/**
 * Specifies a string value that is a file path to an xml descriptor
 * that can be used for a {@link Fragment} initialization.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FragmentDescriptor {

    /**
     * @return a path to the XML descriptor. If the value contains a file name only (i.e. don't start with '/'),
     * it is assumed that the file is located in the package of the fragment class.
     */
    String value();
}
