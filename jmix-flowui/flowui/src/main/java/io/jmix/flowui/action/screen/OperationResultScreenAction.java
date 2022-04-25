package io.jmix.flowui.action.screen;


import io.jmix.flowui.action.OperationResultAction;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.util.UnknownOperationResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class OperationResultScreenAction<A extends OperationResultScreenAction<A, S>, S extends Screen>
        extends ScreenAction<A, S>
        implements OperationResultAction {

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
