/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.gridexportflowui.action.ExportDialogActionContributor;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds one action to the export dialog while enabled, and remembers the context, the actions it returned and the
 * clicks it got.
 */
public class TestExportDialogActionContributor implements ExportDialogActionContributor, Ordered {

    protected final int order;
    protected final String actionText;

    protected boolean enabled;
    protected boolean actionVisible = true;
    protected final List<Action> returnedActions = new ArrayList<>();
    @Nullable
    protected ExportDialogContext lastContext;
    protected int performedCount;

    public TestExportDialogActionContributor(int order, String actionText) {
        this.order = order;
        this.actionText = actionText;
    }

    @Override
    public List<Action> getActions(ExportDialogContext context) {
        lastContext = context;
        if (!enabled) {
            return List.of();
        }
        BaseAction action = new BaseAction("test_" + actionText)
                .withText(actionText)
                .withHandler(event -> performedCount++);
        action.setVisible(actionVisible);
        returnedActions.add(action);
        return List.of(action);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setActionVisible(boolean actionVisible) {
        this.actionVisible = actionVisible;
    }

    public List<Action> getReturnedActions() {
        return returnedActions;
    }

    @Nullable
    public ExportDialogContext getLastContext() {
        return lastContext;
    }

    public int getPerformedCount() {
        return performedCount;
    }

    public void reset() {
        enabled = false;
        actionVisible = true;
        returnedActions.clear();
        lastContext = null;
        performedCount = 0;
    }
}
