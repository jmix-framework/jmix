/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import com.haulmont.cuba.gui.data.impl.GenericDataSupplier;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.gui.xml.data.DsContextLoader;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.component.Frame;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.xml.layout.loader.FragmentLoader;
import org.dom4j.Element;

import javax.annotation.Nullable;

public class CubaFragmentLoader extends FragmentLoader {

    @Override
    public void loadComponent() {
        if (resultComponent.getFrameOwner() instanceof AbstractFrame) {
            getScreenViewsLoader().deployViews(element);
        }

        super.loadComponent();
    }

    @Override
    protected void loadDataElement(Element element) {
        if (resultComponent.getFrameOwner() instanceof LegacyFrame) {
            Element dsContextElement = element.element("dsContext");
            loadDsContext(dsContextElement);
        }

        if (resultComponent.getFrameOwner() instanceof AbstractFrame) {
            Element companionsElem = element.element("companions");
            if (companionsElem != null) {
                getComponentContext().addInjectTask(new FragmentLoaderCompanionTask(resultComponent));
            }
        }
    }

    protected void loadDsContext(@Nullable Element dsContextElement) {
        DsContext dsContext = null;
        if (resultComponent.getFrameOwner() instanceof LegacyFrame) {
            DsContextLoader dsContextLoader;
            DsContext parentDsContext = ((ComponentLoaderContext) getComponentContext().getParent()).getDsContext();
            if (parentDsContext != null) {
                dsContextLoader = new DsContextLoader(parentDsContext.getDataSupplier());
            } else {
                dsContextLoader = new DsContextLoader(new GenericDataSupplier());
            }

            dsContext = dsContextLoader.loadDatasources(dsContextElement, parentDsContext,
                    getComponentContext().getAliasesMap());
            ((ComponentLoaderContext) context).setDsContext(dsContext);
        }
        if (dsContext != null) {
            FrameOwner frameOwner = getComponentContext().getFrame().getFrameOwner();
            if (frameOwner instanceof LegacyFrame) {
                LegacyFrame frame = (LegacyFrame) frameOwner;
                frame.setDsContext(dsContext);

                for (Datasource ds : dsContext.getAll()) {
                    if (ds instanceof DatasourceImplementation) {
                        ((DatasourceImplementation) ds).initialized();
                    }
                }

                dsContext.setFrameContext(resultComponent.getContext());
            }
        }
    }

    protected class FragmentLoaderCompanionTask implements InjectTask {
        protected Fragment fragment;

        public FragmentLoaderCompanionTask(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void execute(ComponentContext context, Frame frame) {
            String loggingId = context.getFullFrameId();
            try {
                if (fragment.getFrameOwner() instanceof AbstractFrame) {
                    Element companionsElem = element.element("companions");
                    if (companionsElem != null) {
                        // todo companions
//                        initCompanion(companionsElem, (AbstractFrame) fragment.getFrameOwner());
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Unable to init frame companion", e);
            }
        }

        protected void initCompanion(Element companionsElem, AbstractFrame frame) {
            // todo companions
            throw new UnsupportedOperationException();
//            String clientTypeId = AppConfig.getClientType().toString().toLowerCase();
//            Element element = companionsElem.element(clientTypeId);
//            if (element != null) {
//                String className = element.attributeValue("class");
//                if (!StringUtils.isBlank(className)) {
//                    Class aClass = getScripting().loadClassNN(className);
//                    Object companion;
//                    try {
//                        companion = aClass.newInstance();
//                        frame.setCompanion(companion);
//
//                        CompanionDependencyInjector cdi = new CompanionDependencyInjector(frame, companion);
//                        cdi.setBeanLocator(beanLocator);
//                        cdi.inject();
//                    } catch (Exception e) {
//                        throw new RuntimeException("Unable to init companion for frame", e);
//                    }
//                }
//            }
        }
    }
}
