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

package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.DevelopmentException;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.UiControllerDependencyInjector;
import io.jmix.ui.sys.UiControllerReflectionInspector;
import io.jmix.ui.sys.UiControllerReflectionInspector.InjectElement;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;

public class CubaUiControllerDependencyInjector extends UiControllerDependencyInjector {

    public CubaUiControllerDependencyInjector(ApplicationContext applicationContext,
                                              UiControllerReflectionInspector reflectionInspector) {
        super(applicationContext, reflectionInspector);
    }

    @Nullable
    @Override
    protected Object getInjectedInstance(Class<?> type, String name, InjectElement injectElement, FrameOwner frameOwner,
                                         ScreenOptions options) {
        if (Config.class.isAssignableFrom(type)) {
            Configuration configuration = (Configuration) applicationContext.getBean(Configuration.NAME);
            //noinspection unchecked
            return configuration.getConfig((Class<? extends Config>) type);
        } else if (Datasource.class.isAssignableFrom(type)) {
            checkLegacyFrame("Datasource can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting a datasource
            return ((LegacyFrame) frameOwner).getDsContext().get(name);
        } else if (DsContext.class.isAssignableFrom(type)) {
            checkLegacyFrame("DsContext can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting the DsContext
            return ((LegacyFrame) frameOwner).getDsContext();
        } else if (DataSupplier.class.isAssignableFrom(type)) {
            checkLegacyFrame("DataSupplier can be injected only into LegacyFrame inheritors", frameOwner);
            // Injecting the DataSupplier
            return ((LegacyFrame) frameOwner).getDsContext().getDataSupplier();
        } else if (WindowManager.class.isAssignableFrom(type)) {
            return UiControllerUtils.getScreenContext(frameOwner).getScreens();
        }

        return super.getInjectedInstance(type, name, injectElement, frameOwner, options);
    }

    protected void checkLegacyFrame(String message, FrameOwner frameOwner) {
        if (!(frameOwner instanceof LegacyFrame)) {
            throw new DevelopmentException(message);
        }
    }
}
