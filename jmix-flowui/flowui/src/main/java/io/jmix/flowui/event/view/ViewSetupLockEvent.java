/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.event.view;

import io.jmix.flowui.view.LockStatus;
import io.jmix.flowui.view.SupportEntityLock;
import io.jmix.flowui.view.View;
import org.springframework.context.ApplicationEvent;

/**
 * Represents an event that occurs during the setup of a lock mechanism
 * for a given view. The purpose of this event is to manage lock status
 * in views that support lock handling.
 *
 * @param <V> a type of view that extends {@link View} and implements {@link SupportEntityLock}.
 */
public class ViewSetupLockEvent<V extends View<?> & SupportEntityLock<?>> extends ApplicationEvent {

    protected LockStatus lockStatus = LockStatus.NOT_SUPPORTED;

    public ViewSetupLockEvent(V view) {
        super(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V getSource() {
        return (V) super.getSource();
    }

    public LockStatus getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(LockStatus lockStatus) {
        this.lockStatus = lockStatus;
    }
}
