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

package io.jmix.flowui.component.grid.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.AbstractGridExtension;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.editor.*;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.shared.Registration;
import elemental.json.JsonObject;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.component.*;
import io.jmix.flowui.component.SupportsStatusChangeHandler.StatusContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.grid.DataGridDataProviderChangeObserver;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.data.*;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewValidation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataGridEditorImpl<T> extends AbstractGridExtension<T>
        implements DataGridEditor<T>, DataGridDataProviderChangeObserver {

    private static final Logger log = LoggerFactory.getLogger(DataGridEditorImpl.class);
    private static final String EDITING = "_editing";

    protected ApplicationContext applicationContext;

    protected final Map<Class<?>, List<?>> listeners = new HashMap<>();
    protected SerializableConsumer<ExecutionContext> editItemRequest;

    protected Consumer<StatusContext<?>> defaultComponentStatusHandler;
    protected Consumer<ValidationErrors> validationErrorsHandler;

    protected T edited;
    protected boolean buffered;
    protected boolean saving;

    protected Map<T, DataGridEditorValueSourceProvider<T>> itemValueSourceProviders;
    protected Map<Grid.Column<T>, Component> columnEditorComponents;

    private EventBus eventBus;

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }

    public DataGridEditorImpl(Grid<T> grid, ApplicationContext applicationContext) {
        super(grid);
        this.applicationContext = applicationContext;

        initGrid(grid);
    }

    protected void initGrid(Grid<T> grid) {
        grid.addItemClickListener(this::handleItemClick);
    }

    @Override
    public Editor<T> setBinder(Binder<T> binder) {
        throw new UnsupportedOperationException(DataGridEditor.class +
                " doesn't support " + Binder.class.getSimpleName());
    }

    @Override
    public Binder<T> getBinder() {
        throw new UnsupportedOperationException(DataGridEditor.class +
                " doesn't support " + Binder.class.getSimpleName());
    }

    @Override
    public DataGridEditor<T> setBuffered(boolean buffered) {
        closeIfOpen();

        this.buffered = buffered;
        return this;
    }

    protected void closeIfOpen() {
        if (!isOpen()) {
            return;
        }

        if (isBuffered()) {
            cancel();
        } else {
            closeEditor();
        }
    }

    @Override
    public boolean isBuffered() {
        return buffered;
    }

    @Override
    public boolean isSaving() {
        return saving;
    }

    @Override
    public boolean isOpen() {
        return edited != null;
    }

    @Override
    public boolean save() {
        if (isOpen() && isBuffered()) {
            if (isEditorEditComponentsValid()) {
                writeEditComponents();
                fireSaveEvent(new EditorSaveEvent<>(this, edited));
                closeInternal();
                return true;
            }
        }
        return false;
    }

    protected boolean isEditorEditComponentsValid() {
        ValidationErrors validationErrors = validateEditor();
        if (!validationErrors.isEmpty() && validationErrorsHandler != null) {
            validationErrorsHandler.accept(validationErrors);
        }

        return validationErrors.isEmpty();
    }

    protected ValidationErrors validateEditor() {
        if (MapUtils.isEmpty(columnEditorComponents)) {
            return ValidationErrors.none();
        }

        ViewValidation viewValidation = getViewValidation();
        ValidationErrors allErrors = new ValidationErrors();

        columnEditorComponents.values().forEach(component -> {
            ValidationErrors errors;
            if (component instanceof SupportsValidation) {
                errors = viewValidation.validateUiComponent(component);
            } else if (UiComponentUtils.isContainer(component)) {
                errors = viewValidation.validateUiComponents(UiComponentUtils.getComponents(component));
            } else {
                errors = ValidationErrors.none();
            }

            allErrors.addAll(errors);
        });

        return allErrors;
    }

    protected void writeEditComponents() {
        if (MapUtils.isEmpty(columnEditorComponents)) {
            return;
        }

        log.debug("Writing edit component values to item: " + edited);

        saving = true;

        columnEditorComponents.values().forEach(this::writeComponent);

        saving = false;
    }

    protected void writeComponent(Component component) {
        if (component instanceof SupportsValueSource) {
            ValueSource<?> valueSource = ((SupportsValueSource<?>) component).getValueSource();
            if (valueSource instanceof BufferedDataUnit) {
                ((BufferedDataUnit) valueSource).write();
            }
        } else if (UiComponentUtils.isContainer(component)) {
            UiComponentUtils.getOwnComponents(component).forEach(this::writeComponent);
        }
    }

    @Override
    public void cancel() {
        fireCancelEvent(new EditorCancelEvent<>(this, edited));
        closeInternal();
    }

    @Override
    public void closeEditor() {
        if (isOpen() && isBuffered()) {
            throw new UnsupportedOperationException(
                    "Buffered editor should be closed using save() or cancel()");
        }
        closeInternal();
    }

    @Override
    public void editItem(T item) {
        Preconditions.checkNotNullArgument(item, "Editor can't edit null");

        final T it = item;
        if (editItemRequest == null) {
            editItemRequest = context -> {
                requestEditItem(it);
                editItemRequest = null;
            };
            getGrid().getElement().getNode().runWhenAttached(
                    ui -> ui.getInternals().getStateTree().beforeClientResponse(
                            getGrid().getElement().getNode(), editItemRequest));
        }
    }

    protected void requestEditItem(T item) {
        validateCanEdit(item);

        closeInternal();
        edited = item;

        refresh(item);

        fireOpenEvent(new EditorOpenEvent<>(this, edited));
    }

    @Override
    public void refresh() {
        if (!isOpen()) {
            return;
        }
        refresh(edited);
    }

    @Override
    public T getItem() {
        return edited;
    }

    @Override
    public DataGrid<T> getGrid() {
        return ((DataGrid<T>) super.getGrid());
    }

    @Override
    public void generateData(@Nullable T item, JsonObject jsonObject) {
        if (item != null && item.equals(edited)) {
            jsonObject.put(EDITING, true);
        } else {
            jsonObject.remove(EDITING);
        }
    }

    protected void closeInternal() {
        if (edited != null) {
            T oldEdited = edited;
            edited = null;

            refresh(oldEdited);
            clearItemValueSourceProviders(oldEdited);
            clearEditorComponents();
            fireCloseEvent(new EditorCloseEvent<>(this, oldEdited));
        }
    }

    protected void handleItemClick(ItemClickEvent<T> event) {
        DataProvider<T, ?> dataProvider = getGrid().getDataProvider();
        if (!isBuffered() && edited != null && !dataProvider.getId(edited)
                .equals(dataProvider.getId(event.getItem()))) {
            closeInternal();
        }
    }

    protected void validateCanEdit(T item) {
        if (isBuffered() && edited != null) {
            throw new IllegalStateException("Editing item " + item
                    + " failed. Item editor is already editing item " + edited);
        }

        if (!getGrid().getDataCommunicator().getKeyMapper().has(item)) {
            throw new IllegalStateException("The item " + item
                    + " is not in the backing data provider");
        }
    }

    protected ValueSourceProvider createValueSourceProvider(T item) {
        if (itemValueSourceProviders == null) {
            itemValueSourceProviders = new WeakHashMap<>();
        }

        DataGridEditorValueSourceProvider<T> valueSourceProvider = itemValueSourceProviders.get(item);
        if (valueSourceProvider != null) {
            return valueSourceProvider;
        }

        log.debug("Creating a new item value source provider for: " + item);

        InstanceContainer<T> instanceContainer = createInstanceContainer(item);
        valueSourceProvider = new DataGridEditorValueSourceProvider<>(this, instanceContainer);

        itemValueSourceProviders.put(item, valueSourceProvider);

        return valueSourceProvider;
    }

    protected InstanceContainer<T> createInstanceContainer(T item) {
        DataComponents factory = getDataComponents();
        MetaClass metaClass = getEntityMetaClass();

        InstanceContainer<T> instanceContainer;
        if (metaClass instanceof KeyValueMetaClass) {
            //noinspection unchecked
            instanceContainer = (InstanceContainer<T>) factory.createKeyValueContainer(metaClass);
        } else {
            instanceContainer = factory.createInstanceContainer(metaClass.getJavaClass());
        }
        instanceContainer.setItem(item);

        return instanceContainer;
    }

    protected void clearItemValueSourceProviders(@Nullable T item) {
        if (MapUtils.isEmpty(itemValueSourceProviders)) {
            return;
        }

        if (item != null) {
            log.debug("Clearing item value source provider for: " + item);

            DataGridEditorValueSourceProvider<T> removed = itemValueSourceProviders.remove(item);
            if (removed != null) {
                detachItemContainer(removed);
            }
        } else {
            log.debug("Clearing all item value source providers");

            // detach instance containers from entities explicitly
            itemValueSourceProviders.values().forEach(this::detachItemContainer);
            itemValueSourceProviders.clear();
        }
    }

    protected void detachItemContainer(DataGridEditorValueSourceProvider<T> valueSourceProvider) {
        valueSourceProvider.getContainer().setItem(null);
    }

    protected void clearEditorComponents() {
        if (MapUtils.isNotEmpty(columnEditorComponents)) {
            log.debug("Clearing editor components");

            columnEditorComponents.clear();
        }
    }

    @Override
    public void dataProviderChanged() {
        clearItemValueSourceProviders(null);
    }

    protected MetaClass getEntityMetaClass() {
        DataUnit items = getGrid().getItems();
        if (items instanceof EntityDataUnit) {
            return ((EntityDataUnit) items).getEntityMetaClass();
        }

        throw new IllegalStateException(getGrid().getClass().getSimpleName() +
                " items is null or does not implement " + EntityDataUnit.class.getSimpleName());
    }

    @Override
    public void initColumnDefaultEditorComponent(String property) {
        setColumnEditorComponent(property, new DefaultEditComponentGenerator(property));
    }

    @Override
    public void setColumnEditorComponent(String property,
                                         Function<EditComponentGenerationContext<T>, Component> generator) {
        Grid.Column<T> column = getGrid().getColumnByKey(property);
        setColumnEditorComponent(column, property, generator);
    }

    @Override
    public void initColumnDefaultEditorComponent(Grid.Column<T> column, String property) {
        setColumnEditorComponent(column, property, new DefaultEditComponentGenerator(property));
    }

    @Override
    public void setColumnEditorComponent(Grid.Column<T> column, String property,
                                         Function<EditComponentGenerationContext<T>, Component> generator) {
        Preconditions.checkNotNullArgument(column);
        column.setEditorComponent(item -> {
            log.debug(String.format("Generating edit component for item: %s; property: %s", item, property));

            ValueSourceProvider valueSourceProvider = createValueSourceProvider(item);
            EditComponentGenerationContext<T> generationContext = new EditComponentGenerationContext<>(item,
                    valueSourceProvider, this::defaultComponentStatusHandler);

            Component editComponent = generator.apply(generationContext);

            if (isBuffered()) {
                registerEditComponent(column, editComponent);
            }

            return editComponent;
        });
    }

    protected void defaultComponentStatusHandler(StatusContext<?> statusContext) {
        if (defaultComponentStatusHandler != null) {
            defaultComponentStatusHandler.accept(statusContext);
        } else {
            handleComponentStatus(statusContext);
        }
    }

    protected void handleComponentStatus(StatusContext<?> statusContext) {
        Component component = statusContext.getComponent();
        if (component instanceof HasTitle) {
            ((HasTitle) component).setTitle(statusContext.getDescription());
        }
    }

    @Override
    public void setDefaultComponentStatusHandler(@Nullable Consumer<StatusContext<?>> handler) {
        this.defaultComponentStatusHandler = handler;
    }

    @Override
    public void setValidationErrorsHandler(@Nullable Consumer<ValidationErrors> validationErrorsHandler) {
        this.validationErrorsHandler = validationErrorsHandler;
    }

    protected void registerEditComponent(Grid.Column<T> column, Component editComponent) {
        if (columnEditorComponents == null) {
            columnEditorComponents = new WeakHashMap<>();
        }

        columnEditorComponents.put(column, editComponent);
    }

    @Override
    public Registration addSaveListener(EditorSaveListener<T> listener) {
        return addListener(EditorSaveListener.class, listener);
    }

    @Override
    public Registration addCancelListener(EditorCancelListener<T> listener) {
        return addListener(EditorCancelListener.class, listener);
    }

    @Override
    public Registration addOpenListener(EditorOpenListener<T> listener) {
        return addListener(EditorOpenListener.class, listener);
    }

    @Override
    public Registration addCloseListener(EditorCloseListener<T> listener) {
        return addListener(EditorCloseListener.class, listener);
    }

    protected <L> Registration addListener(Class<L> listenerType, L listener) {
        @SuppressWarnings("unchecked")
        List<L> list = (List<L>) listeners.computeIfAbsent(listenerType,
                key -> Collections.synchronizedList(new ArrayList<>(1)));
        list.add(listener);

        return () -> list.remove(listener);
    }

    @SuppressWarnings("unchecked")
    protected void fireOpenEvent(EditorOpenEvent<T> event) {
        List<EditorOpenListener<T>> list = (List<EditorOpenListener<T>>) listeners
                .get(EditorOpenListener.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Collections.unmodifiableList(list)
                .forEach(listener -> listener.onEditorOpen(event));
    }

    @SuppressWarnings("unchecked")
    protected void fireCancelEvent(EditorCancelEvent<T> event) {
        List<EditorCancelListener<T>> list = (List<EditorCancelListener<T>>) listeners
                .get(EditorCancelListener.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Collections.unmodifiableList(list)
                .forEach(listener -> listener.onEditorCancel(event));
    }

    @SuppressWarnings("unchecked")
    protected void fireSaveEvent(EditorSaveEvent<T> event) {
        List<EditorSaveListener<T>> list = (List<EditorSaveListener<T>>) listeners
                .get(EditorSaveListener.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Collections.unmodifiableList(list)
                .forEach(listener -> listener.onEditorSave(event));
    }

    @SuppressWarnings("unchecked")
    protected void fireCloseEvent(EditorCloseEvent<T> event) {
        List<EditorCloseListener<T>> list = (List<EditorCloseListener<T>>) listeners
                .get(EditorCloseListener.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        Collections.unmodifiableList(list)
                .forEach(listener -> listener.onEditorClose(event));
    }

    protected DataComponents getDataComponents() {
        return applicationContext.getBean(DataComponents.class);
    }

    protected UiComponentsGenerator getUiComponentsGenerator() {
        return applicationContext.getBean(UiComponentsGenerator.class);
    }

    protected ViewValidation getViewValidation() {
        return applicationContext.getBean(ViewValidation.class);
    }

    protected class DefaultEditComponentGenerator
            implements Function<EditComponentGenerationContext<T>, Component> {

        protected String property;

        public DefaultEditComponentGenerator(String property) {
            this.property = property;
        }

        @Override
        public Component apply(EditComponentGenerationContext<T> generationContext) {
            ComponentGenerationContext context = new ComponentGenerationContext(getEntityMetaClass(), property);
            context.setValueSource(generationContext.getValueSourceProvider().getValueSource(property));

            Component editComponent = getUiComponentsGenerator().generate(context);

            if (editComponent instanceof SupportsStatusChangeHandler) {
                //noinspection unchecked,rawtypes
                ((SupportsStatusChangeHandler) editComponent).setStatusChangeHandler(generationContext.getStatusHandler());
            }

            if (editComponent instanceof HasSize) {
                ((HasSize) editComponent).setWidthFull();
            }

            return editComponent;
        }
    }
}
