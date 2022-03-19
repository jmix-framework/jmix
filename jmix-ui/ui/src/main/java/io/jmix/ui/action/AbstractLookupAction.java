/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.action;

import io.jmix.core.Messages;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.screen.*;
import io.jmix.ui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Base class for actions that should open a screen.
 *
 * @param <E> type of entity
 */
public abstract class AbstractLookupAction<E> extends BaseAction implements Action.ScreenOpeningAction {

    protected ScreenBuilders screenBuilders;
    protected Icons icons;
    protected Messages messages;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Predicate<LookupScreen.ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    public AbstractLookupAction(String id) {
        super(id);
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icons = icons;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
    }

    /**
     * Returns the lookup screen open mode if it was set by {@link #setOpenMode(OpenMode)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public OpenMode getOpenMode() {
        return screenInitializer.getOpenMode();
    }

    /**
     * Sets the lookup screen open mode.
     */
    @StudioPropertiesItem
    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        screenInitializer.setOpenMode(openMode);
    }

    /**
     * Returns the lookup screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the lookup screen id.
     */
    @StudioPropertiesItem
    @Override
    public void setScreenId(@Nullable String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the lookup screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public Class<? extends Screen> getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the lookup screen id.
     */
    @StudioPropertiesItem
    @Override
    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    /**
     * Sets the lookup screen options supplier. The supplier provides {@code ScreenOptions} to the
     * opened screen.
     * <p>
     * The preferred way to set the supplier is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "screenOptionsSupplier")
     * protected ScreenOptions petFieldLookupScreenOptionsSupplier() {
     *     return new MapScreenOptions(ParamsMap.of("someParameter", 10));
     * }
     * </pre>
     */
    @Override
    public void setScreenOptionsSupplier(Supplier<ScreenOptions> screenOptionsSupplier) {
        screenInitializer.setScreenOptionsSupplier(screenOptionsSupplier);
    }

    /**
     * Sets the lookup screen configurer. Use the configurer if you need to provide parameters to the
     * opened screen through setters.
     * <p>
     * The preferred way to set the configurer is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "screenConfigurer")
     * protected void petFieldLookupScreenConfigurer(Screen lookupScreen) {
     *     ((PetBrowse) lookupScreen).setSomeParameter(someValue);
     * }
     * </pre>
     */
    @Override
    public void setScreenConfigurer(Consumer<Screen> screenConfigurer) {
        screenInitializer.setScreenConfigurer(screenConfigurer);
    }

    /**
     * Sets the handler to be invoked when the lookup screen closes.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "afterCloseHandler")
     * protected void petFieldLookupAfterCloseHandler(AfterCloseEvent event) {
     *     CloseAction closeAction = event.getCloseAction();
     *     System.out.println("Closed with " + closeAction);
     * }
     * </pre>
     */
    @Override
    public void setAfterCloseHandler(Consumer<Screen.AfterCloseEvent> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the validator to be invoked when the user selects entities in the lookup screen.
     * <p>
     * The preferred way to set the validator is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "selectValidator")
     * protected void petFieldLookupSelectValidator(LookupScreen.ValidationContext&lt;Pet&gt; context) {
     *     return checkSelected(context.getSelectedItems());
     * }
     * </pre>
     */
    public void setSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    /**
     * Sets the function to transform selected in the lookup screen entities.
     * <p>
     * The preferred way to set the function is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petField.lookup", subject = "transformation")
     * protected Collection&lt;Pet&gt; petFieldLookupTransformation(Collection&lt;Pet&gt; entities) {
     *     return doTransform(entities);
     * }
     * </pre>
     */
    public void setTransformation(Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
    }
}
