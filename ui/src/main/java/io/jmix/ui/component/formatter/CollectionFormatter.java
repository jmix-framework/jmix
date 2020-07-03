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
import io.jmix.core.MetadataTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Collection formatter to be used in screen descriptors and controllers.
 * <p>
 * This formatter formats collection into a string where the elements of the collection are separated by commas.
 * <p>
 * Example usage:
 * <pre>
 *      &lt;formatter name=&quot;ui_CollectionFormatter&quot;/&gt;
 * </pre>
 * Use {@link BeanLocator} when creating the formatter programmatically.
 */
@Component(CollectionFormatter.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CollectionFormatter implements Formatter<Collection> {

    public static final String NAME = "ui_CollectionFormatter";

    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public String apply(Collection value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        //noinspection unchecked
        return ((Collection<Object>) value).stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "));
    }
}
