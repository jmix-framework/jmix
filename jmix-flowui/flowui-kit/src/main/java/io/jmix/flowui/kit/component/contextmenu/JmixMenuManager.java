package io.jmix.flowui.kit.component.contextmenu;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.contextmenu.MenuItemBase;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableRunnable;

import javax.annotation.Nullable;

public class JmixMenuManager<C extends Component, I extends MenuItemBase<?, I, S>, S extends SubMenuBase<?, I, S>>
        extends MenuManager<C, I, S> {

    private final C menu;
    protected final SerializableBiFunction<C, SerializableRunnable, I> itemGenerator;
    private final SerializableRunnable contentReset;

    public JmixMenuManager(C menu, SerializableRunnable contentReset,
                           SerializableBiFunction<C, SerializableRunnable, I> itemGenerator,
                           Class<I> itemType, @Nullable I parentMenuItem) {
        super(menu, contentReset, itemGenerator, itemType, parentMenuItem);

        this.menu = menu;
        this.contentReset = contentReset;
        this.itemGenerator = itemGenerator;
    }

    public I addItemAtIndex(int index, String text) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        menuItem.setText(text);
        addComponentAtIndex(index, menuItem);
        return menuItem;
    }

    public I addItemAtIndex(int index, Component component) {
        I menuItem = itemGenerator.apply(menu, contentReset);
        addComponentAtIndex(index, menuItem);
        menuItem.add(component);
        return menuItem;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public I addItemAtIndex(int index, String text,
                            @Nullable ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItemAtIndex(index, text);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class, (ComponentEventListener) clickListener);
        }
        return menuItem;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public I addItemAtIndex(int index, Component component,
                            @Nullable ComponentEventListener<ClickEvent<I>> clickListener) {
        I menuItem = addItemAtIndex(index, component);
        if (clickListener != null) {
            ComponentUtil.addListener(menuItem, ClickEvent.class, (ComponentEventListener) clickListener);
        }
        return menuItem;
    }
}
