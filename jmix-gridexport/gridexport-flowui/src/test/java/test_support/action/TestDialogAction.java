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

package test_support.action;

import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import io.jmix.gridexportflowui.action.ExportDialogContextAware;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action an export dialog shows by its type id. Reports every created instance to {@link TestDialogActionRecorder}
 * and remembers the export context it got (and whether it came before the target), how many times it was performed
 * and its target at that moment.
 */
public abstract class TestDialogAction extends ListDataComponentAction<TestDialogAction, Object>
        implements ExportDialogContextAware {

    @Nullable
    protected ExportDialogContext exportDialogContext;
    protected int performedCount;
    @Nullable
    protected ListDataComponent<Object> targetOnPerform;
    protected boolean contextSetBeforeTarget;

    protected TestDialogAction(String id, String text) {
        super(id);
        this.text = text;
    }

    @Autowired
    public void setRecorder(TestDialogActionRecorder recorder) {
        recorder.add(this);
    }

    @Override
    public void setExportDialogContext(ExportDialogContext context) {
        this.exportDialogContext = context;
    }

    @Override
    public void setTarget(@Nullable ListDataComponent<Object> target) {
        if (target != null) {
            contextSetBeforeTarget = exportDialogContext != null;
        }
        super.setTarget(target);
    }

    public boolean isContextSetBeforeTarget() {
        return contextSetBeforeTarget;
    }

    @Nullable
    public ExportDialogContext getExportDialogContext() {
        return exportDialogContext;
    }

    @Override
    public void execute() {
        performedCount++;
        targetOnPerform = target;
    }

    public int getPerformedCount() {
        return performedCount;
    }

    @Nullable
    public ListDataComponent<Object> getTargetOnPerform() {
        return targetOnPerform;
    }
}
