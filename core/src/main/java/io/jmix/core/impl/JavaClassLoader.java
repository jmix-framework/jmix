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

package io.jmix.core.impl;

import org.springframework.stereotype.Component;

import java.net.URL;
import java.net.URLClassLoader;

@Component("cuba_JavaClassLoader")
public class JavaClassLoader extends URLClassLoader {

    public JavaClassLoader() {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
    }

    public boolean removeClass(String name) {
        return false;
    }

    public void clearCache() {
    }
}
