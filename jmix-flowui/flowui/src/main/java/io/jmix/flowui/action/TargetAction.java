package io.jmix.flowui.action;

import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;

public interface TargetAction<C> extends Action {

    @Nullable
    C getTarget();

    void setTarget(@Nullable C target);
}
