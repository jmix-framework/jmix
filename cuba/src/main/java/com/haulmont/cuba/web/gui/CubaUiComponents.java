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

package com.haulmont.cuba.web.gui;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Accordion;
import com.haulmont.cuba.gui.components.BrowserFrame;
import com.haulmont.cuba.gui.components.ButtonsPanel;
import com.haulmont.cuba.gui.components.Calendar;
import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.CheckBoxGroup;
import com.haulmont.cuba.gui.components.ColorPicker;
import com.haulmont.cuba.gui.components.CssLayout;
import com.haulmont.cuba.gui.components.CurrencyField;
import com.haulmont.cuba.gui.components.DataGrid;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.DatePicker;
import com.haulmont.cuba.gui.components.EntityLinkField;
import com.haulmont.cuba.gui.components.FileMultiUploadField;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.FlowBoxLayout;
import com.haulmont.cuba.gui.components.Form;
import com.haulmont.cuba.gui.components.Fragment;
import com.haulmont.cuba.gui.components.GridLayout;
import com.haulmont.cuba.gui.components.GroupBoxLayout;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.Image;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.MaskedField;
import com.haulmont.cuba.gui.components.PasswordField;
import com.haulmont.cuba.gui.components.PopupButton;
import com.haulmont.cuba.gui.components.PopupView;
import com.haulmont.cuba.gui.components.RadioButtonGroup;
import com.haulmont.cuba.gui.components.RelatedEntities;
import com.haulmont.cuba.gui.components.ResizableTextArea;
import com.haulmont.cuba.gui.components.RichTextArea;
import com.haulmont.cuba.gui.components.ScrollBoxLayout;
import com.haulmont.cuba.gui.components.Slider;
import com.haulmont.cuba.gui.components.SourceCodeEditor;
import com.haulmont.cuba.gui.components.SplitPanel;
import com.haulmont.cuba.gui.components.SuggestionField;
import com.haulmont.cuba.gui.components.TabSheet;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.TimeField;
import com.haulmont.cuba.gui.components.Tree;
import com.haulmont.cuba.gui.components.TwinColumn;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import com.haulmont.cuba.web.gui.components.*;
import com.haulmont.cuba.web.gui.components.mainwindow.WebAppWorkArea;
import com.haulmont.cuba.web.gui.components.mainwindow.WebFoldersPane;
import io.jmix.core.DevelopmentException;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.TreeTable;
import io.jmix.ui.component.impl.ButtonImpl;
import io.jmix.ui.component.impl.CapsLockIndicatorImpl;
import io.jmix.ui.component.impl.HtmlBoxLayoutImpl;
import io.jmix.ui.component.impl.JavaScriptComponentImpl;
import io.jmix.ui.component.impl.LinkButtonImpl;
import io.jmix.ui.component.impl.LinkImpl;
import io.jmix.ui.component.impl.ProgressBarImpl;
import io.jmix.ui.component.mainwindow.AppMenu;
import io.jmix.ui.component.mainwindow.LogoutButton;
import io.jmix.ui.component.mainwindow.NewWindowButton;
import io.jmix.ui.component.mainwindow.SideMenu;
import io.jmix.ui.component.mainwindow.TimeZoneIndicator;
import io.jmix.ui.component.mainwindow.UserActionsButton;
import io.jmix.ui.component.mainwindow.UserIndicator;
import io.jmix.ui.component.mainwindow.impl.AppMenuImpl;
import io.jmix.ui.component.mainwindow.impl.LogoutButtonImpl;
import io.jmix.ui.component.mainwindow.impl.NewWindowButtonImpl;
import io.jmix.ui.component.mainwindow.impl.SideMenuImpl;
import io.jmix.ui.component.mainwindow.impl.TimeZoneIndicatorImpl;
import io.jmix.ui.component.mainwindow.impl.UserActionsButtonImpl;
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

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Component("cuba_UiComponents")
public class CubaUiComponents implements UiComponents {

    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    protected Map<String, Class<? extends Component>> classes = new ConcurrentHashMap<>();
    protected Map<Class, String> names = new ConcurrentHashMap<>();

