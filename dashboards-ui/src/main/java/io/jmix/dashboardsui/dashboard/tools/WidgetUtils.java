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

package io.jmix.dashboardsui.dashboard.tools;

import io.jmix.core.Messages;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.repository.WidgetTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Component("dshbrd_WidgetUtils")
public class WidgetUtils {

    @Autowired
    protected WidgetRepository widgetRepository;

    @Autowired
    protected Messages messages;

    public String getWidgetType(String fragmentId) {
        String result = StringUtils.EMPTY;
        Optional<WidgetTypeInfo> widgetTypeOpt = widgetRepository.getWidgetTypesInfo().stream()
                .filter(typeInfo -> fragmentId.equals(typeInfo.getFragmentId()))
                .findFirst();

        if (widgetTypeOpt.isPresent()) {
            result = widgetTypeOpt.get().getName();
        }
        return result;
    }

    public Map<String, String> getWidgetCaptions() {
        Map<String, String> map = new HashMap<>();
        List<WidgetTypeInfo> typesInfo = widgetRepository.getWidgetTypesInfo();
        for (WidgetTypeInfo typeInfo : typesInfo) {
            String browseFragmentId = typeInfo.getFragmentId();
            String name = typeInfo.getName();
            String property = format("dashboard-widget.%s", name);
            String mainMessage = messages.getMessage(property);
            String caption = mainMessage.equals(property) ? name : mainMessage;

            map.put(caption, browseFragmentId);
        }

        return map;
    }
}
