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

package io.jmix.flowui.facet.dataloadcoordinator;

import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.model.DataLoader;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Base class of load triggers that set a loader parameter from a source, such as a data container
 * or a visual component, and then load data.
 * <p>
 * If the trigger is created with a {@link DataLoadCoordinator}, then before loading it also sets
 * the parameters of the coordinator's other triggers of this type that belong to the same loader,
 * if the loader does not have these parameters yet. So the loader gets a value for each of its
 * parameters, even if their sources have not changed yet. Parameter values that the loader
 * already has, for example set in code or by another trigger, are kept.
 */
@NullMarked
public abstract class AbstractParameterLoadTrigger implements DataLoadCoordinator.Trigger {

    protected final DataLoader loader;
    protected final String param;
    @Nullable
    protected final DataLoadCoordinator coordinator;

    protected AbstractParameterLoadTrigger(DataLoader loader, String param,
                                           @Nullable DataLoadCoordinator coordinator) {
        checkNotNullArgument(loader);
        checkNotNullArgument(param);

        this.loader = loader;
        this.param = param;
        this.coordinator = coordinator;
    }

    /**
     * Returns the name of the loader parameter whose value is taken from the source.
     */
    public String getParam() {
        return param;
    }

    /**
     * Sets the loader parameter to the current value of the source without loading data.
     */
    public void setParameterFromSource() {
        loader.setParameter(param, getParameterValue());
    }

    /**
     * Returns the current value of the source to be set to the loader parameter.
     */
    @Nullable
    protected abstract Object getParameterValue();

    /**
     * Sets the parameter of this trigger and the missing parameters of related triggers
     * from their sources and loads data.
     */
    protected void load() {
        setMissingRelatedParameters();
        setParameterFromSource();
        loader.load();
    }

    protected void setMissingRelatedParameters() {
        if (coordinator == null) {
            return;
        }

        for (DataLoadCoordinator.Trigger trigger : coordinator.getTriggers()) {
            if (trigger.getLoader() == loader
                    && trigger instanceof AbstractParameterLoadTrigger parameterTrigger
                    && !parameterTrigger.getParam().equals(param)
                    && !loader.getParameters().containsKey(parameterTrigger.getParam())) {
                parameterTrigger.setParameterFromSource();
            }
        }
    }

    @Override
    public DataLoader getLoader() {
        return loader;
    }
}
