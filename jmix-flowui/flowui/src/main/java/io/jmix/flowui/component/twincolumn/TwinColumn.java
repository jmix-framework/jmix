/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.twincolumn;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class TwinColumn<V> extends AbstractCompositeField<VerticalLayout, TwinColumn<V>, Collection<V>>
            implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasRequired, HasTheme,
                       SupportsItemsContainer<V>, SupportsValueSource<Collection<V>>, Focusable<ListBox<V>>,
                       ApplicationContextAware, InitializingBean {
    @Override
    public Registration addAttachListener(ComponentEventListener<AttachEvent> listener) {
        return super.addAttachListener(listener);
    }

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;

    protected VerticalLayout  contentWrapper;
    protected VerticalLayout controlsLayout;

    protected JmixButton moveLeftButton;
    protected JmixButton moveRightButton;
    protected JmixButton moveAllLeftButton;
    protected JmixButton moveAllRightButton;

    protected JmixListBox<V> optionsListBox;
    protected JmixListBox<V> selectionsListBox;

    List<String> allOptions = new LinkedList<>();
    List<String> entityOptions = new LinkedList<>();

    public TwinColumn() {
        super(null);
    }

    public void setAllOptions(List<String> allOptions) {
        this.allOptions = allOptions;
    }

    public void setEntityOptions(List<String> entityOptions) {
        this.entityOptions = entityOptions;
    }

    @Override
    public String getRequiredMessage() {
        return null;
    }

    @Override
    public void setRequiredMessage(String requiredMessage) {

    }

    @Override
    public void setItems(CollectionContainer<V> container) {

    }

    @Override
    protected void setPresentationValue(Collection<V> newPresentationValue) {

    }

    @Override
    public ValueSource<Collection<V>> getValueSource() {
        return null;
    }

    @Override
    public void setValueSource(ValueSource<Collection<V>> valueSource) {

    }

    public void mergeItems() {
        int i = 0;
        while (i < entityOptions.size()) {
            for (int j = 0; j < allOptions.size(); j++) {
                String s = entityOptions.get(i);
                if (s.equals(allOptions.get(j))) {
                    allOptions.remove(j);

                    removeLeft(j);

                    i++;
                    break;
                }
            }
        }
    }

    private void removeLeft(int leftIndex) {
        JmixGrid grid = null;
        grid.setDataProvider(new DataProvider() {
            @Override
            public boolean isInMemory() {
                return false;
            }

            @Override
            public int size(Query query) {
                return 0;
            }

            @Override
            public Stream fetch(Query query) {
                return null;
            }

            @Override
            public void refreshItem(Object o) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(DataProviderListener dataProviderListener) {
                return null;
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();

    }

    /*@Override
    protected VerticalLayout initContent() {
        VerticalLayout root = super.initContent();
        //root.addClassName(USER_INDICATOR_CLASS_NAME);
        return root;
    }*/

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout root = super.initContent();
        //root.addClassName(USER_INDICATOR_CLASS_NAME);
        return root;
    }

    protected void initLayout() {
        contentWrapper = createContentWrapper();
        initContentWrapper(contentWrapper);
        getContent().add(contentWrapper);

        controlsLayout = createControlsLayout();
        initControlsLayout(controlsLayout);
        contentWrapper.add(controlsLayout);

        createLists();
    }

    private void createLists() {
        optionsListBox = uiComponents.create(JmixListBox.class);
        contentWrapper.add(optionsListBox);

        selectionsListBox = uiComponents.create(JmixListBox.class);
        contentWrapper.add(selectionsListBox);
    }

    protected VerticalLayout createContentWrapper() {
        return uiComponents.create(VerticalLayout.class);
    }

    protected void initContentWrapper(VerticalLayout contentWrapper) {
        /*contentWrapper.setPadding(false);
        contentWrapper.setClassName(FILTER_CONTENT_WRAPPER_CLASS_NAME);*/
    }

    protected VerticalLayout createControlsLayout() {
        return uiComponents.create(VerticalLayout.class);
    }

    protected void initControlsLayout(VerticalLayout controlsLayout) {
        moveLeftButton = createControlButton(new Icon(VaadinIcon.ANGLE_LEFT), new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                moveLeft();
            }
        });
        controlsLayout.add(moveLeftButton);

        moveRightButton = createControlButton(new Icon(VaadinIcon.ANGLE_RIGHT), new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                moveRight();
            }
        });
        controlsLayout.add(moveRightButton);

        moveAllLeftButton = createControlButton(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT), new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                moveAllLeft();
            }
        });
        controlsLayout.add(moveAllLeftButton);

        moveAllRightButton = createControlButton(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT), new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                moveAllRight();
            }
        });
        controlsLayout.add(moveAllRightButton);
    }

    private void moveAllRight() {

    }

    private void moveAllLeft() {

    }

    private void moveLeft() {

    }

    private void moveRight() {

    }

    private JmixButton createControlButton(Icon icon, ComponentEventListener<ClickEvent<Button>> listener) {
        JmixButton button = new JmixButton();
        button.setIcon(icon);
        button.addClickListener(listener);
        return button;
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    protected void initComponent() {
        initLayout();
    }

    public void setAllBtnEnabled(Boolean setAllBtnEnabled) {

    }

    public void setLeftColumnCaption(String leftColumnCaption) {

    }

    public void setRightColumnCaption(String rightColumnCaption) {

    }

    public void setReorderable(Boolean reorderable) {

    }

    public void setRows(Integer rows) {

    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    public static class TwinColumnItem {
        public Object value;
    }

    public class TwinColumnValueChangeEvent<C extends Component> extends ComponentEvent<C> implements ValueChangeEvent<Collection<V>> {
        public TwinColumnValueChangeEvent(C source, boolean fromClient) {
            super(source, fromClient);
        }

        @Override
        public HasValue getHasValue() {
            return null;
        }

        @Override
        public boolean isFromClient() {
            return false;
        }

        @Override
        public Collection<V> getOldValue() {
            return null;
        }

        @Override
        public Collection<V> getValue() {
            return null;
        }
    }
}
