package io.jmix.flowui.action;

import io.jmix.flowui.SimilarToUi;

/**
 * Interface to be implemented by actions which may adjust
 * their 'enabled' state according to the screen read-only mode.
 */
@SimilarToUi
public interface AdjustWhenScreenReadOnly {

    /**
     * @return whether this action must be disabled when a screen in the read-only mode
     */
    default boolean isDisabledWhenScreenReadOnly() {
        return true;
    }
}