package io.jmix.flowui.action;

import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface OperationResultAction extends Action {

    OperationResult getOperationResult();

    void setNextStepSupplier(@Nullable Supplier<OperationResult> nextStepSupplier);

    void setSuccessHandler(@Nullable Runnable successHandler);

    void setFailHandler(@Nullable Runnable failHandler);
}
