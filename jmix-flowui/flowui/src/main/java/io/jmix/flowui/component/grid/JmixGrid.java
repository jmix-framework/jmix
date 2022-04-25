package io.jmix.flowui.component.grid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSelectionModel;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.grid.GridDataItems;
import io.jmix.flowui.component.HasActions;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.LookupComponent.MultiSelectLookupComponent;
import io.jmix.flowui.component.SelectionChangeNotifier;
import io.jmix.flowui.component.delegate.GridDelegate;
import io.jmix.flowui.kit.action.Action;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public class JmixGrid<E> extends Grid<E> implements ListDataComponent<E>, SelectionChangeNotifier<Grid<E>, E>,
        MultiSelectLookupComponent<E>, HasActions, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;

    protected GridDelegate<E> gridDelegate;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        gridDelegate = createDelegate();
    }

    @SuppressWarnings("unchecked")
    protected GridDelegate<E> createDelegate() {
        return applicationContext.getBean(GridDelegate.class, this);
    }

    @Override
    public GridDataView<E> setItems(DataProvider<E, Void> dataProvider) {
        // TODO: gg, refactor
        if (dataProvider instanceof GridDataItems) {
            gridDelegate.setItems((GridDataItems<E>) dataProvider);
        }

        return super.setItems(dataProvider);
    }

    @Nullable
    @Override
    public E getSingleSelectedItem() {
        return gridDelegate.getSingleSelectedItem();
    }

    @Override
    public Set<E> getSelectedItems() {
        return gridDelegate.getSelectedItems();
    }

    @Override
    public void select(E item) {
        gridDelegate.select(item);
    }

    @Override
    public void select(Collection<E> items) {
        gridDelegate.select(items);
    }

    @Override
    public void deselect(E item) {
        gridDelegate.deselect(item);
    }

    @Override
    public void deselectAll() {
        gridDelegate.deselectAll();
    }

    @Nullable
    @Override
    public DataUnit getItems() {
        return gridDelegate.getItems();
    }

    @Override
    public boolean isMultiSelect() {
        return gridDelegate.isMultiSelect();
    }

    @Override
    public void addAction(Action action, int index) {
        gridDelegate.addAction(action, index);
    }

    @Override
    public void removeAction(Action action) {
        gridDelegate.removeAction(action);
    }

    @Override
    public Collection<Action> getActions() {
        return gridDelegate.getActions();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        return gridDelegate.getAction(id);
    }

    @Override
    public Registration addSelectionListener(SelectionListener<Grid<E>, E> listener) {
        return gridDelegate.addSelectionListener(listener);
    }

    @Override
    public void enableMultiSelect() {
        gridDelegate.enableMultiSelect();
    }

    @Override
    public GridSelectionModel<E> setSelectionMode(SelectionMode selectionMode) {
        GridSelectionModel<E> selectionModel = super.setSelectionMode(selectionMode);

        gridDelegate.onSelectionModelChange(selectionModel);

        return selectionModel;
    }
}
