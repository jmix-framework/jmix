package io.jmix.flowui.view;

import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ViewActions {

    default void addAction(Action action) {
        addAction(action, getActions().size());
    }

    void addAction(Action action, int index);

    void removeAction(Action action);

    default void removeAction(String id) {
        Action action = getAction(id);
        if (action != null) {
            removeAction(action);
        }
    }

    default void removeAllActions() {
        getActions().forEach(this::removeAction);
    }

    Collection<Action> getActions();

    @Nullable
    Action getAction(String id);
}
