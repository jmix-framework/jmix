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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.model.impl.CubaScreenDataImpl;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.impl.DataLoadCoordinatorImpl;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.sys.UiControllerReflectionInspector;

import javax.annotation.Nullable;

public class CubaDataLoadCoordinator extends DataLoadCoordinatorImpl {

    public CubaDataLoadCoordinator(UiControllerReflectionInspector reflectionInspector) {
        super(reflectionInspector);
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);
        if (owner != null) {
            ((CubaScreenDataImpl) UiControllerUtils.getScreenData(owner.getFrameOwner()))
                    .setLoadBeforeShowStrategy(screen -> { /* do nothing */ });
        }
    }
}
