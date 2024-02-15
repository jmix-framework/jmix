/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.component.loader;

import com.google.common.base.Strings;
import io.jmix.chartsflowui.kit.component.model.*;
import io.jmix.chartsflowui.kit.component.model.axis.HasAxisName;
import io.jmix.chartsflowui.kit.component.model.series.HasStack;
import io.jmix.chartsflowui.kit.component.model.shared.*;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component("chart_ChartLoaderSupport")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChartLoaderSupport {

    protected ComponentLoader.Context context;
    protected LoaderSupport loaderSupport;
    protected ApplicationContext applicationContext;

    public ChartLoaderSupport(ComponentLoader.Context context) {
        this.context = context;
    }

    @Autowired
    public void setLoaderSupport(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Optional<Color> loadColor(Element element, String attributeName) {
        return loaderSupport.loadString(element, attributeName)
                .map(Color::valueOf);
    }

    public void loadColor(Element element, String attributeName, Consumer<Color> setter) {
        loadColor(element, attributeName)
                .ifPresent(setter);
    }

    public void loadTextAttributes(HasText<?> component, Element element) {
        loadColor(element, "textBorderColor", component::setTextBorderColor);
        loaderSupport.loadDouble(element, "textBorderWidth", component::setTextBorderWidth);
        loaderSupport.loadString(element, "textBorderType", component::setTextBorderType);
        loaderSupport.loadInteger(element, "textBorderDashOffset", component::setTextBorderDashOffset);
        loaderSupport.loadInteger(element, "textShadowBlur", component::setTextShadowBlur);
        loadColor(element, "textShadowColor", component::setTextShadowColor);
        loaderSupport.loadInteger(element, "textShadowOffsetX", component::setTextShadowOffsetX);
        loaderSupport.loadInteger(element, "textShadowOffsetY", component::setTextShadowOffsetY);
    }

    public void loadPosition(HasPosition<?> component, Element element) {
        loaderSupport.loadString(element, "left", component::setLeft);
        loaderSupport.loadString(element, "top", component::setTop);
        loaderSupport.loadString(element, "right", component::setRight);
        loaderSupport.loadString(element, "bottom", component::setBottom);
    }

    public void loadShadow(HasShadow<?> component, Element element) {
        loaderSupport.loadInteger(element, "shadowBlur", component::setShadowBlur);
        loadColor(element, "shadowColor", component::setShadowColor);
        loaderSupport.loadInteger(element, "shadowOffsetX", component::setShadowOffsetX);
        loaderSupport.loadInteger(element, "shadowOffsetY", component::setShadowOffsetY);
    }

    public void loadLineStyle(HasLineStyle<?> component, Element element) {
        loaderSupport.loadEnum(element, HasLineStyle.Cap.class, "cap", component::setCap);
        loaderSupport.loadEnum(element, HasLineStyle.Join.class, "join", component::setJoin);
        loaderSupport.loadInteger(element, "miterLimit", component::setMiterLimit);
    }

    public void loadBorder(HasBorder<?> component, Element element) {
        loadColor(element, "borderColor", component::setBorderColor);
        loaderSupport.loadInteger(element, "borderWidth", component::setBorderWidth);
        loaderSupport.loadInteger(element, "borderRadius", component::setBorderRadius);
    }

    public void loadPadding(HasPadding<?> component, Element element) {
        loaderSupport.loadString(element, "padding")
                .map(this::splitToInteger)
                .ifPresent(paddingList -> {
                            switch (paddingList.size()) {
                                case 1 -> component.setPadding(paddingList.get(0));
                                case 2 -> component.setPadding(paddingList.get(0), paddingList.get(1));
                                case 4 -> component.setPadding(
                                        paddingList.get(0),
                                        paddingList.get(1),
                                        paddingList.get(2),
                                        paddingList.get(3)
                                );
                                default -> throw new GuiDevelopmentException(
                                        String.format("Invalid %s", Padding.class.getName()), context
                                );
                            }
                        }
                );
    }

    public void loadAlign(HasAlign<?> component, Element element) {
        loaderSupport.loadEnum(element, Align.class, "align", component::setAlign);
        loaderSupport.loadEnum(element, VerticalAlign.class, "verticalAlign", component::setVerticalAlign);
    }

    public void loadAxisNameAttributes(HasAxisName<?> component, Element element,
                                       BiConsumer<AbstractRichText<?>, Element> richLoader) {
        loaderSupport.loadResourceString(element, "name", context.getMessageGroup(), component::setName);
        loaderSupport.loadEnum(element, HasAxisName.NameLocation.class, "nameLocation", component::setNameLocation);
        loaderSupport.loadInteger(element, "nameGap", component::setNameGap);
        loaderSupport.loadInteger(element, "nameRotate", component::setNameRotate);
        loaderSupport.loadBoolean(element, "inverse", component::setInverse);

        Element nameTextStyleElement = element.element("nameTextStyle");
        if (nameTextStyleElement != null) {
            HasAxisName.NameTextStyle nameTextStyle = new HasAxisName.NameTextStyle();

            richLoader.accept(nameTextStyle, nameTextStyleElement);

            loadColor(nameTextStyleElement, "backgroundColor", nameTextStyle::setBackgroundColor);
            loaderSupport.loadString(nameTextStyleElement, "borderType", nameTextStyle::setBorderType);
            loaderSupport.loadInteger(nameTextStyleElement, "borderDashOffset", nameTextStyle::setBorderDashOffset);

            loadAlign(nameTextStyle, nameTextStyleElement);
            loadShadow(nameTextStyle, nameTextStyleElement);
            loadPadding(nameTextStyle, nameTextStyleElement);
            loadBorder(nameTextStyle, nameTextStyleElement);

            component.setNameTextStyle(nameTextStyle);
        }
    }

    public void loadSymbols(HasSymbols<?> component, Element element) {
        loaderSupport.loadString(element, "symbol", component::setSymbol);
        loaderSupport.loadEnum(element, HasSymbols.SymbolType.class, "symbolType", component::setSymbol);
        loaderSupport.loadInteger(element, "symbolSize", component::setSymbolSize);
        loaderSupport.loadInteger(element, "symbolRotate", component::setSymbolRotate);
        loaderSupport.loadBoolean(element, "symbolKeepAspect", component::setSymbolKeepAspect);
        loadJsFunction(element, "symbolSizeFunction", component::setSymbolSizeFunction);
        loadStringPair(element, "symbolOffset", component::setSymbolOffset,
                String.format(
                        String.format("%s supports only x and y symbols offset",
                                HasSymbols.class.getSimpleName())
                ));
    }

    public void loadJsFunction(Element element, String attributeName, Consumer<JsFunction> setter) {
        Element functionElement = element.element(attributeName);
        if (functionElement != null) {

            String jsFunctionText = functionElement.getText();
            if (Strings.isNullOrEmpty(jsFunctionText)) {
                throw new GuiDevelopmentException(
                        String.format("'%s' element cannot be empty", attributeName), context);
            }

            setter.accept(new JsFunction(jsFunctionText.trim()));
            return;
        }

        loaderSupport.loadString(element, attributeName)
                .map(JsFunction::new)
                .ifPresent(setter);
    }

    public void loadStack(HasStack<?> component, Element element) {
        loaderSupport.loadString(element, "stack", component::setStack);
        loaderSupport.loadEnum(element, HasStack.StackStrategy.class, "stackStrategy", component::setStackStrategy);
    }

    public List<String> split(String names) {
        return Arrays.stream(names.split("[\\s,]+"))
                .filter(split -> !Strings.isNullOrEmpty(split))
                .toList();
    }

    public List<Integer> splitToInteger(String names) {
        return split(names).stream()
                .map(Integer::valueOf)
                .toList();
    }

    public List<Double> splitToDouble(String names) {
        return split(names).stream()
                .map(Double::valueOf)
                .toList();
    }

    public List<Color> splitToColor(String names) {
        return split(names).stream()
                .map(Color::valueOf)
                .toList();
    }

    public void loadIntegerPair(Element element, String attributeName,
                                BiConsumer<Integer, Integer> setter, String exceptionMessage) {
        loadIntegerList(element, attributeName, valuesList -> {
            if (valuesList.length != 2) {
                throw new GuiDevelopmentException(exceptionMessage, context);
            }

            setter.accept(valuesList[0], valuesList[1]);
        });
    }

    public void loadDoublePair(Element element, String attributeName,
                               BiConsumer<Double, Double> setter, String exceptionMessage) {
        loadDoubleList(element, attributeName, valuesList -> {
            if (valuesList.length != 2) {
                throw new GuiDevelopmentException(exceptionMessage, context);
            }

            setter.accept(valuesList[0], valuesList[1]);
        });
    }

    public void loadStringPair(Element element, String attributeName,
                               BiConsumer<String, String> setter, String exceptionMessage) {
        loadStringList(element, attributeName, valuesList -> {
            if (valuesList.length != 2) {
                throw new GuiDevelopmentException(exceptionMessage, context);
            }

            setter.accept(valuesList[0], valuesList[1]);
        });
    }

    public void loadColorPair(Element element, String attributeName,
                              BiConsumer<Color, Color> setter, String exceptionMessage) {
        loadColorList(element, attributeName, valuesList -> {
            if (valuesList.length != 2) {
                throw new GuiDevelopmentException(exceptionMessage, context);
            }

            setter.accept(valuesList[0], valuesList[1]);
        });
    }

    public <E extends Enum<E>> void loadEnumPair(Element element, Class<E> enumClass, String attributeName,
                                                 BiConsumer<E, E> setter, String exceptionMessage) {
        loadEnumList(element, enumClass, attributeName, enumList -> {
            if (enumList.length != 2) {
                throw new GuiDevelopmentException(exceptionMessage, context);
            }

            setter.accept(enumList[0], enumList[1]);
        });
    }

    public void loadIntegerList(Element element, String attributeName, Consumer<Integer[]> setter) {
        loaderSupport.loadString(element, attributeName)
                .map(this::splitToInteger)
                .ifPresent(integerList -> setter.accept(integerList.toArray(new Integer[0])));
    }

    public void loadDoubleList(Element element, String attributeName, Consumer<Double[]> setter) {
        loaderSupport.loadString(element, attributeName)
                .map(this::splitToDouble)
                .ifPresent(doubleList -> setter.accept(doubleList.toArray(new Double[0])));
    }

    public void loadStringList(Element element, String attributeName, Consumer<String[]> setter) {
        loaderSupport.loadString(element, attributeName)
                .map(this::split)
                .ifPresent(integerList -> setter.accept(integerList.toArray(new String[0])));
    }

    public void loadColorList(Element element, String attributeName, Consumer<Color[]> setter) {
        loaderSupport.loadString(element, attributeName)
                .map(this::splitToColor)
                .ifPresent(integerList -> setter.accept(integerList.toArray(new Color[0])));
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> void loadEnumList(Element element, Class<E> enumClass,
                                                 String attributeName, Consumer<E[]> setter) {
        loaderSupport.loadString(element, attributeName)
                .map(this::split)
                .map(typesList -> typesList.stream()
                        .map(enumValue -> Enum.valueOf(enumClass, enumValue))
                        .toArray(value -> ((E[]) Array.newInstance(enumClass, value)))
                )
                .ifPresent(setter);
    }
}