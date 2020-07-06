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

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.data.DataSupplier;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.compatibility.LegacyDependencyResolver;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component(LegacyDependencyResolver.NAME)
public class CubaLegacyDependencyResolver implements LegacyDependencyResolver {

    @Nullable
    @Override
    public Object resolveDependency(FrameOwner frameOwner, Class<?> type, String name) {
        if (!(frameOwner instanceof LegacyFrame)) {
            throw new UnsupportedOperationException("CubaLegacyDependencyResolver supports only legacy screens");
        }

        if (Datasource.class.isAssignableFrom(type)) {
            // Injecting a datasource
            return ((LegacyFrame) frameOwner).getDsContext().get(name);

        } else if (DsContext.class.isAssignableFrom(type)) {
            return ((LegacyFrame) frameOwner).getDsContext();
        } else if (DataSupplier.class.isAssignableFrom(type)) {
            return ((LegacyFrame) frameOwner).getDsContext().getDataSupplier();
        } else if (WindowManager.class.isAssignableFrom(type)) {
            return UiControllerUtils.getScreenContext(frameOwner).getScreens();
        }
        return null;
    }
}
