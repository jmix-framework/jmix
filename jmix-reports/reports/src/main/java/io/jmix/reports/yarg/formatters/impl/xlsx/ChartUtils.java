/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reports.yarg.formatters.impl.xlsx;

import org.docx4j.dml.chart.*;

import java.util.List;

public class ChartUtils {

    public static List<?> getAreas(Object series) {
        if (series instanceof CTAreaChart) {
            return ((CTAreaChart) series).getSer();
        } else if (series instanceof CTArea3DChart) {
            return ((CTArea3DChart) series).getSer();
        } else if (series instanceof CTLineChart) {
            return ((CTLineChart) series).getSer();
        } else if (series instanceof CTLine3DChart) {
            return ((CTLine3DChart) series).getSer();
        } else if (series instanceof CTStockChart) {
            return ((CTStockChart) series).getSer();
        } else if (series instanceof CTRadarChart) {
            return ((CTRadarChart) series).getSer();
        } else if (series instanceof CTScatterChart) {
            return ((CTScatterChart) series).getSer();
        } else if (series instanceof CTPieChart) {
            return ((CTPieChart) series).getSer();
        } else if (series instanceof CTPie3DChart) {
            return ((CTPie3DChart) series).getSer();
        } else if (series instanceof CTDoughnutChart) {
            return ((CTDoughnutChart) series).getSer();
        } else if (series instanceof CTBarChart) {
            return ((CTBarChart) series).getSer();
        } else if (series instanceof CTBar3DChart) {
            return ((CTBar3DChart) series).getSer();
        } else if (series instanceof CTOfPieChart) {
            return ((CTOfPieChart) series).getSer();
        } else if (series instanceof CTSurfaceChart) {
            return ((CTSurfaceChart) series).getSer();
        } else if (series instanceof CTSurface3DChart) {
            return ((CTSurface3DChart) series).getSer();
        } else if (series instanceof CTBubbleChart) {
            return ((CTBubbleChart) series).getSer();
        }
        return null;
    }

    public static CTAxDataSource getAreaCat(Object area) {
        if (area instanceof CTAreaSer) {
            return ((CTAreaSer) area).getCat();
        } else if (area instanceof CTLineSer) {
            return ((CTLineSer) area).getCat();
        } else if (area instanceof CTRadarSer) {
            return ((CTRadarSer) area).getCat();
        } else if (area instanceof CTPieSer) {
            return ((CTPieSer) area).getCat();
        } else if (area instanceof CTBarSer) {
            return ((CTBarSer) area).getCat();
        } else if (area instanceof CTSurfaceSer) {
            ((CTSurfaceSer) area).getCat();
        } else if (area instanceof CTBubbleSer) {
            return ((CTBubbleSer) area).getXVal();
        } else if (area instanceof CTScatterSer) {
            ((CTScatterSer) area).getXVal();
        }
        return null;
    }

    public static CTNumDataSource getAreaVal(Object area) {
        if (area instanceof CTAreaSer) {
            return ((CTAreaSer) area).getVal();
        } else if (area instanceof CTLineSer) {
            return ((CTLineSer) area).getVal();
        } else if (area instanceof CTRadarSer) {
            return ((CTRadarSer) area).getVal();
        } else if (area instanceof CTPieSer) {
            return ((CTPieSer) area).getVal();
        } else if (area instanceof CTBarSer) {
            return ((CTBarSer) area).getVal();
        } else if (area instanceof CTSurfaceSer) {
            ((CTSurfaceSer) area).getCat();
        } else if (area instanceof CTBubbleSer) {
            return ((CTBubbleSer) area).getYVal();
        } else if (area instanceof CTScatterSer) {
            ((CTScatterSer) area).getYVal();
        }
        return null;
    }
}
