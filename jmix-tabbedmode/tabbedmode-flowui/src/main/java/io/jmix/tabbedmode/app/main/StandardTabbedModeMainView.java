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

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.router.RouterLayout;
import io.jmix.flowui.component.applayout.JmixAppLayout;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import io.jmix.tabbedmode.JmixUI;
import io.jmix.tabbedmode.component.workarea.WorkArea;
import io.jmix.tabbedmode.navigation.RedirectHandler;
import io.jmix.tabbedmode.sys.MainViewSupport;

import java.util.Optional;

/**
 * Base class for tabbed mode main view.
 */
public class StandardTabbedModeMainView extends View<JmixAppLayout> implements HasWorkArea, RouterLayout {

    protected WorkArea workArea;

    @Override
    public Optional<WorkArea> getWorkAreaOptional() {
        return Optional.ofNullable(workArea);
    }

    /**
     * Sets the {@link WorkArea} component for the view and updates the content
     * with the specified work area.
     *
     * @param workArea the {@link WorkArea} instance to set
     * @throws IllegalStateException if the work area has already been initialized
     */
    public void setWorkArea(WorkArea workArea) {
        Preconditions.checkState(this.workArea == null, "%s has already been initialized"
                .formatted(WorkArea.class.getSimpleName()));

        this.workArea = workArea;
        getContent().setContent(workArea);
    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        openDefaultView();
        handleRedirect();
    }

    protected void openDefaultView() {
        mainViewSupport().openDefaultView();
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

    @Override
    public void showRouterLayoutContent(HasElement content) {
        throw new UnsupportedOperationException("Use %s to show content instead"
                .formatted(WorkArea.class.getSimpleName()));
    }

    @Override
    public void removeRouterLayoutContent(HasElement oldContent) {
        throw new UnsupportedOperationException("Use %s to show content instead"
                .formatted(WorkArea.class.getSimpleName()));
    }

    private MainViewSupport mainViewSupport() {
        return getApplicationContext().getBean(MainViewSupport.class);
    }
}