/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.core.impl.DatatypeRegistryImpl;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.flowui.component.SupportsDatatype;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;

public abstract class AbstractTypedFieldLoader<C extends Component & SupportsDatatype<?>>
        extends AbstractComponentLoader<C> {

    protected DatatypeRegistryImpl datatypeRegistry;

    public DatatypeRegistryImpl getDatatypeRegistry() {
        if (datatypeRegistry == null) {
            datatypeRegistry = applicationContext.getBean(DatatypeRegistryImpl.class);
        }
        return datatypeRegistry;
    }

    @Override
    public void loadComponent() {
        loadDataType();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void loadDataType() {
        String dataTypeString = loadString(element, "dataType").orElse(null);

        if (dataTypeString == null) {
            return;
        }

        Datatype datatype = getDatatypeRegistry().find(dataTypeString);
        resultComponent.setDatatype(datatype);
    }
}
