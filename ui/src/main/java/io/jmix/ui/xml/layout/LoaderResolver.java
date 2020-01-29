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

package io.jmix.ui.xml.layout;

import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

@Component(LoaderResolver.NAME)
public class LoaderResolver {

    public static final String NAME = "jmix_LoaderResolver";

    @Inject
    protected List<LoaderConfig> loaderConfigs;

    @SuppressWarnings("rawtypes")
    @Nullable
    public Class<? extends ComponentLoader> getLoader(Element element) {
        for (LoaderConfig config : loaderConfigs) {
            if (config.supports(element)) {
                return config.getLoader(element);
            }
        }
        return null;
    }
}
