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

package io.jmix.flowui.facet.dataloadcoordinator;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import io.jmix.core.DevelopmentException;
import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.facet.DataLoadCoordinator.LikeClause;
import io.jmix.flowui.model.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnComponentValueChangedLoadTrigger implements DataLoadCoordinator.Trigger {

    private static final Logger log = LoggerFactory.getLogger(OnComponentValueChangedLoadTrigger.class);

    protected final DataLoader loader;
    protected final HasValue<?, ?> component;
    protected final String param;
    protected final LikeClause likeClause;

    public OnComponentValueChangedLoadTrigger(DataLoader loader, Component component,
                                              String param, LikeClause likeClause) {
        if (!(component instanceof HasValue)) {
            throw new DevelopmentException(String.format(
                    "Invalid component type in load trigger: %s. Expected " + HasValue.class.getSimpleName(),
                    component.getClass().getName()));
        }

        this.likeClause = likeClause;
        this.loader = loader;
        this.component = (HasValue<?, ?>) component;
        this.param = param;

        this.component.addValueChangeListener(event -> load());
    }

    @SuppressWarnings("rawtypes")
    protected void load() {
        Object value = component instanceof SupportsTypedValue
                ? ((SupportsTypedValue) component).getTypedValue()
                : component.getValue();
        if (value != null && likeClause != LikeClause.NONE) {
            if (!(value instanceof String)) {
                log.warn("Like clause with non-string parameter. The value is passed as is without wrapping in %.");
            } else {
                value = "%" + value + "%";
                if (likeClause == LikeClause.CASE_INSENSITIVE) {
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
