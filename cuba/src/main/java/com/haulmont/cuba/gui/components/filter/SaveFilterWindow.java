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

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.security.entity.FilterEntity;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.DialogWindow;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Window;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Window for editing new filter name
 */
public class SaveFilterWindow extends AbstractWindow {
    @Autowired
    protected TextField<String> filterName;

    @Autowired
    protected ThemeConstants theme;

    @Autowired
    protected Metadata metadata;

    @WindowParam(name = "existingNames")
    protected List<String> existingNames;

    @Override
    public void init(Map<String, Object> params) {
        if (getWindow() instanceof DialogWindow) {
            getWindow().setWidth(theme.get("cuba.gui.saveFilterWindow.dialog.width"));
        }

        super.init(params);
        String filterNameParam = (String) params.get("filterName");
        if (!Strings.isNullOrEmpty(filterNameParam)) {
            filterName.setValue(filterNameParam);
        }
        MetaProperty property = metadata.getClassNN(FilterEntity.class).getProperty("name");
        Map<String, Object> annotations = property.getAnnotations();
        Integer maxLength = (Integer) annotations.get("length");
        if (maxLength != null) {
            filterName.setMaxLength(maxLength);
        }
    }

    public void commit() {
        if (Strings.isNullOrEmpty(filterName.getValue())) {
            showNotification(messages.getMessage("filter.saveFilter.fillName"), NotificationType.WARNING);
            return;
        }
        if (existingNames != null && existingNames.contains(filterName.getValue())) {
            showNotification(messages.getMessage("filter.saveFilter.suchNameAlreadyExists"), NotificationType.WARNING);
            return;
        }
        close(Window.COMMIT_ACTION_ID);
    }

    public void cancel() {
        close(Window.CLOSE_ACTION_ID);
    }

    public String getFilterName() {
        return filterName.getValue();
    }
}
