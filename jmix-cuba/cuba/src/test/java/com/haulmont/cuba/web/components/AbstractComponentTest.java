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

package com.haulmont.cuba.web.components;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.web.testsupport.WebTest;
import test_support.WebTestConfiguration;
import io.jmix.core.Metadata;
import io.jmix.core.FetchPlanRepository;
import io.jmix.ui.component.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simple base class for UI component tests.
 *
 * @see WebTestConfiguration
 */
@WebTest
public class AbstractComponentTest {

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected FetchPlanRepository viewRepository;

    @Autowired
    protected Metadata metadata;

    protected <T extends Component> T create(Class<T> componentClass) {
        return uiComponents.create(componentClass);
    }

    protected Component create(String name) {
        return uiComponents.create(name);
    }
}
