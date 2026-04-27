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

package io.jmix.flowui.action.view;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.View;
import io.micrometer.observation.Observation;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract class ViewAction<A extends ViewAction<A, V>, V extends View<?>> extends SecuredBaseAction<A>
        implements TargetAction<V>, ExecutableAction {

    protected V target;

    public ViewAction(String id) {
        super(id);

        initAction();
    }

    protected void initAction() {
        // hook to be implemented
    }

    @Nullable
    @Override
    public V getTarget() {
        return target;
    }

    @Override
    public void setTarget(@Nullable V target) {
        if (!Objects.equals(this.target, target)) {
            this.target = target;

            refreshState();
        }
    }

    public A withTarget(@Nullable V target) {
        setTarget(target);
        return self();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasListener(ActionPerformedEvent.class)) {
            getUiObservationSupport()
                    .map(support -> support.createActionExecutionObservation(this))
                    .orElse(Observation.NOOP)
                    .observe(this::execute);
        } else {
            super.actionPerform(component);
        }
    }

    @Override
    protected boolean isApplicable() {
        return super.isApplicable() && target != null;
    }

    protected void checkTarget() {
        if (target == null) {
            throw new IllegalStateException(String.format("%s target is not set", getClass().getSimpleName()));
        }
    }
}
