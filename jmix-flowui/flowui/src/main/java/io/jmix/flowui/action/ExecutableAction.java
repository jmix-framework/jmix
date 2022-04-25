package io.jmix.flowui.action;

import io.jmix.flowui.SimilarToUi;
import io.jmix.flowui.kit.action.Action;

@SimilarToUi
public interface ExecutableAction extends Action {

    /**
     * Executes the {@link Action}
     */
    void execute();
}
