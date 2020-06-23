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

package io.jmix.securityui.screen.resourcepolicy;

import com.google.common.base.Strings;
import io.jmix.securityui.model.ResourcePolicyModel;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ScreensHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.util.TreeMap;
import java.util.stream.Collectors;

@UiController("sec_ScreenResourcePolicyModel.edit")
@UiDescriptor("screen-resource-policy-model-edit.xml")
@EditedEntityContainer("resourcePolicyModelDc")
public class ScreenResourcePolicyModelEdit extends StandardEditor<ResourcePolicyModel> {

    @Autowired
    private ComboBox<String> screenField;

    @Autowired
    private WindowConfig windowConfig;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private ScreensHelper screensHelper;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        TreeMap<String, String> map = windowConfig.getWindows().stream()
                .collect(Collectors.toMap(
                        windowInfo -> {
                            try {
                                String screenCaption = screensHelper.getScreenCaption(windowInfo);
                                if (Strings.isNullOrEmpty(screenCaption))
                                    return windowInfo.getId();
                                else
                                    return screenCaption;
                            } catch (FileNotFoundException e) {
                                return windowInfo.getId();
                            }
                        },
                        WindowInfo::getId,
                        (v1, v2) -> {
                            throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                        },
                        TreeMap::new));
        map.put(messageBundle.getMessage("allScreens"), "*");
        screenField.setOptionsMap(map);
    }
}