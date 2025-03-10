/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.sys;

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.view.*;
import io.jmix.tabbedmode.TabbedModeProperties;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.view.TabbedModeViewProperties;
import io.jmix.tabbedmode.view.TabbedModeViewUtils;
import io.jmix.tabbedmode.view.ViewOpenMode;
import org.springframework.stereotype.Component;

@Component("tabmod_MainViewSupport")
public class MainViewSupport {

    protected final Views views;
    protected final UiProperties uiProperties;
    protected final ViewRegistry viewRegistry;
    protected final Metadata metadata;
    protected final TabbedModeProperties tabbedModeProperties;

    public MainViewSupport(Views views,
                           UiProperties uiProperties,
                           ViewRegistry viewRegistry,
                           Metadata metadata,
                           TabbedModeProperties tabbedModeProperties) {
        this.views = views;
        this.uiProperties = uiProperties;
        this.viewRegistry = viewRegistry;
        this.metadata = metadata;
        this.tabbedModeProperties = tabbedModeProperties;
    }

    public void openDefaultView() {
        String defaultViewId = uiProperties.getDefaultViewId();
        if (Strings.isNullOrEmpty(defaultViewId)) {
            return;
        }

        if (!viewRegistry.hasView(defaultViewId)) {
            return;
        }

        View<?> view = views.create(defaultViewId);
        if (view instanceof DetailView<?> detailView) {
            detailView.setEntityToEdit(getEntityToEdit(defaultViewId));
        }

        TabbedModeViewUtils.setDefaultView(view, true);

        if (!tabbedModeProperties.isDefaultViewCloseable()) {
            TabbedModeViewUtils.setCloseable(view, false);
        }

        views.open(view, ViewOpenMode.NEW_TAB);
    }

    protected <E> E getEntityToEdit(String viewId) {
        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        Class<?> entityClass = getEntityClass(viewInfo);

        //noinspection unchecked
        return (E) metadata.create(entityClass);
    }

    protected Class<?> getEntityClass(ViewInfo viewInfo) {
        return DetailViewTypeExtractor.extractEntityClass(viewInfo)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Failed to determine entity type for detail view '%s'", viewInfo.getId())));
    }
}
