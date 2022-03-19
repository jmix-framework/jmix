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
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.charts.ChartLocaleHelper;
import io.jmix.charts.component.CustomChart;
import io.jmix.charts.model.axis.CategoryAxis;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.chart.impl.AbstractSerialChart;
import io.jmix.charts.model.chart.impl.RectangularChartModelImpl;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.charts.model.date.DayOfWeek;
import io.jmix.charts.model.settings.*;
import io.jmix.charts.serialization.JmixChartSerializer;
import io.jmix.charts.widget.amcharts.JmixAmchartsIntegration;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;
import io.jmix.charts.widget.amcharts.serialization.ChartJsonSerializationContext;
import io.jmix.charts.widget.amcharts.serialization.ChartSerializer;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;


public class CustomChartImpl extends AbstractComponent<JmixAmchartsScene> implements CustomChart, InitializingBean {
    protected Messages messages;
    protected MessageTools messageTools;
    protected ChartLocaleHelper chartLocaleHelper;
    protected FormatStringsRegistry formatStringsRegistry;
    protected CurrentAuthentication currentAuthentication;

    public CustomChartImpl() {
        component = createComponent();
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setCurrentAuthentication(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Autowired
    public void setChartLocaleHelper(ChartLocaleHelper chartLocaleHelper) {
        this.chartLocaleHelper = chartLocaleHelper;
    }

    @Autowired
    public void setFormatStringsRegistry(FormatStringsRegistry formatStringsRegistry) {
        this.formatStringsRegistry = formatStringsRegistry;
    }

    protected JmixAmchartsScene createComponent() {
        return new JmixAmchartsScene();
    }

    protected ChartSerializer createChartSerializer() {
        return applicationContext.getBean(JmixChartSerializer.class);
    }

    @Override
    public AbstractChart getConfiguration() {
        return component.getChart();
    }

    @Override
    public void setConfiguration(AbstractChart configuration) {
        checkNotNullArgument(configuration);

        setupDefaults(configuration);
        component.drawChart(configuration);
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


    protected void setupDefaults(AbstractChart chart) {
        setupChartLocale(chart);

        if (chart instanceof RectangularChartModelImpl) {
            setupRectangularChartDefaults((RectangularChartModelImpl) chart);
        }
        if (chart instanceof AbstractSerialChart) {
            setupSerialChartDefaults((AbstractSerialChart) chart);
        }
    }

    protected void setupChartLocale(AbstractChart chart) {

        // language
        if (StringUtils.isEmpty(chart.getLanguage())) {
            chart.setLanguage(LocaleResolver.localeToString(currentAuthentication.getLocale()));
        }

        // export
        if (chart.getExport() != null && chart.getExport().getDateFormat() == null) {
            chart.getExport().setDateFormat(messages.getMessage("amcharts.export.dateFormat"));
        }

        // number formatting
        FormatStrings formatStrings = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale());
        if (formatStrings != null) {
            DecimalFormatSymbols formatSymbols = formatStrings.getFormatSymbols();

            if (chart.getPrecision() == null) {
                chart.setPrecision(-1);
            }

            if (chart.getPercentPrecision() == null) {
                chart.setPercentPrecision(2);
            }

            if (chart.getDecimalSeparator() == null) {
                chart.setDecimalSeparator(Character.toString(formatSymbols.getDecimalSeparator()));
            }

            if (chart.getThousandsSeparator() == null) {
                chart.setThousandsSeparator(Character.toString(formatSymbols.getGroupingSeparator()));
            }
        }

        // number prefixes
        if (BooleanUtils.isTrue(chart.getUsePrefixes())) {
            if (chart.getPrefixesOfBigNumbers() == null) {
                List<BigNumberPrefix> prefixes = new ArrayList<>();
                for (BigNumberPower power : BigNumberPower.values()) {
                    prefixes.add(new BigNumberPrefix(power,
                            messages.getMessage("amcharts.bigNumberPower." + power.name())));
                }
                chart.setPrefixesOfBigNumbers(prefixes);
            }
            if (chart.getPrefixesOfSmallNumbers() == null) {
                List<SmallNumberPrefix> prefixes = new ArrayList<>();
                for (SmallNumberPower power : SmallNumberPower.values()) {
                    prefixes.add(new SmallNumberPrefix(power,
                            messages.getMessage("amcharts.smallNumberPower." + power.name())));
                }
                chart.setPrefixesOfSmallNumbers(prefixes);
            }
        }
    }

    protected void setupRectangularChartDefaults(RectangularChartModelImpl chart) {
        if (chart.getZoomOutText() == null) {
            chart.setZoomOutText(messages.getMessage("amcharts.zoomOutText"));
        }

        Cursor cursor = chart.getChartCursor();
        if (cursor != null) {
            if (StringUtils.isEmpty(cursor.getCategoryBalloonDateFormat())) {
                String format = messages.getMessage("amcharts.rectangularChart.categoryBalloonDateFormat");
                cursor.setCategoryBalloonDateFormat(format);
            }
        }
    }

    protected void setupSerialChartDefaults(AbstractSerialChart chart) {
        CategoryAxis categoryAxis = chart.getCategoryAxis();
        if (categoryAxis == null) {
            categoryAxis = new CategoryAxis();
            chart.setCategoryAxis(categoryAxis);
        }

        String firstDayOfWeek = messages.getMessage("amcharts.firstDayOfWeek");
        if (categoryAxis.getFirstDayOfWeek() == null) {
            categoryAxis.setFirstDayOfWeek(DayOfWeek.valueOf(firstDayOfWeek));
        }

        if (StringUtils.isEmpty(chart.getDataDateFormat())) {
            chart.setDataDateFormat(ChartJsonSerializationContext.DEFAULT_JS_DATE_FORMAT);
        }

        if (StringUtils.isEmpty(chart.getBalloonDateFormat())) {
            String format = messages.getMessage("amcharts.serialChart.balloonDateFormat");
            chart.setBalloonDateFormat(format);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        component.setChartSerializer(createChartSerializer());

        initLocale();
    }
}