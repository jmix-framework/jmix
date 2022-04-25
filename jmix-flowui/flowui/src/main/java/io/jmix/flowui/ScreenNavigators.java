package io.jmix.flowui;

import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.HasType;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.screen.Screen;
import io.jmix.flowui.screen.navigation.*;
import org.springframework.stereotype.Component;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component("flowui_ScreenNavigators")
public class ScreenNavigators {

    protected EditorNavigationProcessor editorNavigationProcessor;
    protected BrowserNavigationProcessor browserNavigationProcessor;
    protected ScreenNavigationProcessor screenNavigationProcessor;

    public ScreenNavigators(EditorNavigationProcessor editorNavigationProcessor,
                            BrowserNavigationProcessor browserNavigationProcessor,
                            ScreenNavigationProcessor screenNavigationProcessor) {
        this.editorNavigationProcessor = editorNavigationProcessor;
        this.browserNavigationProcessor = browserNavigationProcessor;
        this.screenNavigationProcessor = screenNavigationProcessor;
    }

    public <E> EditorNavigator<E> editor(Class<E> entityClass) {
        checkNotNullArgument(entityClass);
        return new EditorNavigator<>(entityClass, editorNavigationProcessor::processNavigation);
    }

    public <E> EditorNavigator<E> editor(Class<E> entityClass, Screen parent) {
        checkNotNullArgument(entityClass);
        return editor(entityClass)
                .withBackNavigationTarget(parent.getClass());
    }

    public <E> EditorNavigator<E> editor(ListDataComponent<E> listDataComponent) {
        checkNotNullArgument(listDataComponent);

        Class<E> beanType = getBeanType(listDataComponent);

        EditorNavigator<E> navigation =
                new EditorNavigator<>(beanType, editorNavigationProcessor::processNavigation);

        E selected = listDataComponent.getSingleSelectedItem();
        if (selected != null) {
            navigation.editEntity(selected);
        }

        return navigation;
    }

    public <E> EditorNavigator<E> editor(ListDataComponent<E> listDataComponent, Screen parent) {
        return editor(listDataComponent)
                .withBackNavigationTarget(parent.getClass());
    }

    public <E> BrowserNavigator<E> browser(Class<E> entityClass) {
        checkNotNullArgument(entityClass);

        return new BrowserNavigator<>(entityClass, browserNavigationProcessor::processNavigation);
    }

    public ScreenNavigator screen(Class<? extends Screen> screenClass) {
        return new ScreenNavigator(screenNavigationProcessor::processNavigation)
                .withScreenClass(screenClass);
    }

    public ScreenNavigator screen(String screenId) {
        return new ScreenNavigator(screenNavigationProcessor::processNavigation)
                .withScreenId(screenId);
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

    // TODO: gg, public <E> NavigationBuilder<E> editor(EntityPicker<E> entityPicker) {}
}
