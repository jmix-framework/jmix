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

package io.jmix.charts.widget.client.amcharts.rpc;

import com.vaadin.shared.communication.ServerRpc;

import javax.annotation.Nullable;
import java.util.Date;

public interface JmixAmchartsServerRpc extends ServerRpc {

    void onChartClick(int x, int y, int absoluteX, int absoluteY, double xAxis, double yAxis);

    void onChartRightClick(int x, int y, int absoluteX, int absoluteY, double xAxis, double yAxis);

    void onGraphClick(String graphId, int x, int y, int absoluteX, int absoluteY);

    void onGraphItemClick(String graphId, int itemIndex, String itemKey, int x, int y, int absoluteX, int absoluteY);

    void onGraphItemRightClick(String graphId, int itemIndex, String itemKey, int x, int y, int absoluteX, int absoluteY);

    void onZoom(int startIndex, int endIndex, Date startDate, Date endDate, String startValue, String endValue);

    void onSliceClick(int itemIndex, String dataItemKey, int x, int y, int absoluteX, int absoluteY);

    void onSliceRightClick(int itemIndex, String dataItemKey, int x, int y, int absoluteX, int absoluteY);

    void onSlicePullIn(String dataItemKey);

    void onSlicePullOut(String dataItemKey);

    void onLegendLabelClick(int legendItemIndex, @Nullable String dataItemKey);

    void onLegendMarkerClick(int legendItemIndex, @Nullable String dataItemKey);

    void onLegendItemHide(int legendItemIndex, @Nullable String dataItemKey);

    void onLegendItemShow(int legendItemIndex, @Nullable String dataItemKey);

    void onCursorZoom(String start, String end);

    void onCursorPeriodSelect(String start, String end);

    void onValueAxisZoom(String axisId, double startValue, double endValue);

    void onCategoryItemClick(String value, int x, int y, int offsetX, int offsetY, int xAxis, int yAxis);

    void onRollOutGraph(String graphId);

    void onRollOutGraphItem(String graphId, int itemIndex, String itemKey);

    void onRollOverGraph(String graphId);

    void onRollOverGraphItem(String graphId, int itemIndex, String itemKey);
}