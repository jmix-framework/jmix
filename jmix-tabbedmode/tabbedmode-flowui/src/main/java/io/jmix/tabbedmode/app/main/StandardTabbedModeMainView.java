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

package io.jmix.tabbedmode.app.main;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import io.jmix.core.Metadata;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.component.applayout.JmixAppLayout;
import io.jmix.flowui.view.*;
import io.jmix.tabbedmode.JmixUI;
import io.jmix.tabbedmode.Views;
import io.jmix.tabbedmode.component.workarea.AppWorkArea;
import io.jmix.tabbedmode.component.workarea.HasWorkArea;
import io.jmix.tabbedmode.navigation.RedirectHandler;
import io.jmix.tabbedmode.view.ViewOpenMode;
import org.springframework.lang.Nullable;

@CssImport("./src/view/main-view.css")
public class StandardTabbedModeMainView extends StandardMainView implements HasWorkArea {

    private AppWorkArea workArea;

    @Override
    protected JmixAppLayout initContent() {
        JmixAppLayout jmixAppLayout = super.initContent();
        workArea = createWorkArea();
        jmixAppLayout.setContent(workArea);

        return jmixAppLayout;
    }

    private AppWorkArea createWorkArea() {
        return uiComponents().create(AppWorkArea.class);
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        // TODO: gg, explicitly define in XML?
        return workArea;
    }

    @Override
    public Component getInitialLayout() {
        return workArea.getInitialLayout();
    }

    @Override
    public void setInitialLayout(@Nullable Component initialLayout) {
        workArea.setInitialLayout(initialLayout);
    }

    @Override
    public void showRouterLayoutContent(@Nullable HasElement content) {
        // TODO: gg, re-implement and delegate to WorkArea
        super.showRouterLayoutContent(content);
    }

    @Override
    public void removeRouterLayoutContent(HasElement oldContent) {
        // TODO: gg, re-implement and delegate to WorkArea
        super.removeRouterLayoutContent(oldContent);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        openDefaultView();
        handleRedirect();
    }

    private void showNotification(String message) {
        Notification.show(message, 3000, Notification.Position.MIDDLE);
    }

    // TODO: gg, move to helper class

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void openDefaultView() {
        String defaultViewId = uiProperties().getDefaultViewId();
        if (Strings.isNullOrEmpty(defaultViewId)) {
            return;
        }

        if (!viewRegistry().hasView(defaultViewId)) {
            return;
        }

        View<?> view = views().create(defaultViewId);
        if (view instanceof DetailView detailView) {
            detailView.setEntityToEdit(getEntityToEdit(defaultViewId));
        }

        views().open(view, ViewOpenMode.NEW_TAB);

        // TODO: gg, implement
        /*view.setDefaultView(view);

        if (!uiProperties.isDefaultScreenCanBeClosed()) {
            view.setCloseable(false);
        }*/
    }

    protected Object getEntityToEdit(String viewId) {
        ViewInfo viewInfo = viewRegistry().getViewInfo(viewId);
        Class<?> entityClass = getEntityClass(viewInfo);

        return metadata().create(entityClass);
    }

    protected Class<?> getEntityClass(ViewInfo viewInfo) {
        return DetailViewTypeExtractor.extractEntityClass(viewInfo)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Failed to determine entity type for detail view '%s'", viewInfo.getId())));
    }

    protected void handleRedirect() {
        JmixUI ui = JmixUI.getCurrent();
        if (ui != null) {
            RedirectHandler redirectHandler = ui.getRedirectHandler();
            if (redirectHandler.scheduled()) {
                redirectHandler.redirect();
            }
        }
    }

    protected UiComponents uiComponents() {
        return getApplicationContext().getBean(UiComponents.class);
    }

    protected UiProperties uiProperties() {
        return getApplicationContext().getBean(UiProperties.class);
    }

    protected Views views() {
        return getApplicationContext().getBean(Views.class);
    }

    protected ViewRegistry viewRegistry() {
        return getApplicationContext().getBean(ViewRegistry.class);
    }

    protected Metadata metadata() {
        return getApplicationContext().getBean(Metadata.class);
    }
}