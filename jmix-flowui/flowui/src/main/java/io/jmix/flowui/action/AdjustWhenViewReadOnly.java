package io.jmix.flowui.action;

/**
 * Interface to be implemented by actions which may adjust
 * their 'enabled' state according to the view read-only mode.
 */
public interface AdjustWhenViewReadOnly {

    /**
     * @return whether this action must be disabled when a view in the read-only mode
     */
    default boolean isDisabledWhenViewReadOnly() {
        return true;
    }
}