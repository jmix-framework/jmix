/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.filter.addcondition.AddConditionWindow;
import com.haulmont.cuba.gui.components.filter.addcondition.ConditionDescriptorsTreeBuilderAPI;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.descriptor.AbstractConditionDescriptor;
import com.haulmont.cuba.gui.components.filter.descriptor.CustomConditionCreator;
import com.haulmont.cuba.gui.components.filter.descriptor.DynamicAttributesConditionCreator;
import com.haulmont.cuba.gui.components.filter.edit.CustomConditionEditor;
import com.haulmont.cuba.gui.components.filter.edit.DynamicAttributesConditionEditor;
import io.jmix.core.common.datastruct.Tree;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Window;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that does a sequence of steps for adding new condition:
 * <p>
 * <ol> <li>opens add condition dialog</li> <li>if necessary opens new condition editor</li> <li>invokes a handler
 * passing new condition to it</li> </ol>
 */
public class AddConditionHelper {

    protected WindowManager windowManager;
    protected WindowConfig windowConfig;
    protected Filter filter;
    protected Handler handler;
    protected boolean hideDynamicAttributes;
    protected boolean hideCustomConditions;
    protected CubaProperties properties;

    public AddConditionHelper(Filter filter, Handler handler) {
        this(filter, false, false, handler);
    }

    public AddConditionHelper(Filter filter, boolean hideDynamicAttributes,
                              boolean hideCustomConditions, Handler handler) {
        this.filter = filter;
        this.handler = handler;
        this.hideDynamicAttributes = hideDynamicAttributes;
        this.hideCustomConditions = hideCustomConditions;
        windowManager = AppBeans.get(WindowManagerProvider.class).get();
        windowConfig = AppBeans.get(WindowConfig.class);
        properties = AppBeans.get(CubaProperties.class);
    }

    public interface Handler {
        void handle(AbstractCondition condition);
    }

    /**
     * Opens AddCondition window. When condition is selected/created a {@code Handler#handle} method will be called
     *
     * @param conditionsTree conditions tree is necessary for custom condition editing. It is used for suggestion of
     *                       other component names in 'param where' field.
     */
    public void addCondition(final ConditionsTree conditionsTree) {
        Map<String, Object> params = new HashMap<>();
        ConditionDescriptorsTreeBuilderAPI descriptorsTreeBuilder = AppBeans.getPrototype(ConditionDescriptorsTreeBuilderAPI.NAME,
                filter,
                properties.getGenericFilterPropertiesHierarchyDepth(),
                hideDynamicAttributes,
                hideCustomConditions,
                conditionsTree);
        Tree<AbstractConditionDescriptor> descriptorsTree = descriptorsTreeBuilder.build();
        params.put("descriptorsTree", descriptorsTree);
        WindowInfo windowInfo = windowConfig.getWindowInfo("addCondition");
        AddConditionWindow window = (AddConditionWindow) windowManager.openWindow(windowInfo, OpenType.DIALOG, params);
        window.addCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                Collection<AbstractConditionDescriptor> descriptors = window.getDescriptors();
                if (descriptors != null) {
                    for (AbstractConditionDescriptor descriptor : descriptors) {
                        _addCondition(descriptor, conditionsTree);
                    }
                }
            }
        });
    }

    protected void _addCondition(AbstractConditionDescriptor descriptor, ConditionsTree conditionsTree) {
        final AbstractCondition condition = descriptor.createCondition();

        if (descriptor instanceof CustomConditionCreator) {
            WindowInfo windowInfo = windowConfig.getWindowInfo("customConditionEditor");
            Map<String, Object> params = new HashMap<>();
            params.put("condition", condition);
            params.put("conditionsTree", conditionsTree);
            params.put("filter", filter);
            final CustomConditionEditor window = (CustomConditionEditor) windowManager.openWindow(windowInfo, OpenType.DIALOG, params);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    handler.handle(condition);
                }
            });
        } else if (descriptor instanceof DynamicAttributesConditionCreator) {
            WindowInfo windowInfo = windowConfig.getWindowInfo("dynamicAttributesConditionEditor");
            Map<String, Object> params = new HashMap<>();
            params.put("condition", condition);
            params.put("filter", filter);
            DynamicAttributesConditionEditor window = (DynamicAttributesConditionEditor) windowManager.openWindow(windowInfo, OpenType.DIALOG, params);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    handler.handle(condition);
                }
            });
        } else {
            handler.handle(condition);
        }
    }
}
