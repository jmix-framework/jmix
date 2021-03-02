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


package serialization

import com.google.gson.JsonParser
import io.jmix.ui.data.impl.ListDataProvider
import io.jmix.ui.data.impl.MapDataItem
import io.jmix.charts.model.chart.impl.AbstractChart
import io.jmix.charts.model.chart.impl.SerialChartModelImpl
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime

class ChartDataItemSerializerTest extends Specification {

    def "Chart DataItem's LocalDate and LocalDateTime property types serialization"() {
        given: "Chart which DataProvider contains LocalDate and LocalDateTime property types"
        AbstractChart chart = new SerialChartModelImpl()
                .setAdditionalFields(["localDateTime", "localDate"])
                .setDataProvider(new ListDataProvider(
                        [new MapDataItem(localDate: LocalDate.now(), localDateTime: LocalDateTime.now())]))

        def serializer = new TestChartSerializer()

        when: "Chart is serialized and parsed to json"
        String json = serializer.serialize(chart)

        def chartElement = JsonParser.parseString(json)
        def dataProvider = chartElement.getAsJsonObject().getAsJsonArray("dataProvider")
        def dataItem = dataProvider.asList().get(0).getAsJsonObject()

        def localDate = dataItem.get("localDate")
        def localDateTime = dataItem.get("localDateTime")

        then: "LocalDate and LocalDateTime should be as primitives"
        localDate.isJsonPrimitive()
        localDateTime.isJsonPrimitive()
    }
}
