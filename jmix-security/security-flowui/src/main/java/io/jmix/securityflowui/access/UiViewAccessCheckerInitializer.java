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

package io.jmix.securityflowui.access;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * @deprecated Vaadin introduced new {@link com.vaadin.flow.server.auth.NavigationAccessControl} mechanism.
 * {@link JmixNavigationAccessChecker} is used for view access control.
 */
@Deprecated(since = "2.3", forRemoval = true)
//@Component("flowui_ViewAccessCheckerInitializer")
public class UiViewAccessCheckerInitializer implements VaadinServiceInitListener {

    protected UiViewAccessChecker viewAccessChecker;

    public UiViewAccessCheckerInitializer(UiViewAccessChecker viewAccessChecker) {
        this.viewAccessChecker = viewAccessChecker;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource()
                .addUIInitListener(uiInitEvent -> uiInitEvent.getUI()
                        .addBeforeEnterListener(viewAccessChecker));
    }
}
