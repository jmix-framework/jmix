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

package io.jmix.charts.model.chart.impl;

import com.google.gson.annotations.Expose;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.DataProvider;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.charts.model.*;
import io.jmix.charts.model.balloon.Balloon;
import io.jmix.charts.model.chart.ChartModel;
import io.jmix.charts.model.chart.ChartType;
import io.jmix.charts.model.export.Export;
import io.jmix.charts.model.label.Label;
import io.jmix.charts.model.legend.Legend;
import io.jmix.charts.model.settings.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for charts. <br>
 * See documentation for properties of AmChart JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmChart">http://docs.amcharts.com/3/javascriptcharts/AmChart</a>
 */
@SuppressWarnings("unchecked")
public abstract class AbstractChart<T extends AbstractChartObject & ChartModel> extends ChartModelImpl
        implements ChartModel<T> {

    private static final long serialVersionUID = -7360797549413731632L;

    @Expose(serialize = false, deserialize = false)
    private List<DataProviderChangeListener> dataProviderChangeListeners;

    private Boolean accessible;

    private String accessibleTitle;

    private Boolean addClassNames;

    private List<Label> allLabels;

    private Boolean autoDisplay;

    private Boolean autoResize;

    private Boolean autoTransform;

    private Double backgroundAlpha;

    private Color backgroundColor;

    private Balloon balloon;

    private Double borderAlpha;

    private Color borderColor;

    private String classNamePrefix;

    private Color color;

    private CreditsPosition creditsPosition;

    private DataProvider dataProvider;

    private String decimalSeparator;

    /**
     * Using this property you can add any additional information to SVG,
     * like SVG filters or clip paths. The structure of this object should be
     * identical to XML structure of a object you are adding, only in JSON format.
     */
    private String defs;

    private Export export;

    private String fontFamily;

    private Integer fontSize;

    private Boolean handDrawn;

    private Integer handDrawScatter;

    private Integer handDrawThickness;

    private Integer hideBalloonTime;

    private String language;

    private Legend legend;

    private Boolean panEventsEnabled;

    private String path;

    private String pathToImages;

    private Integer percentPrecision;

    private Integer precision;

    private List<BigNumberPrefix> prefixesOfBigNumbers;

    private List<SmallNumberPrefix> prefixesOfSmallNumbers;

    private Integer processCount;

    private Integer processTimeout;

    private Boolean svgIcons;

    private Boolean tapToActivate;

    private ChartTheme theme;

    private String thousandsSeparator;

    private List<Title> titles;

    private Integer touchClickDuration;

    private ChartType type;

    private Boolean usePrefixes;

    private Responsive responsive;

    private String accessibleDescription;

    @Expose(serialize = false, deserialize = false)
    private List<String> additionalFields;

    protected AbstractChart(ChartType type) {
        this.type = type;
    }

    protected AbstractChart() {
    }

    @Override
    public Boolean getAddClassNames() {
        return addClassNames;
    }

    @Override
    public T setAddClassNames(Boolean addClassNames) {
        this.addClassNames = addClassNames;
        return (T) this;
    }

    @Override
    public List<Label> getAllLabels() {
        return allLabels;
    }

    @Override
    public T setAllLabels(List<Label> allLabels) {
        this.allLabels = allLabels;
        return (T) this;
    }

    @Override
    public T addLabels(Label... allLabels) {
        if (allLabels != null) {
            if (this.allLabels == null) {
                this.allLabels = new ArrayList<>();
            }
            this.allLabels.addAll(Arrays.asList(allLabels));
        }
        return (T) this;
    }

    @Override
    public Export getExport() {
        return export;
    }

    @Override
    public T setExport(Export amExport) {
        this.export = amExport;
        return (T) this;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public T setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return (T) this;
    }

    @Override
    public Balloon getBalloon() {
        return balloon;
    }

    @Override
    public T setBalloon(Balloon balloon) {
        this.balloon = balloon;
        return (T) this;
    }

    @Override
    public Legend getLegend() {
        return legend;
    }

    @Override
    public T setLegend(Legend legend) {
        this.legend = legend;
        return (T) this;
    }

    @Override
    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    @Override
    public T setDecimalSeparator(String decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
        return (T) this;
    }

    @Override
    public Integer getPercentPrecision() {
        return percentPrecision;
    }

    @Override
    public T setPercentPrecision(Integer percentPrecision) {
        this.percentPrecision = percentPrecision;
        return (T) this;
    }

    @Override
    public Integer getPrecision() {
        return precision;
    }

    @Override
    public T setPrecision(Integer precision) {
        this.precision = precision;
        return (T) this;
    }

    @Override
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    @Override
    public T setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        fireDataProviderChanged();
        return (T) this;
    }

    protected void fireDataProviderChanged() {
        if (CollectionUtils.isNotEmpty(dataProviderChangeListeners)) {
            for (DataProviderChangeListener listener : new ArrayList<>(dataProviderChangeListeners)) {
                listener.onChange();
            }
        }
    }

    @Override
    public T addData(DataItem... dataItems) {
        if (dataItems != null) {
            if (this.dataProvider == null) {
                this.dataProvider = new ListDataProvider();
            }
            this.dataProvider.addItems(Arrays.asList(dataItems));
        }
        return (T) this;
    }

    @Override
    public String getPathToImages() {
        return pathToImages;
    }

    @Override
    public T setPathToImages(String pathToImages) {
        this.pathToImages = pathToImages;
        return (T) this;
    }

    @Override
    public ChartTheme getTheme() {
        return theme;
    }

    @Override
    public T setTheme(ChartTheme theme) {
        this.theme = theme;
        return (T) this;
    }

    @Override
    public Double getBorderAlpha() {
        return borderAlpha;
    }

    @Override
    public T setBorderAlpha(Double borderAlpha) {
        this.borderAlpha = borderAlpha;
        return (T) this;
    }

    @Override
    public Color getBorderColor() {
        return borderColor;
    }

    @Override
    public T setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return (T) this;
    }

    @Override
    public String getClassNamePrefix() {
        return classNamePrefix;
    }

    @Override
    public T setClassNamePrefix(String classNamePrefix) {
        this.classNamePrefix = classNamePrefix;
        return (T) this;
    }

    @Override
    public CreditsPosition getCreditsPosition() {
        return creditsPosition;
    }

    @Override
    public T setCreditsPosition(CreditsPosition creditsPosition) {
        this.creditsPosition = creditsPosition;
        return (T) this;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public T setColor(Color color) {
        this.color = color;
        return (T) this;
    }

    @Override
    public String getFontFamily() {
        return fontFamily;
    }

    @Override
    public T setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return (T) this;
    }

    @Override
    public Integer getFontSize() {
        return fontSize;
    }

    @Override
    public T setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return (T) this;
    }

    @Override
    public Boolean getHandDrawn() {
        return handDrawn;
    }

    @Override
    public T setHandDrawn(Boolean handDrawn) {
        this.handDrawn = handDrawn;
        return (T) this;
    }

    @Override
    public Integer getHandDrawScatter() {
        return handDrawScatter;
    }

    @Override
    public T setHandDrawScatter(Integer handDrawScatter) {
        this.handDrawScatter = handDrawScatter;
        return (T) this;
    }

    @Override
    public Integer getHandDrawThickness() {
        return handDrawThickness;
    }

    @Override
    public T setHandDrawThickness(Integer handDrawThickness) {
        this.handDrawThickness = handDrawThickness;
        return (T) this;
    }

    @Override
    public Integer getHideBalloonTime() {
        return hideBalloonTime;
    }

    @Override
    public T setHideBalloonTime(Integer hideBalloonTime) {
        this.hideBalloonTime = hideBalloonTime;
        return (T) this;
    }

    @Override
    public Boolean getPanEventsEnabled() {
        return panEventsEnabled;
    }

    @Override
    public T setPanEventsEnabled(Boolean panEventsEnabled) {
        this.panEventsEnabled = panEventsEnabled;
        return (T) this;
    }

    @Override
    public List<BigNumberPrefix> getPrefixesOfBigNumbers() {
        return prefixesOfBigNumbers;
    }

    @Override
    public T setPrefixesOfBigNumbers(List<BigNumberPrefix> prefixesOfBigNumbers) {
        this.prefixesOfBigNumbers = prefixesOfBigNumbers;
        return (T) this;
    }

    @Override
    public T addPrefixesOfBigNumbers(BigNumberPrefix... prefixesOfBigNumbers) {
        if (prefixesOfBigNumbers != null) {
            if (this.prefixesOfBigNumbers == null) {
                this.prefixesOfBigNumbers = new ArrayList<>();
            }
            this.prefixesOfBigNumbers.addAll(Arrays.asList(prefixesOfBigNumbers));
        }
        return (T) this;
    }

    @Override
    public List<SmallNumberPrefix> getPrefixesOfSmallNumbers() {
        return prefixesOfSmallNumbers;
    }

    @Override
    public T setPrefixesOfSmallNumbers(List<SmallNumberPrefix> prefixesOfSmallNumbers) {
        this.prefixesOfSmallNumbers = prefixesOfSmallNumbers;
        return (T) this;
    }

    @Override
    public T addPrefixesOfSmallNumbers(SmallNumberPrefix... prefixesOfSmallNumbers) {
        if (prefixesOfSmallNumbers != null) {
            if (this.prefixesOfSmallNumbers == null) {
                this.prefixesOfSmallNumbers = new ArrayList<>();
            }
            this.prefixesOfSmallNumbers.addAll(Arrays.asList(prefixesOfSmallNumbers));
        }
        return (T) this;
    }

    @Override
    public String getThousandsSeparator() {
        return thousandsSeparator;
    }

    @Override
    public T setThousandsSeparator(String thousandsSeparator) {
        this.thousandsSeparator = thousandsSeparator;
        return (T) this;
    }

    @Override
    public List<Title> getTitles() {
        return titles;
    }

    @Override
    public T setTitles(List<Title> titles) {
        this.titles = titles;
        return (T) this;
    }

    @Override
    public T addTitles(Title... titles) {
        if (titles != null) {
            if (this.titles == null) {
                this.titles = new ArrayList<>();
            }
            this.titles.addAll(Arrays.asList(titles));
        }
        return (T) this;
    }

    @Override
    public Boolean getUsePrefixes() {
        return usePrefixes;
    }

    @Override
    public T setUsePrefixes(Boolean usePrefixes) {
        this.usePrefixes = usePrefixes;
        return (T) this;
    }

    @Override
    public List<String> getWiredFields() {
        List<String> fields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(getAdditionalFields())) {
            fields.addAll(getAdditionalFields());
        }
        return fields;
    }

    @Override
    public List<String> getAdditionalFields() {
        return additionalFields;
    }

    @Override
    public T setAdditionalFields(List<String> additionalFields) {
        this.additionalFields = additionalFields;
        return (T) this;
    }

    @Override
    public T addAdditionalFields(String... fields) {
        if (additionalFields == null) {
            additionalFields = new ArrayList<>();
        }
        additionalFields.addAll(Arrays.asList(fields));
        return (T) this;
    }

    public ChartType getType() {
        return type;
    }

    @Override
    public Boolean getAutoDisplay() {
        return autoDisplay;
    }

    @Override
    public T setAutoDisplay(Boolean autoDisplay) {
        this.autoDisplay = autoDisplay;
        return (T) this;
    }

    @Override
    public Boolean getAutoResize() {
        return autoResize;
    }

    @Override
    public T setAutoResize(Boolean autoResize) {
        this.autoResize = autoResize;
        return (T) this;
    }

    @Override
    public Double getBackgroundAlpha() {
        return backgroundAlpha;
    }

    @Override
    public T setBackgroundAlpha(Double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return (T) this;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public T setLanguage(String language) {
        this.language = language;
        return (T) this;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public T setPath(String path) {
        this.path = path;
        return (T) this;
    }

    @Override
    public Boolean getSvgIcons() {
        return svgIcons;
    }

    @Override
    public T setSvgIcons(Boolean svgIcons) {
        this.svgIcons = svgIcons;
        return (T) this;
    }

    @Override
    public Boolean getTapToActivate() {
        return tapToActivate;
    }

    @Override
    public T setTapToActivate(Boolean tapToActivate) {
        this.tapToActivate = tapToActivate;
        return (T) this;
    }

    @Override
    public String getDefs() {
        return defs;
    }

    /**
     * /**
     * Using this property you can add any additional information to SVG,
     * like SVG filters or clip paths. The structure of this object should be
     * identical to XML structure of a object you are adding, only in JSON format.
     * @param defs json string
     * @return this object
     */
    @Override
    public T setDefs(String defs) {
        this.defs = defs;
        return (T) this;
    }

    @Override
    public Boolean getAccessible() {
        return accessible;
    }

    @Override
    public T setAccessible(Boolean accessible) {
        this.accessible = accessible;
        return (T) this;
    }

    @Override
    public String getAccessibleTitle() {
        return accessibleTitle;
    }

    @Override
    public T setAccessibleTitle(String accessibleTitle) {
        this.accessibleTitle = accessibleTitle;
        return (T) this;
    }

    @Override
    public T setResponsive(Responsive responsive) {
        this.responsive = responsive;
        return (T) this;
    }

    @Override
    public Responsive getResponsive() {
        return responsive;
    }

    @Override
    public Integer getProcessCount() {
        return processCount;
    }

    @Override
    public T setProcessCount(Integer processCount) {
        this.processCount = processCount;
        return (T) this;
    }

    @Override
    public Integer getProcessTimeout() {
        return processTimeout;
    }

    @Override
    public T setProcessTimeout(Integer processTimeout) {
        this.processTimeout = processTimeout;
        return (T) this;
    }

    @Override
    public Integer getTouchClickDuration() {
        return touchClickDuration;
    }

    @Override
    public T setTouchClickDuration(Integer touchClickDuration) {
        this.touchClickDuration = touchClickDuration;
        return (T) this;
    }

    public void addDataProviderChangeListener(DataProviderChangeListener listener) {
        if (dataProviderChangeListeners == null) {
            dataProviderChangeListeners = new ArrayList<>();
        }
        dataProviderChangeListeners.add(listener);
    }

    public void removeDataProviderSetListener(DataProviderChangeListener listener) {
        if (dataProviderChangeListeners != null) {
            dataProviderChangeListeners.remove(listener);
        }
    }

    @Override
    public Boolean getAutoTransform() {
        return autoTransform;
    }

    @Override
    public T setAutoTransform(Boolean autoTransform) {
        this.autoTransform = autoTransform;
        return (T) this;
    }

    public String getAccessibleDescription() {
        return accessibleDescription;
    }

    public T setAccessibleDescription(String accessibleDescription) {
        this.accessibleDescription = accessibleDescription;
        return (T) this;
    }

    public interface DataProviderChangeListener {
        void onChange();
    }
}