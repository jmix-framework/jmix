/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.testassist;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.testassist.dialog.OpenedDialogs;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.flowui.testassist.notification.OpenedNotifications;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * Class provides helper methods for testing.
 */
public final class UiTestUtils {

    private static final Method VALIDATE_VIEW_METHOD =
            Objects.requireNonNull(ReflectionUtils.findMethod(StandardDetailView.class, "validateView"));
    private static ApplicationContext applicationContext;

    static {
        ReflectionUtils.makeAccessible(VALIDATE_VIEW_METHOD);
    }

    private UiTestUtils() {
    }

    @Internal
    static void setApplicationContext(ApplicationContext applicationContext) {
        UiTestUtils.applicationContext = applicationContext;
    }

    /**
     * Returns instance of currently navigated view. Usage example:
     * <pre>
     * &#64;Test
     * public void navigateToUserListView() {
     *     viewNavigators.view(UserListView.class)
     *             .navigate();
     *
     *     UserListView view = UiTestUtils.getCurrentView();
     * }
     * </pre>
     *
     * @param <T> type of navigated view
     * @return instance of the currently navigated view
     */
    @SuppressWarnings("unchecked")
    public static <T extends View<?>> T getCurrentView() {
        List<HasElement> targetsChain = UI.getCurrent().getInternals().getActiveRouterTargetsChain();
        if (CollectionUtils.isEmpty(targetsChain)) {
            throw new IllegalStateException("No active router targets. Check that " + View.class.getSimpleName() +
                    " navigation was performed before getting current view");
        }
        return (T) targetsChain.get(0);
    }

    /**
     * Returns a component defined in the view by the component id.
     *
     * @throws IllegalArgumentException if not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getComponent(View<?> view, String componentId) {
        return (T) UiComponentUtils.getComponent(view, componentId);
    }

    /**
     * Validates provided {@link View} by calling {@code StandardDetailView#validateView()} method from it.
     *
     * @param view detail view to validate
     * @return errors if validation failed, otherwise empty object will be returned
     */
    public static ValidationErrors validateView(StandardDetailView<?> view) {
        return (ValidationErrors) Objects.requireNonNull(ReflectionUtils.invokeMethod(VALIDATE_VIEW_METHOD, view));
    }

    /**
     * Returns an immutable list of {@link NotificationInfo} objects in the order they were opened.
     * <p>
     * Example of the order in which notifications are stored:
     * <ul>
     *     <li>first opened notification has index {@code 0}</li>
     *     <li>second opened notification has index {@code 1}</li>
     *     <li>last opened notification has index {@code openedNotifications.size() - 1}</li>
     * </ul>
     *
     * @return immutable list of {@link NotificationInfo} in order of opening
     */
    public static List<NotificationInfo> getOpenedNotifications() {
        return applicationContext.getBean(OpenedNotifications.class).getNotifications();
    }

    /**
     * @return the most recent opened {@link NotificationInfo} or {@code null} if no opened notifications
     */
    @Nullable
    public static NotificationInfo getLastOpenedNotification() {
        return applicationContext.getBean(OpenedNotifications.class).getLastNotification();
    }

    /**
     * Returns an immutable list of {@link DialogInfo} objects in the order they were opened.
     * <p>
     * Example of the order in which dialogs are stored:
     * <ul>
     *     <li>first opened dialog has index {@code 0}</li>
     *     <li>second opened dialog has index {@code 1}</li>
     *     <li>last opened dialog has index {@code openedDialogs.size() - 1}</li>
     * </ul>
     *
     * @return immutable list of {@link DialogInfo}s in order of opening
     */
    public static List<DialogInfo> getOpenedDialogs() {
        return applicationContext.getBean(OpenedDialogs.class).getDialogs();
    }

    /**
     * @return the most recent opened {@link DialogInfo} or {@code null} if no opened dialogs
     */
    @Nullable
    public static DialogInfo getLastOpenedDialog() {
        return applicationContext.getBean(OpenedDialogs.class).getLastDialog();
    }
}
