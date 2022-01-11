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

package chart_to_json_converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.jmix.reports.entity.charts.ChartSeries
import io.jmix.reports.entity.charts.ChartToJsonConverter
import io.jmix.reports.entity.charts.SerialChartDescription
import io.jmix.reports.entity.charts.SeriesType
import org.apache.groovy.util.Maps
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime

class ChartToJsonConverterTest extends Specification {

    def "testLocalDateCategoryValue"() throws IOException, URISyntaxException {
        def converter = new ChartToJsonConverter()

        when: "Chart is configured with LocalDate category field type"
        List<Map<String, Object>> data = new ArrayList<>()
        data.add(Maps.of("value", 1, "date", LocalDate.of(2022, 1, 11)))
        data.add(Maps.of("value", 2, "date", LocalDate.of(2022, 1, 12)))
        data.add(Maps.of("value", 3, "date", LocalDate.of(2022, 1, 13)))
        def json = converter.convertSerialChart(generateSerialChartDescription(), data)

        then: "LocalData value should be converted to a string"
        def expectedJson = readFile("localdate-serialchart.json")

        prettyJson(expectedJson) == prettyJson(json)
    }

    def "testLocalDateTimeCategoryValue"() throws IOException, URISyntaxException {
        def converter = new ChartToJsonConverter()

        when: "Chart is configured with LocalDateTime category field type"
        List<Map<String, Object>> data = new ArrayList<>()
        data.add(Maps.of("value", 1, "date", LocalDateTime.of(2022, 1, 11, 1, 12, 33)))
        data.add(Maps.of("value", 2, "date", LocalDateTime.of(2022, 1, 12, 2, 13, 34)))
        data.add(Maps.of("value", 3, "date", LocalDateTime.of(2022, 1, 13, 3, 14, 35)))
        def json = converter.convertSerialChart(generateSerialChartDescription(), data)

        then: "LocalDataTime value should be converted to a string"
        def expectedJson = readFile("localdatetime-serialchart.json")
        prettyJson(expectedJson) == prettyJson(json)
    }

    protected SerialChartDescription generateSerialChartDescription() {
        SerialChartDescription serialChartDescription = new SerialChartDescription()
        serialChartDescription.setId(UUID.randomUUID())
        serialChartDescription.setBandName("testBand")
        serialChartDescription.setCategoryField("date")
        serialChartDescription.setSeries(Collections.singletonList(generateChartSeries()))
        return serialChartDescription
    }

    protected ChartSeries generateChartSeries() {
        ChartSeries chartSeries = new ChartSeries()
        chartSeries.setId(UUID.randomUUID())
        chartSeries.setType(SeriesType.COLUMN)
        chartSeries.setValueField("value")
        return chartSeries
    }

    protected String readFile(String fileName) throws IOException, URISyntaxException {
        URL resource = ChartToJsonConverterTest.class
                .getResource("/chart_to_json_converter/" + fileName);
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    protected String prettyJson(String json) {
        JsonElement parsedJson = JsonParser.parseString(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(parsedJson);
    }
}
