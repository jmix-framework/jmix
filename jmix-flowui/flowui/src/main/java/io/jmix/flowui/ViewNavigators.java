package io.jmix.flowui;

import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.*;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_ViewNavigators")
public class ViewNavigators {

    protected DetailViewNavigationProcessor detailViewNavigationProcessor;
    protected ListViewNavigationProcessor listViewNavigationProcessor;
    protected ViewNavigationProcessor viewNavigationProcessor;

    public ViewNavigators(DetailViewNavigationProcessor detailViewNavigationProcessor,
                          ListViewNavigationProcessor listViewNavigationProcessor,
                          ViewNavigationProcessor viewNavigationProcessor) {
        this.detailViewNavigationProcessor = detailViewNavigationProcessor;
        this.listViewNavigationProcessor = listViewNavigationProcessor;
        this.viewNavigationProcessor = viewNavigationProcessor;
    }

    public <E> DetailViewNavigator<E> detailView(Class<E> entityClass) {
        checkNotNullArgument(entityClass);
        return new DetailViewNavigator<>(entityClass, detailViewNavigationProcessor::processNavigation);
    }

    public <E> DetailViewNavigator<E> detailView(Class<E> entityClass, View<?> parent) {
        checkNotNullArgument(entityClass);
        return detailView(entityClass)
                .withBackNavigationTarget(parent.getClass());
    }

    public <E> DetailViewNavigator<E> detailView(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        Class<E> beanType = getBeanType(listDataComponent);

        DetailViewNavigator<E> navigation =
                new DetailViewNavigator<>(beanType, detailViewNavigationProcessor::processNavigation);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            navigation.editEntity(selected);
        }

        return navigation;
    }

    public <E> DetailViewNavigator<E> detailView(ListDataComponent<E> listDataComponent, View<?> parent) {
        return detailView(listDataComponent)
                .withBackNavigationTarget(parent.getClass());
    }

    // TODO: gg, public <E> NavigationBuilder<E> detailView(EntityPicker<E> entityPicker) {}

    public <E> ListViewNavigator<E> listView(Class<E> entityClass) {
        checkNotNullArgument(entityClass);

        return new ListViewNavigator<>(entityClass, listViewNavigationProcessor::processNavigation);
    }

    public ViewNavigator view(Class<? extends View> viewClass) {
        return new ViewNavigator(viewNavigationProcessor::processNavigation)
                .withViewClass(viewClass);
    }

    public ViewNavigator view(String viewId) {
        return new ViewNavigator(viewNavigationProcessor::processNavigation)
                .withViewId(viewId);
    }

    protected  <E> Class<E> getBeanType(ListDataComponent<E> listDataComponent) {
        DataUnit items = listDataComponent.getItems();
        if (items instanceof HasType) {
            //noinspection unchecked
            return ((HasType<E>) items).getType();
        } else {
            throw new IllegalStateException(String.format("Component '%s' is not bound to data " +
                    "or unable to determine type of items", listDataComponent));
        }
    }
}
