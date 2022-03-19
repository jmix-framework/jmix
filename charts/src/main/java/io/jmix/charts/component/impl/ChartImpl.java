/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.component.impl;

import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import io.jmix.charts.ChartLocaleHelper;
import io.jmix.charts.component.Chart;
import io.jmix.charts.model.*;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.export.Export;
import io.jmix.charts.model.label.Label;
import io.jmix.charts.model.legend.Legend;
import io.jmix.charts.model.settings.*;
import io.jmix.charts.model.settings.Responsive;
import io.jmix.charts.serialization.JmixChartSerializer;
import io.jmix.charts.widget.amcharts.JmixAmchartsIntegration;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;
import io.jmix.charts.widget.amcharts.events.chart.listener.ChartClickListener;
import io.jmix.charts.widget.amcharts.events.chart.listener.ChartRightClickListener;
import io.jmix.charts.widget.amcharts.events.legend.LegendLabelClickEvent;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendItemHideListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendItemShowListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendLabelClickListener;
import io.jmix.charts.widget.amcharts.events.legend.listener.LegendMarkerClickListener;
import io.jmix.charts.widget.amcharts.serialization.ChartSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class ChartImpl<T extends Chart, M extends AbstractChart>
        extends AbstractComponent<JmixAmchartsScene> implements Chart<T>, InitializingBean {

    protected Messages messages;
    protected MessageTools messageTools;
    protected CurrentAuthentication currentAuthentication;
    protected ChartLocaleHelper chartLocaleHelper;
    protected FormatStringsRegistry formatStringsRegistry;

    protected ChartClickListener clickHandler;
    protected ChartRightClickListener rightClickHandler;
    protected LegendItemHideListener legendItemHideHandler;
    protected LegendItemShowListener legendItemShowHandler;
    protected LegendLabelClickListener legendLabelClickHandler;
    protected LegendMarkerClickListener legendMarkerClickHandler;

    public ChartImpl() {
        component = createComponent();
    }

    protected JmixAmchartsScene createComponent() {
        return new JmixAmchartsScene();
    }

    protected ChartSerializer createChartSerializer() {
        return applicationContext.getBean(JmixChartSerializer.class);
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setChartLocaleHelper(ChartLocaleHelper chartLocaleHelper) {
        this.chartLocaleHelper = chartLocaleHelper;
    }

    @Autowired
    public void setFormatStringsRegistry(FormatStringsRegistry formatStringsRegistry) {
        this.formatStringsRegistry = formatStringsRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        component.setChartSerializer(createChartSerializer());
        initLocale();

        M configuration = createChartConfiguration();
        setupDefaults(configuration);
        component.drawChart(configuration);
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    protected abstract M createChartConfiguration();

    protected M getModel() {
        return (M) component.getChart();
    }

    protected void initLocale() {
        JmixAmchartsIntegration amchartsIntegration = JmixAmchartsIntegration.get();
        if (amchartsIntegration.getSettings() == null
                || !Objects.equals(currentAuthentication.getLocale(), amchartsIntegration.getLocale())) {
            Settings settings = new Settings();
            Locale locale = currentAuthentication.getLocale();

            // chart
            String localeString = LocaleResolver.localeToString(locale);
            amchartsIntegration.setChartMessages(localeString, chartLocaleHelper.getChartLocaleMap(locale));

            // export
            amchartsIntegration.setExportMessages(localeString, chartLocaleHelper.getExportLocaleMap(locale));

            amchartsIntegration.setSettings(settings);
            amchartsIntegration.setLocale(currentAuthentication.getLocale());
        }
    }


    protected void setupDefaults(M chart) {
        setupChartLocale(chart);
    }

    protected void setupChartLocale(AbstractChart chart) {
        chart.setLanguage(LocaleResolver.localeToString(currentAuthentication.getLocale()));

        // number formatting
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
        if (formatStrings != null) {
            DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();

            chart.setPrecision(-1);
            chart.setPercentPrecision(2);
            chart.setDecimalSeparator(Character.toString(formatSymbols.getDecimalSeparator()));
            chart.setThousandsSeparator(Character.toString(formatSymbols.getGroupingSeparator()));
        }

        // number prefixes
        List<BigNumberPrefix> bigPrefixes = new ArrayList<>();
        for (BigNumberPower power : BigNumberPower.values()) {
            bigPrefixes.add(new BigNumberPrefix(power,
                    messages.getMessage("amcharts.bigNumberPower." + power.name())));
        }
        chart.setPrefixesOfBigNumbers(bigPrefixes);

        List<SmallNumberPrefix> smallPrefixes = new ArrayList<>();
        for (SmallNumberPower power : SmallNumberPower.values()) {
            smallPrefixes.add(new SmallNumberPrefix(power,
                    messages.getMessage("amcharts.smallNumberPower." + power.name())));
        }
        chart.setPrefixesOfSmallNumbers(smallPrefixes);
    }

    @Override
    public void repaint() {
        component.drawChart();
    }

    @Override
    public String getNativeJson() {
        return component.getJson();
    }

    @Override
    public void setNativeJson(String json) {
        component.setJson(json);
    }

    @Override
    public Boolean getAddClassNames() {
        return getModel().getAddClassNames();
    }

    @Override
    public T setAddClassNames(Boolean addClassNames) {
        getModel().setAddClassNames(addClassNames);
        return (T) this;
    }

    @Override
    public List<Label> getAllLabels() {
        return getModel().getAllLabels();
    }

    @Override
    public T setAllLabels(List allLabels) {
        getModel().setAllLabels(allLabels);
        return (T) this;
    }

    @Override
    public T addLabels(Label... allLabels) {
        getModel().addLabels(allLabels);
        return (T) this;
    }

    @Override
    public Export getExport() {
        return getModel().getExport();
    }

    @Override
    public T setExport(Export export) {
        if (export != null && export.getDateFormat() == null) {
            export.setDateFormat(messages.getMessage("amcharts.export.dateFormat"));
        }

        getModel().setExport(export);
        return (T) this;
    }

    @Override
    public Color getBackgroundColor() {
        return getModel().getBackgroundColor();
    }

    @Override
    public T setBackgroundColor(Color backgroundColor) {
        getModel().setBackgroundColor(backgroundColor);
        return (T) this;
    }

    @Override
    public Balloon getBalloon() {
        return getModel().getBalloon();
    }

    @Override
    public T setBalloon(Balloon balloon) {
        getModel().setBalloon(balloon);
        return (T) this;
    }

    @Override
    public Legend getLegend() {
        return getModel().getLegend();
    }

    @Override
    public T setLegend(Legend legend) {
        getModel().setLegend(legend);
        return (T) this;
    }

    @Override
    public String getDecimalSeparator() {
        return getModel().getDecimalSeparator();
    }

    @Override
    public T setDecimalSeparator(String decimalSeparator) {
        getModel().setDecimalSeparator(decimalSeparator);
        return (T) this;
    }

    @Override
    public Integer getPercentPrecision() {
        return getModel().getPercentPrecision();
    }

    @Override
    public T setPercentPrecision(Integer percentPrecision) {
        getModel().setPercentPrecision(percentPrecision);
        return (T) this;
    }

    @Override
    public Integer getPrecision() {
        return getModel().getPrecision();
    }

    @Override
    public T setPrecision(Integer precision) {
        getModel().setPrecision(precision);
        return (T) this;
    }

    @Override
    public DataProvider getDataProvider() {
        return getModel().getDataProvider();
    }

    @Override
    public T setDataProvider(DataProvider dataProvider) {
        getModel().setDataProvider(dataProvider);
        return (T) this;
    }

    @Override
    public T addData(DataItem... dataItems) {
        getModel().addData(dataItems);
        return (T) this;
    }

    @Override
    public String getPathToImages() {
        return getModel().getPathToImages();
    }

    @Override
    public T setPathToImages(String pathToImages) {
        getModel().setPathToImages(pathToImages);
        return (T) this;
    }

    @Override
    public ChartTheme getTheme() {
        return getModel().getTheme();
    }

    @Override
    public T setTheme(ChartTheme theme) {
        getModel().setTheme(theme);
        return (T) this;
    }

    @Override
    public Double getBorderAlpha() {
        return getModel().getBorderAlpha();
    }

    @Override
    public T setBorderAlpha(Double borderAlpha) {
        getModel().setBorderAlpha(borderAlpha);
        return (T) this;
    }

    @Override
    public Color getBorderColor() {
        return getModel().getBorderColor();
    }

    @Override
    public T setBorderColor(Color borderColor) {
        getModel().setBorderColor(borderColor);
        return (T) this;
    }

    @Override
    public String getClassNamePrefix() {
        return getModel().getClassNamePrefix();
    }

    @Override
    public T setClassNamePrefix(String classNamePrefix) {
        getModel().setClassNamePrefix(classNamePrefix);
        return (T) this;
    }

    @Override
    public CreditsPosition getCreditsPosition() {
        return getModel().getCreditsPosition();
    }

    @Override
    public T setCreditsPosition(CreditsPosition creditsPosition) {
        getModel().setCreditsPosition(creditsPosition);
        return (T) this;
    }

    @Override
    public Color getColor() {
        return getModel().getColor();
    }

    @Override
    public T setColor(Color color) {
        getModel().setColor(color);
        return (T) this;
    }

    @Override
    public String getFontFamily() {
        return getModel().getFontFamily();
    }

    @Override
    public T setFontFamily(String fontFamily) {
        getModel().setFontFamily(fontFamily);
        return (T) this;
    }

    @Override
    public Integer getFontSize() {
        return getModel().getFontSize();
    }

    @Override
    public T setFontSize(Integer fontSize) {
        getModel().setFontSize(fontSize);
        return (T) this;
    }

    @Override
    public Boolean getHandDrawn() {
        return getModel().getHandDrawn();
    }

    @Override
    public T setHandDrawn(Boolean handDrawn) {
        getModel().setHandDrawn(handDrawn);
        return (T) this;
    }

    @Override
    public Integer getHandDrawScatter() {
        return getModel().getHandDrawScatter();
    }

    @Override
    public T setHandDrawScatter(Integer handDrawScatter) {
        getModel().setHandDrawScatter(handDrawScatter);
        return (T) this;
    }

    @Override
    public Integer getHandDrawThickness() {
        return getModel().getHandDrawThickness();
    }

    @Override
    public T setHandDrawThickness(Integer handDrawThickness) {
        getModel().setHandDrawThickness(handDrawThickness);
        return (T) this;
    }

    @Override
    public Integer getHideBalloonTime() {
        return getModel().getHideBalloonTime();
    }

    @Override
    public T setHideBalloonTime(Integer hideBalloonTime) {
        getModel().setHideBalloonTime(hideBalloonTime);
        return (T) this;
    }

    @Override
    public Boolean getPanEventsEnabled() {
        return getModel().getPanEventsEnabled();
    }

    @Override
    public T setPanEventsEnabled(Boolean panEventsEnabled) {
        getModel().setPanEventsEnabled(panEventsEnabled);
        return (T) this;
    }

    @Override
    public List<BigNumberPrefix> getPrefixesOfBigNumbers() {
        return getModel().getPrefixesOfBigNumbers();
    }

    @Override
    public T setPrefixesOfBigNumbers(List prefixesOfBigNumbers) {
        getModel().setPrefixesOfBigNumbers(prefixesOfBigNumbers);
        return (T) this;
    }

    @Override
    public T addPrefixesOfBigNumbers(BigNumberPrefix... prefixesOfBigNumbers) {
        getModel().addPrefixesOfBigNumbers(prefixesOfBigNumbers);
        return (T) this;
    }

    @Override
    public List<SmallNumberPrefix> getPrefixesOfSmallNumbers() {
        return getModel().getPrefixesOfSmallNumbers();
    }

    @Override
    public T setPrefixesOfSmallNumbers(List prefixesOfSmallNumbers) {
        getModel().setPrefixesOfSmallNumbers(prefixesOfSmallNumbers);
        return (T) this;
    }

    @Override
    public T addPrefixesOfSmallNumbers(SmallNumberPrefix... prefixesOfSmallNumbers) {
        getModel().addPrefixesOfSmallNumbers(prefixesOfSmallNumbers);
        return (T) this;
    }

    @Override
    public String getThousandsSeparator() {
        return getModel().getThousandsSeparator();
    }

    @Override
    public T setThousandsSeparator(String thousandsSeparator) {
        getModel().setThousandsSeparator(thousandsSeparator);
        return (T) this;
    }

    @Override
    public List<Title> getTitles() {
        return getModel().getTitles();
    }

    @Override
    public T setTitles(List list) {
        getModel().setTitles(list);
        return (T) this;
    }

    @Override
    public T addTitles(Title... titles) {
        getModel().addTitles(titles);
        return (T) this;
    }

    @Override
    public Boolean getUsePrefixes() {
        return getModel().getUsePrefixes();
    }

    @Override
    public T setUsePrefixes(Boolean usePrefixes) {
        getModel().setUsePrefixes(usePrefixes);
        return (T) this;
    }

    @Override
    public List<String> getAdditionalFields() {
        return getModel().getAdditionalFields();
    }

    @Override
    public T setAdditionalFields(List additionalFields) {
        getModel().setAdditionalFields(additionalFields);
        return (T) this;
    }

    @Override
    public T addAdditionalFields(String... fields) {
        getModel().addAdditionalFields(fields);
        return (T) this;
    }

    @Override
    public Boolean getAutoDisplay() {
        return getModel().getAutoDisplay();
    }

    @Override
    public T setAutoDisplay(Boolean autoDisplay) {
        getModel().setAutoDisplay(autoDisplay);
        return (T) this;
    }

    @Override
    public Boolean getAutoResize() {
        return getModel().getAutoResize();
    }

    @Override
    public T setAutoResize(Boolean autoResize) {
        getModel().setAutoResize(autoResize);
        return (T) this;
    }

    @Override
    public Double getBackgroundAlpha() {
        return getModel().getBackgroundAlpha();
    }

    @Override
    public T setBackgroundAlpha(Double backgroundAlpha) {
        getModel().setBackgroundAlpha(backgroundAlpha);
        return (T) this;
    }

    @Override
    public String getLanguage() {
        return getModel().getLanguage();
    }

    @Override
    public T setLanguage(String language) {
        getModel().setLanguage(language);
        return (T) this;
    }

    @Override
    public String getPath() {
        return getModel().getPath();
    }

    @Override
    public T setPath(String path) {
        getModel().setPath(path);
        return (T) this;
    }

    @Override
    public Boolean getSvgIcons() {
        return getModel().getSvgIcons();
    }

    @Override
    public T setSvgIcons(Boolean svgIcons) {
        getModel().setSvgIcons(svgIcons);
        return (T) this;
    }

    @Override
    public Boolean getTapToActivate() {
        return getModel().getTapToActivate();
    }

    @Override
    public T setTapToActivate(Boolean tapToActivate) {
        getModel().setTapToActivate(tapToActivate);
        return (T) this;
    }

    @Override
    public String getDefs() {
        return getModel().getDefs();
    }

    @Override
    public T setDefs(String defs) {
        getModel().setDefs(defs);
        return (T) this;
    }

    @Override
    public Boolean getAccessible() {
        return getModel().getAccessible();
    }

    @Override
    public T setAccessible(Boolean accessible) {
        getModel().setAccessible(accessible);
        return (T) this;
    }

    @Override
    public String getAccessibleTitle() {
        return getModel().getAccessibleTitle();
    }

    @Override
    public T setAccessibleTitle(String accessibleTitle) {
        getModel().setAccessibleTitle(accessibleTitle);
        return (T) this;
    }

    @Override
    public T setResponsive(Responsive responsive) {
        if (responsive != null) {
            component.activateResponsivePlugin();
        }

        getModel().setResponsive(responsive);
        return (T) this;
    }

    @Override
    public Responsive getResponsive() {
        return getModel().getResponsive();
    }

    @Override
    public Integer getProcessCount() {
        return getModel().getProcessCount();
    }

    @Override
    public T setProcessCount(Integer processCount) {
        getModel().setProcessCount(processCount);
        return (T) this;
    }

    @Override
    public Integer getProcessTimeout() {
        return getModel().getProcessTimeout();
    }

    @Override
    public T setProcessTimeout(Integer processTimeout) {
        getModel().setProcessTimeout(processTimeout);
        return (T) this;
    }

    @Override
    public Integer getTouchClickDuration() {
        return getModel().getTouchClickDuration();
    }

    @Override
    public T setTouchClickDuration(Integer touchClickDuration) {
        getModel().setTouchClickDuration(touchClickDuration);
        return (T) this;
    }

    @Override
    public Boolean getAutoTransform() {
        return getModel().getAutoTransform();
    }

    @Override
    public T setAutoTransform(Boolean autoTransform) {
        getModel().setAutoTransform(autoTransform);
        return (T) this;
    }

    @Override
    public Subscription addClickListener(Consumer<ChartClickEvent> listener) {
        if (clickHandler == null) {
            clickHandler = this::onChartClick;
            component.addChartClickListener(clickHandler);
        }

        return getEventHub().subscribe(ChartClickEvent.class, listener);
    }

    protected void onChartClick(io.jmix.charts.widget.amcharts.events.chart.ChartClickEvent e) {
        publish(ChartClickEvent.class,
                new ChartClickEvent(this, e.getX(), e.getY(), e.getAbsoluteX(),
                        e.getAbsoluteY(), e.getXAxis(), e.getYAxis()));
    }


    @Override
    public Subscription addRightClickListener(Consumer<ChartRightClickEvent> listener) {
        if (rightClickHandler == null) {
            rightClickHandler = this::onChartRightClick;
            component.addChartRightClickListener(rightClickHandler);
        }

        return getEventHub().subscribe(ChartRightClickEvent.class, listener);
    }

    protected void onChartRightClick(io.jmix.charts.widget.amcharts.events.chart.ChartRightClickEvent e) {
        publish(ChartRightClickEvent.class,
                new ChartRightClickEvent(this, e.getX(), e.getY(), e.getAbsoluteX(),
                        e.getAbsoluteY(), e.getXAxis(), e.getYAxis()));
    }

    @Override
    public Subscription addLegendItemHideListener(Consumer<LegendItemHideEvent> listener) {
        if (legendItemHideHandler == null) {
            legendItemHideHandler = this::onLegendItemHide;
            component.addLegendItemHideListener(legendItemHideHandler);
        }

        return getEventHub().subscribe(LegendItemHideEvent.class, listener);
    }

    protected void onLegendItemHide(io.jmix.charts.widget.amcharts.events.legend.LegendItemHideEvent e) {
        publish(LegendItemHideEvent.class, new LegendItemHideEvent(this, e.getItemIndex(), e.getDataItem()));
    }


    @Override
    public Subscription addLegendItemShowListener(Consumer<LegendItemShowEvent> listener) {
        if (legendItemShowHandler == null) {
            legendItemShowHandler = this::onLegendItemShow;
            component.addLegendItemShowListener(legendItemShowHandler);
        }

        return getEventHub().subscribe(LegendItemShowEvent.class, listener);
    }

    protected void onLegendItemShow(io.jmix.charts.widget.amcharts.events.legend.LegendItemShowEvent e) {
        publish(LegendItemShowEvent.class, new LegendItemShowEvent(this, e.getItemIndex(), e.getDataItem()));
    }

    @Override
    public Subscription addLegendLabelClickListener(Consumer<LegendItemClickEvent> listener) {
        if (legendLabelClickHandler == null) {
            legendLabelClickHandler = this::onLegendLabelClick;
            component.addLegendLabelClickListener(legendLabelClickHandler);
        }

        return getEventHub().subscribe(LegendItemClickEvent.class, listener);
    }

    protected void onLegendLabelClick(LegendLabelClickEvent e) {
        publish(LegendItemClickEvent.class, new LegendItemClickEvent(this, e.getItemIndex(), e.getDataItem()));
    }

    @Override
    public Subscription addLegendMarkerClickListener(Consumer<LegendMarkerClickEvent> listener) {
        if (legendMarkerClickHandler == null) {
            legendMarkerClickHandler = this::onLegendMarkerClick;
            component.addLegendMarkerClickListener(legendMarkerClickHandler);
        }

        return getEventHub().subscribe(LegendMarkerClickEvent.class, listener);
    }

    protected void onLegendMarkerClick(io.jmix.charts.widget.amcharts.events.legend.LegendMarkerClickEvent e) {
        publish(LegendMarkerClickEvent.class, new LegendMarkerClickEvent(this, e.getItemIndex(), e.getDataItem()));
    }


    @Override
    public String getAccessibleDescription() {
        return getModel().getAccessibleDescription();
    }

    @Override
    public T setAccessibleDescription(String accessibleDescription) {
        getModel().setAccessibleDescription(accessibleDescription);
        return (T) this;
    }
}