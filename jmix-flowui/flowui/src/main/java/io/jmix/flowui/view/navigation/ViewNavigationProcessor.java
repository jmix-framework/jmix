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

package io.jmix.flowui.view.navigation;

import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.sys.ViewSupport;
import org.springframework.stereotype.Component;

@Component("flowui_ViewNavigationProcessor")
public class ViewNavigationProcessor extends AbstractNavigationProcessor<ViewNavigator> {

    public ViewNavigationProcessor(ViewSupport viewSupport,
                                   ViewRegistry viewRegistry,
                                   ViewNavigationSupport navigationSupport) {
        super(viewSupport, viewRegistry, navigationSupport);
    }

    @Override
    protected Class<? extends View> inferViewClass(ViewNavigator navigator) {
        throw new IllegalStateException("Can't open a view. " +
                "Either view id or view class must be defined");
    }
}
