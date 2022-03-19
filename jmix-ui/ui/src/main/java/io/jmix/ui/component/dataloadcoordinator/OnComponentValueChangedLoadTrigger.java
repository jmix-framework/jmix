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

package io.jmix.ui.component.dataloadcoordinator;

import io.jmix.core.DevelopmentException;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataLoadCoordinator;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@StudioElement(
        caption = "OnComponentValueChanged Trigger",
        xmlElement = "onComponentValueChanged",
        icon = "io/jmix/ui/icon/facet/onComponentValueChangedLoadTrigger.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "component", type = PropertyType.COMPONENT_REF, required = true,
                        options = {"io.jmix.ui.component.Component"}),
                @StudioProperty(name = "param", type = PropertyType.STRING),
                @StudioProperty(name = "likeClause", type = PropertyType.ENUMERATION, defaultValue = "NONE",
                        options = {"NONE", "CASE_SENSITIVE", "CASE_INSENSITIVE"})
        }
)
public class OnComponentValueChangedLoadTrigger implements DataLoadCoordinator.Trigger {

    private final DataLoader loader;
    private final HasValue component;
    private final String param;
    private final DataLoadCoordinator.LikeClause likeClause;

    private static final Logger log = LoggerFactory.getLogger(OnComponentValueChangedLoadTrigger.class);

    public OnComponentValueChangedLoadTrigger(DataLoader loader, Component component, String param, DataLoadCoordinator.LikeClause likeClause) {
        this.likeClause = likeClause;
        if (!(component instanceof HasValue)) {
            throw new DevelopmentException(String.format(
                    "Invalid component type in load trigger: %s. Expected HasValue", component.getClass().getName()));
        }
        this.loader = loader;
        this.component = (HasValue) component;
        this.param = param;
        //noinspection unchecked
        this.component.addValueChangeListener(event -> load());
    }

    private void load() {
        Object value = component.getValue();
        if (value != null && likeClause != DataLoadCoordinator.LikeClause.NONE) {
            if (!(value instanceof String)) {
                log.warn("Like clause with non-string parameter. The value is passed as is without wrapping in %.");
            } else {
                value = "%" + value + "%";
                if (likeClause == DataLoadCoordinator.LikeClause.CASE_INSENSITIVE) {
                    value = "(?i)" + value;
                }
            }
        }
        loader.setParameter(param, value);
        loader.load();
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}
