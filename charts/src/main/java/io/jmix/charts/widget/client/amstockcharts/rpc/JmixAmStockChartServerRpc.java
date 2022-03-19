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

package io.jmix.charts.widget.client.amstockcharts.rpc;

import com.vaadin.shared.communication.ServerRpc;

import java.util.Date;

public interface JmixAmStockChartServerRpc extends ServerRpc {

    void onChartClick(int x, int y, int absoluteX, int absoluteY);

    void onChartRightClick(int x, int y, int absoluteX, int absoluteY);

    void onStockEventClick(String graphId, Date date, String stockEventId);

    void onStockEventRollOut(String graphId, Date date, String stockEventId);

    void onStockEventRollOver(String graphId, Date date, String stockEventId);

    void onZoom(Date startDate, Date endDate, String period);

    void onPeriodSelectorChange(Date startDate, Date endDate, String predefinedPeriod,
                                Integer count, int x, int y, int absoluteX, int absoluteY);

    void onDataSetSelectorCompare(String dataSetId);

    void onDataSetSelectorSelect(String dataSetId);

    void onDataSetSelectorUnCompare(String dataSetId);

    void onStockGraphClick(String panelId, String graphId, int x, int y, int absoluteX, int absoluteY);

    void onStockGraphRollOut(String panelId, String graphId, int x, int y, int absoluteX, int absoluteY);

    void onStockGraphRollOver(String panelId, String graphId, int x, int y, int absoluteX, int absoluteY);

    void onStockGraphItemClick(String panelId, String graphId, int itemIndex, String dataSetId, String itemKey,
                               int x, int y, int absoluteX, int absoluteY);

    void onStockGraphItemRightClick(String panelId, String graphId, int itemIndex, String dataSetId, String itemKey,
                                    int x, int y, int absoluteX, int absoluteY);

    void onStockGraphItemRollOut(String panelId, String graphId, int itemIndex, String dataSetId, String itemKey,
                                 int x, int y, int absoluteX, int absoluteY);

    void onStockGraphItemRollOver(String panelId, String graphId, int itemIndex, String dataSetId, String itemKey,
                                  int x, int y, int absoluteX, int absoluteY);
}