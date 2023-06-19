/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.reporting.extraction.controller;

import io.jmix.reports.yarg.loaders.factory.impl.DefaultLoaderFactory;
import io.jmix.reports.yarg.reporting.extraction.preprocessor.SqlCrosstabPreprocessor;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.reporting.extraction.ExtractionContext;
import io.jmix.reports.yarg.reporting.extraction.ExtractionControllerFactory;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extraction controller for {@link BandOrientation#CROSS} orientation.
 * <p>
 * Contains custom logic to get more simple way to create crosstab bands
 */
public class CrossTabExtractionController extends DefaultExtractionController {

    public static final String VERTICAL_BAND = "master_data";
    public static final String VERTICAL_KEY_TPL = "%s_" + VERTICAL_BAND;
    public static final String HORIZONTAL_BAND = "dynamic_header";
    public static final String HORIZONTAL_KEY_TPL = "%s_" + HORIZONTAL_BAND;
    public static final String HEADER_TPL = "%s_header";

    public CrossTabExtractionController(ExtractionControllerFactory controllerFactory, ReportLoaderFactory loaderFactory) {
        super(controllerFactory, loaderFactory);

        preprocessorFactory.register(DefaultLoaderFactory.SQL_DATA_LOADER, new SqlCrosstabPreprocessor());
    }

    @Override
    protected List<Map<String, Object>> getQueriesResult(ExtractionContext context) {
        Map<String, Object> crossTabParams = new HashMap<>(context.getParams());
        getQueries(context)
                .filter(e -> e.getName().endsWith(VERTICAL_BAND) || e.getName().endsWith(HORIZONTAL_BAND))
                .forEach(q -> crossTabParams.put(q.getName(), getQueryData(context, q)));

        return getQueriesResult(
                getQueries(context)
                        .filter(e -> !e.getName().endsWith(VERTICAL_BAND) && !e.getName().endsWith(HORIZONTAL_BAND))
                        .iterator(), context.extendParams(crossTabParams));
    }

    @Override
    protected List<BandData> traverseData(ExtractionContext context, List<Map<String, Object>> outputData) {
        String horizontalKey = String.format(HORIZONTAL_KEY_TPL, context.getBand().getName());
        String verticalKey = String.format(VERTICAL_KEY_TPL, context.getBand().getName());
        BandData header = new BandData(String.format(HEADER_TPL, context.getBand().getName()),
                context.getParentBandData(), BandOrientation.HORIZONTAL);
        header.setData(Collections.emptyMap());

        String horizontalDataLink = outputData.stream().findFirst().map(data ->
                data.keySet().stream().filter(key -> key.startsWith(horizontalKey)).findFirst().orElse(null))
                .orElse(null);
        String horizontalLink = horizontalDataLink == null ? null
                : horizontalDataLink.substring(horizontalKey.length() + 1);

        String verticalDataLink = outputData.stream().findFirst().map(data ->
                data.keySet().stream().filter(key -> key.startsWith(verticalKey)).findFirst().orElse(null))
                .orElse(null);
        String verticalLink = verticalDataLink == null ? null
                : verticalDataLink.substring(verticalKey.length() + 1);

        List<BandData> horizontalValues = Optional.ofNullable(context.getParams().get(horizontalKey))
                .map(e -> (List<Map<String, Object>>) e)
                .orElse(Collections.emptyList()).stream().map(hdata -> {
                    BandData horizontal = new BandData(horizontalKey, header, BandOrientation.VERTICAL);
                    horizontal.setData(hdata);
                    header.addChild(horizontal);
                    return horizontal;
                }).filter(Objects::nonNull).collect(Collectors.toList());

        Map<CrossKey, Map<String, Object>> outputDataMap = outputData.stream().collect(Collectors.toMap(
                data -> new CrossKey(data.get(horizontalDataLink), data.get(verticalDataLink)),
                Function.identity(),
                (e1, e2) -> e2
        ));

        List<BandData> verticalData = Optional.ofNullable(context.getParams().get(verticalKey))
                .map(e -> (List<Map<String, Object>>) e)
                .orElse(Collections.emptyList()).stream().map(vData -> {
                    BandData horizontal = new BandData(verticalKey, context.getParentBandData(), BandOrientation.HORIZONTAL);
                    horizontal.setData(vData);

                    for (BandData hData : horizontalValues) {
                        Object hkey = horizontalLink == null ? null : hData.getData().get(horizontalLink);
                        Object vkey = verticalLink == null ? null : vData.get(verticalLink);
                        Map<String, Object> crossTabData = outputDataMap.get(new CrossKey(hkey, vkey));
                        horizontal.addChild(wrapData(context.withParentData(horizontal), crossTabData));
                    }
                    return horizontal;
                }).collect(Collectors.toList());

        if (verticalData.isEmpty()) {
            verticalData.add(wrapData(context, null));
        }

        return Stream.concat(Stream.of(header), verticalData.stream()).collect(Collectors.toList());
    }

    @Override
    protected BandData wrapData(ExtractionContext context, Map<String, Object> data) {
        final BandData bandData = new BandData(context.getBand().getName(), context.getParentBandData(), BandOrientation.VERTICAL);
        bandData.setData(ObjectUtils.defaultIfNull(data, new HashMap<>()));
        return bandData;
    }

    static class CrossKey {
        Object hkey;
        Object vkey;

        public CrossKey(Object hkey, Object vkey) {
            this.hkey = hkey;
            this.vkey = vkey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CrossKey crossKey = (CrossKey) o;

            if (hkey != null ? !hkey.equals(crossKey.hkey) : crossKey.hkey != null) return false;
            return vkey != null ? vkey.equals(crossKey.vkey) : crossKey.vkey == null;
        }

        @Override
        public int hashCode() {
            int result = hkey != null ? hkey.hashCode() : 0;
            result = 31 * result + (vkey != null ? vkey.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "CrossKey{" +
                    "hkey=" + hkey +
                    ", vkey=" + vkey +
                    '}';
        }
    }
}
