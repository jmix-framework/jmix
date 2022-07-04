/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.formatter;

import io.jmix.core.MetadataTools;
import io.jmix.flowui.kit.component.formatter.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Collection formatter to be used in view descriptors and controllers.
 * <p>
 * This formatter formats collection into a string where the elements of the collection are separated by commas.
 */
@Component("flowui_CollectionFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CollectionFormatter implements Formatter<Collection<?>> {

    protected MetadataTools metadataTools;

    public CollectionFormatter(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public String apply(@Nullable Collection value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }

        //noinspection unchecked
        return ((Collection<Object>) value).stream()
                .map(metadataTools::format)
                .collect(Collectors.joining(", "));
    }
}