    {
        classes.put(RootWindow.NAME, WebRootWindow.class);
        classes.put(TabWindow.NAME, WebTabWindow.class);
        classes.put(DialogWindow.NAME, WebDialogWindow.class);
        classes.put(Fragment.NAME, WebFragment.class);

        classes.put(HBoxLayout.NAME, WebHBoxLayout.class);
        classes.put(VBoxLayout.NAME, WebVBoxLayout.class);
        classes.put(GridLayout.NAME, WebGridLayout.class);
        classes.put(ScrollBoxLayout.NAME, WebScrollBoxLayout.class);
        classes.put(HtmlBoxLayout.NAME, HtmlBoxLayoutImpl.class);
        classes.put(FlowBoxLayout.NAME, WebFlowBoxLayout.class);
        classes.put(CssLayout.NAME, WebCssLayout.class);

        classes.put(Button.NAME, ButtonImpl.class);
        classes.put(LinkButton.NAME, LinkButtonImpl.class);
        classes.put(Label.NAME, WebLabel.class);
        classes.put(Link.NAME, LinkImpl.class);
        classes.put(CheckBox.NAME, WebCheckBox.class);
        classes.put(GroupBoxLayout.NAME, WebGroupBox.class);
        classes.put(SourceCodeEditor.NAME, WebSourceCodeEditor.class);
        classes.put(TextField.NAME, WebTextField.class);
        classes.put(PasswordField.NAME, WebPasswordField.class);
        classes.put(Slider.NAME, WebSlider.class);

        classes.put(ResizableTextArea.NAME, WebResizableTextArea.class);
        classes.put(TextArea.NAME, WebTextArea.class);
        classes.put(RichTextArea.NAME, WebRichTextArea.class);
        classes.put(MaskedField.NAME, WebMaskedField.class);

        classes.put(Table.NAME, WebTable.class);
        classes.put(TreeTable.NAME, WebTreeTable.class);
        classes.put(GroupTable.NAME, WebGroupTable.class);
        classes.put(DataGrid.NAME, WebDataGrid.class);
        classes.put(TreeDataGrid.NAME, WebTreeDataGrid.class);
        classes.put(DateField.NAME, WebDateField.class);
        classes.put(TimeField.NAME, WebTimeField.class);
        classes.put(LookupField.NAME, WebLookupField.class);
        classes.put(SearchField.NAME, WebSearchField.class);
        classes.put(PickerField.NAME, WebPickerField.class);
        classes.put(SuggestionField.NAME, WebSuggestionField.class);
        classes.put(SuggestionPickerField.NAME, WebSuggestionPickerField.class);
        classes.put(ColorPicker.NAME, WebColorPicker.class);
        classes.put(LookupPickerField.NAME, WebLookupPickerField.class);
        classes.put(SearchPickerField.NAME, WebSearchPickerField.class);
        classes.put(OptionsGroup.NAME, WebOptionsGroup.class);
        classes.put(CheckBoxGroup.NAME, WebCheckBoxGroup.class);
        classes.put(RadioButtonGroup.NAME, WebRadioButtonGroup.class);
        classes.put(OptionsList.NAME, WebOptionsList.class);
        classes.put(FileUploadField.NAME, WebFileUploadField.class);
        classes.put(FileMultiUploadField.NAME, WebFileMultiUploadField.class);
        classes.put(CurrencyField.NAME, WebCurrencyField.class);
        classes.put(SplitPanel.NAME, WebSplitPanel.class);
        classes.put(Tree.NAME, WebTree.class);
        classes.put(TabSheet.NAME, WebTabSheet.class);
        classes.put(Accordion.NAME, WebAccordion.class);
        classes.put(Calendar.NAME, WebCalendar.class);
        classes.put(Embedded.NAME, WebEmbedded.class);
        classes.put(Image.NAME, WebImage.class);
        classes.put(BrowserFrame.NAME, WebBrowserFrame.class);
        classes.put(Filter.NAME, WebFilter.class);
        classes.put(ButtonsPanel.NAME, WebButtonsPanel.class);
        classes.put(PopupButton.NAME, WebPopupButton.class);
        classes.put(PopupView.NAME, WebPopupView.class);

        classes.put(FieldGroup.NAME, WebFieldGroup.class);
        classes.put(TokenList.NAME, WebTokenList.class);
        classes.put(TwinColumn.NAME, WebTwinColumn.class);
        classes.put(ProgressBar.NAME, ProgressBarImpl.class);
        classes.put(RowsCount.NAME, WebRowsCount.class);
        classes.put(RelatedEntities.NAME, WebRelatedEntities.class);
        classes.put(BulkEditor.NAME, WebBulkEditor.class);
        classes.put(DatePicker.NAME, WebDatePicker.class);
        classes.put(ListEditor.NAME, WebListEditor.class);
        classes.put(CapsLockIndicator.NAME, CapsLockIndicatorImpl.class);

        classes.put(EntityLinkField.NAME, WebEntityLinkField.class);
        classes.put(JavaScriptComponent.NAME, JavaScriptComponentImpl.class);

        classes.put(Form.NAME, WebForm.class);

        /* Main window components */

        classes.put(AppMenu.NAME, AppMenuImpl.class);
        classes.put(AppWorkArea.NAME, WebAppWorkArea.class);
        classes.put(UserActionsButton.NAME, UserActionsButtonImpl.class);
        classes.put(LogoutButton.NAME, LogoutButtonImpl.class);
        classes.put(NewWindowButton.NAME, NewWindowButtonImpl.class);
        classes.put(UserIndicator.NAME, UserActionsButtonImpl.class);
        classes.put(FoldersPane.NAME, WebFoldersPane.class);
        //classes.put(FtsField.NAME, WebFtsField.class); // todo fts
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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T create(TypeToken<T> type) {
        T t = (T) create((Class) type.getRawType());
        if (t instanceof HasDatatype) {
            Type[] actualTypeArguments = ((ParameterizedType) type.getType()).getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                Class actualTypeArgument = (Class) actualTypeArguments[0];

                ((HasDatatype) t).setDatatype(datatypeRegistry.find(actualTypeArgument));
            }
        }
        return t;
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
        context.setMessageGroup(getMessagePack(descriptorPath));

        CompositeDescriptorLoader compositeDescriptorLoader = applicationContext.getBean(CompositeDescriptorLoader.class);
        Element element = compositeDescriptorLoader.load(descriptorPath);

        CompositeComponentLayoutLoader layoutLoader =
                applicationContext.getBean(CompositeComponentLayoutLoader.class, context);

        return layoutLoader.createComponent(element);
    }

    protected String getMessagePack(String descriptorPath) {
        if (descriptorPath.contains("/")) {
            descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
        }

        String messagesPack = descriptorPath.replace("/", ".");
        int start = messagesPack.startsWith(".") ? 1 : 0;
        messagesPack = messagesPack.substring(start);
        return messagesPack;
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

    public void register(String name, Class<? extends Component> componentClass) {
        classes.put(name, componentClass);
    }
}
