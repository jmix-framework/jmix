package io.jmix.flowui.component.delegate;

import com.google.common.base.Strings;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridNoneSelectionModel;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.event.SortEvent;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.EmptyDataUnit;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.data.binding.JmixBinding;
import io.jmix.flowui.data.grid.GridDataItems;
import io.jmix.flowui.data.provider.StringPresentationValueProvider;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AbstractGridDelegate<C extends Grid<E> & ListDataComponent<E> & HasActions, E>
        extends AbstractComponentDelegate<C>
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected MetadataTools metadataTools;
    protected MessageTools messageTools;
    protected UiComponents uiComponents;

    protected GridDataItems<E> dataBinding;

    protected Registration selectedItemChangeRegistration;
    protected Registration selectionListenerRegistration;
    protected Set<SelectionListener<Grid<E>, E>> selectionListeners = new HashSet<>();

    public AbstractGridDelegate(C component) {
        super(component);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        metadataTools = applicationContext.getBean(MetadataTools.class);
        messageTools = applicationContext.getBean(MessageTools.class);
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    protected void initComponent() {
        component.addSortListener(this::onSort);
    }

    protected void onSort(SortEvent<Grid<E>, GridSortOrder<E>> event) {
        if (!(dataBinding instanceof GridDataItems.Sortable)
                || !(dataBinding instanceof EntityDataUnit)) {
            return;
        }

        GridDataItems.Sortable<E> dataProvider = (GridDataItems.Sortable<E>) dataBinding;

        List<GridSortOrder<E>> sortOrders = event.getSortOrder();
        if (sortOrders.isEmpty()) {
            dataProvider.resetSortOrder();
        } else {
            GridSortOrder<E> sortOrder = sortOrders.get(0);

            Grid.Column<E> column = sortOrder.getSorted();
            if (column != null
                    && !Strings.isNullOrEmpty(column.getKey())) {
                MetaClass metaClass = ((EntityDataUnit) dataBinding).getEntityMetaClass();
                MetaPropertyPath mpp = metaClass.getPropertyPath(column.getKey());
                if (mpp != null) {
                    boolean ascending = SortDirection.ASCENDING.equals(sortOrder.getDirection());
                    dataProvider.sort(new Object[]{mpp}, new boolean[]{ascending});
                }
            }
        }
    }

    @Nullable
    public GridDataItems<E> getItems() {
        return dataBinding;
    }

    public void setItems(@Nullable GridDataItems<E> gridDataItems) {
        unbind();

        if (gridDataItems != null && isSupportedDataItems(gridDataItems)) {
            dataBinding = gridDataItems;

            if (dataBinding instanceof JmixBinding) {
                ((JmixBinding) dataBinding).bind();
            }

            selectedItemChangeRegistration = dataBinding
                    .addSelectedItemChangeListener(this::onDataProviderSelectedItemChange);

            if (component.getColumns().isEmpty()) {
                setupAutowiredColumns(gridDataItems);
            }
        }
    }

    @Nullable
    public E getSingleSelectedItem() {
        return getSelectionModel()
                .getFirstSelectedItem()
                .orElse(null);
    }

    public Set<E> getSelectedItems() {
        return getSelectionModel().getSelectedItems();
    }

    public void select(E item) {
        Preconditions.checkNotNullArgument(item);

        select(Collections.singletonList(item));
    }

    public void select(Collection<E> items) {
        if (CollectionUtils.isNotEmpty(items)) {
            if (isMultiSelect()) {
                //noinspection unchecked
                ((SelectionModel.Multi<Grid<E>, E>) getSelectionModel())
                        .updateSelection(new LinkedHashSet<>(items), Collections.emptySet());
            } else {
                getSelectionModel().select(items.iterator().next());
            }
        } else {
            deselectAll();
        }
    }

    public void deselect(E item) {
        getSelectionModel().deselect(item);
    }

    public void deselectAll() {
        getSelectionModel().deselectAll();
    }

    public boolean isMultiSelect() {
        return getSelectionModel() instanceof SelectionModel.Multi;
    }

    public void enableMultiSelect() {
        component.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    public Registration addSelectionListener(SelectionListener<Grid<E>, E> listener) {
        if (selectionListenerRegistration == null) {
            attachSelectionListener();
        }

        selectionListeners.add(listener);

        return () -> {
            selectionListeners.remove(listener);
            if (selectionListeners.isEmpty()) {
                detachSelectionListener();
            }
        };
    }

    public Grid.Column<E> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        return addColumnInternal(key, metaPropertyPath);
    }

    protected void unbind() {
        if (dataBinding != null) {
            if (dataBinding instanceof JmixBinding) {
                ((JmixBinding) dataBinding).unbind();
            }

            dataBinding = null;
            setupEmptyDataProvider();
        }

        if (selectedItemChangeRegistration != null) {
            selectedItemChangeRegistration.remove();
            selectedItemChangeRegistration = null;
        }
    }

    protected void setupEmptyDataProvider() {
        component.setItems(new ListDataProvider<>(Collections.emptyList()));
    }

    protected boolean isSupportedDataItems(GridDataItems<E> gridDataItems) {
        return true;
    }

    protected void onDataProviderSelectedItemChange(GridDataItems.SelectedItemChangeEvent<E> event) {
        // todo rp should we react on container select item change?
    }

    protected void setupAutowiredColumns(GridDataItems<E> gridDataItems) {
        Collection<MetaPropertyPath> paths = getAutowiredProperties(gridDataItems);

        for (MetaPropertyPath metaPropertyPath : paths) {
            MetaProperty property = metaPropertyPath.getMetaProperty();
            if (!property.getRange().getCardinality().isMany()
                    && !metadataTools.isSystem(property)) {
                addColumnInternal(metaPropertyPath);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Collection<MetaPropertyPath> getAutowiredProperties(GridDataItems<E> gridDataItems) {
        if (gridDataItems instanceof ContainerDataUnit) {
            CollectionContainer<E> container = ((ContainerDataUnit<E>) gridDataItems).getContainer();

            return container.getFetchPlan() != null ?
                    // if a fetchPlan is specified - use fetchPlan properties
                    metadataTools.getFetchPlanPropertyPaths(container.getFetchPlan(), container.getEntityMetaClass()) :
                    // otherwise use all properties from meta-class
                    metadataTools.getPropertyPaths(container.getEntityMetaClass());
        }

        if (gridDataItems instanceof EmptyDataUnit && gridDataItems instanceof EntityDataUnit) {
            return metadataTools.getPropertyPaths(((EntityDataUnit) gridDataItems).getEntityMetaClass());
        }

        return Collections.emptyList();
    }

    protected Grid.Column<E> addColumnInternal(MetaPropertyPath metaPropertyPath) {
        return addColumnInternal(metaPropertyPath.getMetaProperty().getName(), metaPropertyPath);
    }

    protected Grid.Column<E> addColumnInternal(String key, MetaPropertyPath metaPropertyPath) {
        ValueProvider<E, ?> valueProvider = getValueProvider(metaPropertyPath);

        Grid.Column<E> column = component.addColumn(valueProvider);
        column.setKey(key);

        initColumn(column, metaPropertyPath);

        return column;
    }

    protected ValueProvider<E, ?> getValueProvider(MetaPropertyPath metaPropertyPath) {
        return new StringPresentationValueProvider<>(metaPropertyPath, metadataTools);
    }

    protected void initColumn(Grid.Column<E> column, MetaPropertyPath metaPropertyPath) {
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
        column.setSortable(true);

        MetaClass propertyMetaClass = metadataTools.getPropertyEnclosingMetaClass(metaPropertyPath);
        column.setHeader(messageTools.getPropertyCaption(propertyMetaClass, metaProperty.getName()));
    }

    protected void onSelectionChange(SelectionEvent<Grid<E>, E> event) {
        for (SelectionListener<Grid<E>, E> listener : selectionListeners) {
            listener.selectionChange(event);
        }
    }

    public void onSelectionModelChange(SelectionModel<Grid<E>, E> selectionModel) {
        if (selectionModel instanceof GridNoneSelectionModel) {
            detachSelectionListener();
            return;
        }

        if (!selectionListeners.isEmpty()) {
            attachSelectionListener();
        }
    }

    protected void attachSelectionListener() {
        detachSelectionListener();

        if (!(getSelectionModel() instanceof GridNoneSelectionModel)) {
            selectionListenerRegistration = getSelectionModel().addSelectionListener(this::onSelectionChange);
        }
    }

    protected void detachSelectionListener() {
        if (selectionListenerRegistration != null) {
            selectionListenerRegistration.remove();
            selectionListenerRegistration = null;
        }
    }

    protected GridSelectionModel<E> getSelectionModel() {
        return component.getSelectionModel();
    }
}
