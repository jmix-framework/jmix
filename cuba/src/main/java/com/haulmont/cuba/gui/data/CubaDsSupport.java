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

package com.haulmont.cuba.gui.data;

import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.xml.data.DsContextLoader;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import io.jmix.core.commons.util.ReflectionHelper;
import io.jmix.ui.components.Window;
import io.jmix.ui.gui.data.compatibility.DsSupport;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenOptions;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CubaDsSupport implements DsSupport {

    protected DataSupplier defaultDataSupplier = new GenericDataSupplier();

    @Override
    public io.jmix.ui.xml.layout.loaders.ComponentLoaderContext createComponentLoaderContext(ScreenOptions screenOptions) {
        return new ComponentLoaderContext(screenOptions);
    }

    @Override
    public void initDsContext(Screen screen, Element screenDescriptor, io.jmix.ui.xml.layout.loaders.ComponentLoaderContext context) {
        DsContext dsContext = loadDsContext(screenDescriptor);
        initDatasources(screen.getWindow(), dsContext, context.getParams());

        ((ComponentLoaderContext) context).setDsContext(dsContext);

        DsContext screenContext = ((LegacyFrame) screen).getDsContext();
        if (screenContext != null) {
            screenContext.setFrameContext(screen.getWindow().getContext());
        }
    }

    protected DsContext loadDsContext(Element element) {
        DataSupplier dataSupplier;

        String dataSupplierClass = element.attributeValue("dataSupplier");
        if (StringUtils.isEmpty(dataSupplierClass)) {
            dataSupplier = defaultDataSupplier;
        } else {
            Class<Object> aClass = ReflectionHelper.getClass(dataSupplierClass);
            try {
                dataSupplier = (DataSupplier) aClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to create data supplier for screen", e);
            }
        }

        //noinspection UnnecessaryLocalVariable
        DsContext dsContext = new DsContextLoader(dataSupplier).loadDatasources(element.element("dsContext"), null, null);
        return dsContext;
    }

    protected void initDatasources(Window window, DsContext dsContext, @SuppressWarnings("unused") Map<String, Object> params) {
        ((LegacyFrame) window.getFrameOwner()).setDsContext(dsContext);

        for (Datasource ds : dsContext.getAll()) {
            if (Datasource.State.NOT_INITIALIZED.equals(ds.getState()) && ds instanceof DatasourceImplementation) {
                ((DatasourceImplementation) ds).initialized();
            }
        }
    }
}
