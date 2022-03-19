/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.action.list;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ListAction;
import io.jmix.ui.builder.LookupBuilder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.meta.StudioPropertiesItem;
import io.jmix.ui.model.Nested;
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
 * Standard action for adding an entity to the list using its lookup screen.
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 * <p>
 * The action instance can be parameterized using the nested {@code properties} XML element or programmatically in the
 * screen controller.
 *
 * @param <E> type of entity
 */
@StudioAction(
        target = "io.jmix.ui.component.ListComponent",
        description = "Adds entities to the list using a lookup screen",
        availableInScreenWizard = true)
@ActionType(AddAction.ID)
public class AddAction<E> extends ListAction
        implements Action.AdjustWhenScreenReadOnly, Action.ScreenOpeningAction, Action.ExecutableAction {

    public static final String ID = "add";

    protected ScreenBuilders screenBuilders;
    protected AccessManager accessManager;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Predicate<LookupScreen.ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    public AddAction() {
        super(ID);
    }

    public AddAction(String id) {
        super(id);
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
     * &#64;Install(to = "petsTable.add", subject = "screenOptionsSupplier")
     * protected ScreenOptions petsTableAddScreenOptionsSupplier() {
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
     * &#64;Install(to = "petsTable.add", subject = "screenConfigurer")
     * protected void petsTableAddScreenConfigurer(Screen lookupScreen) {
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
     * &#64;Install(to = "petsTable.add", subject = "afterCloseHandler")
     * protected void petsTableAddAfterCloseHandler(AfterCloseEvent event) {
     *     if (event.closedWith(StandardOutcome.SELECT)) {
     *         System.out.println("Selected");
     *     }
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
     * &#64;Install(to = "petsTable.add", subject = "selectValidator")
     * protected void petsTableAddSelectValidator(LookupScreen.ValidationContext&lt;Pet&gt; context) {
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
     * &#64;Install(to = "petsTable.add", subject = "transformation")
     * protected Collection&lt;Pet&gt; petsTableAddTransformation(Collection&lt;Pet&gt; entities) {
     *     return doTransform(entities);
     * }
     * </pre>
     */
    public void setTransformation(Function<Collection<E>, Collection<E>> transformation) {
        this.transformation = transformation;
    }

    @Autowired
    protected void setIcons(Icons icons) {
        this.icon = icons.get(JmixIcon.ADD_ACTION);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.caption = messages.getMessage("actions.Add");
    }

    @Autowired
    public void setSecurity(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof ContainerDataUnit)) {
            return false;
        }

        ContainerDataUnit containerDataUnit = (ContainerDataUnit) target.getItems();

        MetaClass metaClass = containerDataUnit.getEntityMetaClass();
        if (metaClass == null) {
            return false;
        }

        if (containerDataUnit.getContainer() instanceof Nested) {
            Nested nestedContainer = (Nested) containerDataUnit.getContainer();

            MetaClass masterMetaClass = nestedContainer.getMaster().getEntityMetaClass();
            MetaProperty metaProperty = masterMetaClass.getProperty(nestedContainer.getProperty());

            UiEntityAttributeContext attributeContext =
                    new UiEntityAttributeContext(masterMetaClass, metaProperty.getName());
            accessManager.applyRegisteredConstraints(attributeContext);

            if (!attributeContext.canModify()) {
                return false;
            }
        }

        return super.isPermitted();
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    /**
     * Executes the action.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        if (target == null) {
            throw new IllegalStateException("AddAction target is not set");
        }

        LookupBuilder builder = screenBuilders.lookup(target);

        builder = screenInitializer.initBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        Screen lookupScreen = builder.build();

        screenInitializer.initScreen(lookupScreen);

        lookupScreen.show();
    }
}
