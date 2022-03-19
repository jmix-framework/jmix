/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.sys;

import com.google.common.collect.ImmutableList;
import io.jmix.core.DevelopmentException;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.*;
import io.jmix.ui.component.impl.*;
import io.jmix.ui.component.mainwindow.*;
import io.jmix.ui.component.mainwindow.impl.*;
import io.jmix.ui.sys.event.UiEventListenerMethodAdapter;
import io.jmix.ui.xml.layout.loader.CompositeComponentLayoutLoader;
import io.jmix.ui.xml.layout.loader.CompositeComponentLoaderContext;
import io.jmix.ui.xml.layout.loader.CompositeDescriptorLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

@org.springframework.stereotype.Component("ui_UiComponents")
public class UiComponentsImpl implements UiComponents {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    protected Map<String, Class<? extends Component>> classes = new ConcurrentHashMap<>();
    protected Map<Class, String> names = new ConcurrentHashMap<>();

    {
        classes.put(RootWindow.NAME, RootWindowImpl.class);
        classes.put(TabWindow.NAME, TabWindowImpl.class);
        classes.put(DialogWindow.NAME, DialogWindowImpl.class);
        classes.put(Fragment.NAME, FragmentImpl.class);

        classes.put(HBoxLayout.NAME, HBoxLayoutImpl.class);
        classes.put(VBoxLayout.NAME, VBoxLayoutImpl.class);
        classes.put(GridLayout.NAME, GridLayoutImpl.class);
        classes.put(ScrollBoxLayout.NAME, ScrollBoxLayoutImpl.class);
        classes.put(HtmlBoxLayout.NAME, HtmlBoxLayoutImpl.class);
        classes.put(FlowBoxLayout.NAME, FlowBoxLayoutImpl.class);
        classes.put(CssLayout.NAME, CssLayoutImpl.class);
        classes.put(ResponsiveGridLayout.NAME, ResponsiveGridLayoutImpl.class);

        classes.put(Button.NAME, ButtonImpl.class);
        classes.put(LinkButton.NAME, LinkButtonImpl.class);
        classes.put(Label.NAME, LabelImpl.class);
        classes.put(Link.NAME, LinkImpl.class);
        classes.put(CheckBox.NAME, CheckBoxImpl.class);
        classes.put(GroupBoxLayout.NAME, GroupBoxImpl.class);
        classes.put(SourceCodeEditor.NAME, SourceCodeEditorImpl.class);
        classes.put(TextField.NAME, TextFieldImpl.class);
        classes.put(PasswordField.NAME, PasswordFieldImpl.class);
        classes.put(Slider.NAME, SliderImpl.class);

        classes.put(ResizableTextArea.NAME, ResizableTextAreaImpl.class);
        classes.put(TextArea.NAME, TextAreaImpl.class);
        classes.put(RichTextArea.NAME, RichTextAreaImpl.class);
        classes.put(MaskedField.NAME, MaskedFieldImpl.class);

        classes.put(Table.NAME, TableImpl.class);
        classes.put(TreeTable.NAME, TreeTableImpl.class);
        classes.put(GroupTable.NAME, GroupTableImpl.class);
        classes.put(DataGrid.NAME, DataGridImpl.class);
        classes.put(TreeDataGrid.NAME, TreeDataGridImpl.class);
        classes.put(DateField.NAME, DateFieldImpl.class);
        classes.put(TimeField.NAME, TimeFieldImpl.class);
        classes.put(ComboBox.NAME, ComboBoxImpl.class);
        classes.put(EntityPicker.NAME, EntityPickerImpl.class);
        classes.put(ValuePicker.NAME, ValuePickerImpl.class);
        classes.put(ValuesPicker.NAME, ValuesPickerImpl.class);
        classes.put(SuggestionField.NAME, SuggestionFieldImpl.class);
        classes.put(EntitySuggestionField.NAME, EntitySuggestionFieldImpl.class);
        classes.put(ColorPicker.NAME, ColorPickerImpl.class);
        classes.put(EntityComboBox.NAME, EntityComboBoxImpl.class);
        classes.put(CheckBoxGroup.NAME, CheckBoxGroupImpl.class);
        classes.put(RadioButtonGroup.NAME, RadioButtonGroupImpl.class);
        classes.put(MultiSelectList.NAME, MultiSelectListImpl.class);
        classes.put(SingleSelectList.NAME, SingleSelectListImpl.class);
        classes.put(FileUploadField.NAME, FileUploadFieldImpl.class);
        classes.put(FileStorageUploadField.NAME, FileStorageUploadFieldImpl.class);
        classes.put(FileMultiUploadField.NAME, FileMultiUploadFieldImpl.class);
        classes.put(CurrencyField.NAME, CurrencyFieldImpl.class);
        classes.put(SplitPanel.NAME, SplitPanelImpl.class);
        classes.put(Tree.NAME, TreeImpl.class);
        classes.put(TabSheet.NAME, TabSheetImpl.class);
        classes.put(Accordion.NAME, AccordionImpl.class);
        classes.put(Calendar.NAME, CalendarImpl.class);
        classes.put(Image.NAME, ImageImpl.class);
        classes.put(BrowserFrame.NAME, BrowserFrameImpl.class);
        classes.put(ButtonsPanel.NAME, ButtonsPanelImpl.class);
        classes.put(PopupButton.NAME, PopupButtonImpl.class);
        classes.put(PopupView.NAME, PopupViewImpl.class);

        classes.put(TagField.NAME, TagFieldImpl.class);
        classes.put(TagPicker.NAME, TagPickerImpl.class);
        classes.put(TwinColumn.NAME, TwinColumnImpl.class);
        classes.put(ProgressBar.NAME, ProgressBarImpl.class);
        classes.put(Pagination.NAME, PaginationImpl.class);
        classes.put(SimplePagination.NAME, SimplePaginationImpl.class);
        classes.put(RelatedEntities.NAME, RelatedEntitiesImpl.class);
        classes.put(DatePicker.NAME, DatePickerImpl.class);
        classes.put(CapsLockIndicator.NAME, CapsLockIndicatorImpl.class);

        classes.put(Form.NAME, FormImpl.class);

        classes.put(EntityLinkField.NAME, EntityLinkFieldImpl.class);
        classes.put(JavaScriptComponent.NAME, JavaScriptComponentImpl.class);

        classes.put(Filter.NAME, FilterImpl.class);
        classes.put(GroupFilter.NAME, GroupFilterImpl.class);
        classes.put(PropertyFilter.NAME, PropertyFilterImpl.class);
        classes.put(JpqlFilter.NAME, JpqlFilterImpl.class);

        /* Main window components */

        classes.put(AppMenu.NAME, AppMenuImpl.class);
        classes.put(AppWorkArea.NAME, AppWorkAreaImpl.class);
        classes.put(Drawer.NAME, DrawerImpl.class);
        classes.put(UserActionsButton.NAME, UserActionsButtonImpl.class);
        classes.put(LogoutButton.NAME, LogoutButtonImpl.class);
        classes.put(NewWindowButton.NAME, NewWindowButtonImpl.class);
        classes.put(UserIndicator.NAME, UserIndicatorImpl.class);
        classes.put(TimeZoneIndicator.NAME, TimeZoneIndicatorImpl.class);
        classes.put(SideMenu.NAME, SideMenuImpl.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T create(String name) {
        Class<? extends Component> componentClass = classes.get(name);
        if (componentClass == null) {
            throw new IllegalStateException(String.format("Can't find component class for '%s'", name));
        }

        Constructor<? extends Component> constructor;
        try {
            constructor = componentClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Unable to get constructor for '%s' component", name), e);
        }

        try {
            Component instance = constructor.newInstance();
            autowireContext(instance);
            initCompositeComponent(instance, componentClass);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format("Error creating the '%s' component instance", name), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T create(Class<T> type) {
        String name = names.get(type);
        if (name == null) {
            name = getComponentName(type);
            if (name == null)
                throw new DevelopmentException(String.format("Class '%s' doesn't have NAME field", type.getName()));
            else
                names.put(type, name);
        }
        return (T) create(name);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Component> T create(ParameterizedTypeReference<T> typeReference) {
        ParameterizedType type = (ParameterizedType) typeReference.getType();
        T t = create((Class<T>) type.getRawType());
        if (t instanceof HasDatatype) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                Class actualTypeArgument = (Class) actualTypeArguments[0];

                ((HasDatatype) t).setDatatype(datatypeRegistry.find(actualTypeArgument));
            }
        }
        return t;
    }

    @Override
    public boolean isComponentRegistered(String name) {
        return classes.containsKey(name);
    }

    @Override
    public boolean isComponentRegistered(Class<?> type) {
        String name = names.get(type);
        if (name != null) {
            return true;
        }

        name = getComponentName(type);

        return name != null && isComponentRegistered(name);
    }

    @Nullable
    protected String getComponentName(Class<?> type) {
        java.lang.reflect.Field nameField;
        try {
            nameField = type.getField("NAME");
            return (String) nameField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {
        }
        return null;
    }

    protected void autowireContext(Component instance) {
        AutowireCapableBeanFactory autowireBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireBeanFactory.autowireBean(instance);

        if (instance instanceof ApplicationContextAware) {
            ((ApplicationContextAware) instance).setApplicationContext(applicationContext);
        }

        if (instance instanceof InitializingBean) {
            try {
                ((InitializingBean) instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Unable to initialize UI Component - calling afterPropertiesSet for " +
                                instance.getClass(), e);
            }
        }
    }

    protected void initCompositeComponent(Component instance, Class<? extends Component> componentClass) {
        if (!(instance instanceof CompositeComponent)) {
            return;
        }

        CompositeComponent compositeComponent = (CompositeComponent) instance;

        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(componentClass);
        List<Method> eventListeners = Arrays.stream(methods)
                .filter(m -> findMergedAnnotation(m, EventListener.class) != null)
                .peek(m -> {
                    if (!m.isAccessible()) {
                        m.setAccessible(true);
                    }
                })
                .collect(ImmutableList.toImmutableList());
        if (!eventListeners.isEmpty()) {
            List<ApplicationListener> listeners = eventListeners.stream()
                    .map(m -> new UiEventListenerMethodAdapter(compositeComponent, componentClass, m, applicationContext))
                    .collect(Collectors.toList());

            CompositeComponentUtils.setUiEventListeners(compositeComponent, listeners);
        }

        CompositeDescriptor descriptor = componentClass.getAnnotation(CompositeDescriptor.class);
        if (descriptor != null) {
            String descriptorPath = descriptor.value();
            if (!descriptorPath.startsWith("/")) {
                String packageName = getPackage(componentClass);
                if (StringUtils.isNotEmpty(packageName)) {
                    String relativePath = packageName.replace('.', '/');
                    descriptorPath = "/" + relativePath + "/" + descriptorPath;
                }
            }
            Component root = processCompositeDescriptor(componentClass, descriptorPath);
            CompositeComponentUtils.setRoot(compositeComponent, root);
        }

        CompositeComponent.CreateEvent event = new CompositeComponent.CreateEvent(compositeComponent);
        CompositeComponentUtils.fireEvent(compositeComponent, CompositeComponent.CreateEvent.class, event);
    }

    protected String getPackage(Class<? extends Component> componentClass) {
        Package javaPackage = componentClass.getPackage();
        return javaPackage != null ? javaPackage.getName() : "";
    }

    protected Component processCompositeDescriptor(Class<? extends Component> componentClass, String descriptorPath) {
        CompositeComponentLoaderContext context = new CompositeComponentLoaderContext();
        context.setComponentClass(componentClass);
        context.setDescriptorPath(descriptorPath);
        context.setMessageGroup(getMessageGroup(descriptorPath));

        CompositeDescriptorLoader compositeDescriptorLoader = applicationContext.getBean(CompositeDescriptorLoader.class);
        Element element = compositeDescriptorLoader.load(descriptorPath);

        CompositeComponentLayoutLoader layoutLoader =
                applicationContext.getBean(CompositeComponentLayoutLoader.class, context);

        return layoutLoader.createComponent(element);
    }

    protected String getMessageGroup(String descriptorPath) {
        if (descriptorPath.contains("/")) {
            descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
        }

        String messageGroup = descriptorPath.replace("/", ".");
        int start = messageGroup.startsWith(".") ? 1 : 0;
        messageGroup = messageGroup.substring(start);
        return messageGroup;
    }

    public void register(String name, Class<? extends Component> componentClass) {
        classes.put(name, componentClass);
    }
}
