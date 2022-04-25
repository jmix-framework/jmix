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

package io.jmix.flowui.xml.layout;


import io.jmix.flowui.xml.layout.loader.ButtonLoader;
import io.jmix.flowui.xml.layout.loader.HorizontalLayoutLoader;
import io.jmix.flowui.xml.layout.loader.TextFieldLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseLoaderConfig {

    protected Map<String, Class<? extends ComponentLoader>> loaders = new ConcurrentHashMap<>();

    public BaseLoaderConfig() {
        initStandardLoaders();
    }

    protected void initStandardLoaders() {
        // TODO: gg, store component names somewhere
        loaders.put("hbox", HorizontalLayoutLoader.class);

        loaders.put("button", ButtonLoader.class);

        loaders.put("textField", TextFieldLoader.class);
    }
}
