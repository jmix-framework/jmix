/*
 * Copyright 2023 Haulmont.
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

package io.jmix.ui.action.screen;

import io.jmix.ui.action.AbstractScreenAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.util.UnknownOperationResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class OperationResultScreenAction<A extends OperationResultScreenAction<A, S>, S extends Screen>
        extends AbstractScreenAction<A, S>
        implements Action.OperationResultAction {

    protected Supplier<OperationResult> nextStepSupplier;
    protected Runnable successHandler;
    protected Runnable failHandler;
    protected OperationResult operationResult = new UnknownOperationResult();

    public OperationResultScreenAction(String id) {
        super(id);
    }

    @Override
    public OperationResult getOperationResult() {
        return operationResult;
    }

    @Override
    public void setNextStepSupplier(@Nullable Supplier<OperationResult> nextStepSupplier) {
        this.nextStepSupplier = nextStepSupplier;
    }

    @SuppressWarnings("unchecked")
    public A withNextStepSupplier(@Nullable Supplier<OperationResult> nextStepSupplier) {
        setNextStepSupplier(nextStepSupplier);
        return ((A) this);
    }

    @Override
    public void setSuccessHandler(@Nullable Runnable successHandler) {
        this.successHandler = successHandler;
    }

    @SuppressWarnings("unchecked")
    public A withSuccessHandler(@Nullable Runnable successHandler) {
        setSuccessHandler(successHandler);
        return ((A) this);
    }

    @Override
    public void setFailHandler(@Nullable Runnable failHandler) {
        this.failHandler = failHandler;
    }

    @SuppressWarnings("unchecked")
    public A withFailHandler(@Nullable Runnable failHandler) {
        setFailHandler(failHandler);
        return ((A) this);
    }

    @Override
    public void execute() {
        if (nextStepSupplier != null) {
            operationResult = operationResult.compose(nextStepSupplier);
        }

        if (successHandler != null) {
            operationResult = operationResult.then(successHandler);
        }

        if (failHandler != null) {
            operationResult = operationResult.otherwise(failHandler);
        }
    }
}
