/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.sys;

import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.ScreenOptions;
import io.jmix.ui.sys.ControllerDependencyInjector.InjectionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Class collects controller dependency injectors and applies them to the screen controller.
 */
@Component("ui_UiControllerDependencyManager")
public class UiControllerDependencyManager {

    protected UiControllerReflectionInspector reflectionInspector;
    protected UiControllerDependencyInjector controllerDependencyInjector;
    protected List<ControllerDependencyInjector> dependencyInjectors;

    @Autowired
    public void setReflectionInspector(UiControllerReflectionInspector reflectionInspector) {
        this.reflectionInspector = reflectionInspector;
    }

    @Autowired
    public void setControllerDependencyInjector(UiControllerDependencyInjector controllerDependencyInjector) {
        this.controllerDependencyInjector = controllerDependencyInjector;
    }

    @Autowired(required = false)
    public void setDependencyInjectors(List<ControllerDependencyInjector> dependencyInjectors) {
        this.dependencyInjectors = dependencyInjectors;
    }

    /**
     * Injects elements using {@link ControllerDependencyInjector} beans and base
     * {@link UiControllerDependencyInjector} injector.
     *
     * @param controller screen controller
     * @param options    screen options
     */
    public void inject(FrameOwner controller, ScreenOptions options) {
        controllerDependencyInjector.inject(controller, options);

        if (CollectionUtils.isNotEmpty(dependencyInjectors)) {
            InjectionContext injectionContext = new InjectionContext(controller, options);
            for (ControllerDependencyInjector injector : dependencyInjectors) {
                injector.inject(injectionContext);
            }
        }
    }
}
