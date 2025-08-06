package io.jmix.flowui.kit.component.usermenu;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.action.Action;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// TODO: gg, split to separate interfaces
public interface HasMenuItems {

    TextUserMenuItem addItem(String id, String text);

    TextUserMenuItem addItem(String id, String text, int index);

    TextUserMenuItem addItem(String id, String text,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener);

    TextUserMenuItem addItem(String id, String text,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                             int index);

    TextUserMenuItem addItem(String id, String text, Component icon);

    TextUserMenuItem addItem(String id, String text, Component icon, int index);

    TextUserMenuItem addItem(String id,
                             String text, Component icon,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener);

    TextUserMenuItem addItem(String id,
                             String text, Component icon,
                             Consumer<UserMenuItem.HasClickListener.ClickEvent<TextUserMenuItem>> listener,
                             int index);

    ActionUserMenuItem addItem(String id, Action action);

    ActionUserMenuItem addItem(String id, Action action, int index);

    ComponentUserMenuItem addItem(String id, Component content);

    ComponentUserMenuItem addItem(String id, Component content, int index);

    ComponentUserMenuItem addItem(String id, Component content,
                                  Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener);

    ComponentUserMenuItem addItem(String id, Component content,
                                  Consumer<UserMenuItem.HasClickListener.ClickEvent<ComponentUserMenuItem>> listener,
                                  int index);

    void addSeparator();

    void addSeparatorAtIndex(int index);

    Optional<UserMenuItem> findItem(String itemId);

    UserMenuItem getItem(String itemId);

    List<UserMenuItem> getItems();

    void remove(String itemId);

    void remove(UserMenuItem menuItem);

    void removeAll();
}
