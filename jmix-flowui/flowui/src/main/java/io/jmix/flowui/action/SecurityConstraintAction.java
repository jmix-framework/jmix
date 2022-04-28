package io.jmix.flowui.action;

import io.jmix.core.security.EntityOp;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;

public interface SecurityConstraintAction extends Action {

    void setConstraintEntityOp(@Nullable EntityOp entityOp);

    @Nullable
    EntityOp getConstraintEntityOp();
}
