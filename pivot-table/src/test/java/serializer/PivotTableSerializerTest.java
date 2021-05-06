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

package serializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.pivottable.model.Aggregation;
import io.jmix.pivottable.model.AggregationMode;
import io.jmix.pivottable.model.PivotTableModel;
import io.jmix.pivottable.serialization.PivotTableDataItemsSerializer;
import io.jmix.pivottable.widget.serialization.PivotJsonSerializationContext;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spockframework.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.PivotTableTestConfiguration;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PivotTableTestConfiguration.class})
public class PivotTableSerializerTest  {

    private List<DataItem> dataItems;

    @Autowired
    protected PivotTableDataItemsSerializer serializer;

    @Autowired
    protected SystemAuthenticator authenticator;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @BeforeEach
    public void setupTest() {
        authenticator.begin();

        formatStringsRegistry.setFormatStrings(Locale.ENGLISH, new FormatStrings(
                '.', ',',
                "#,##0", "#,##0.###", "#,##0.##",
                "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm Z", "HH:mm", "HH:mm Z",
                "True", "False"));

        dataItems = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.of(2010, Month.APRIL, 10, 14, 11, 59);
        Date date = Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        dataItems.add(new MapDataItem(
                Maps.of("id", 1L,
                        "name", "one",
                        "localDateTime", localDateTime,
                        "localDate", localDateTime.toLocalDate(),
                        "date", date)
        ));
    }

    @AfterEach
    public void cleanup() {
        authenticator.end();
    }

    @Test
    public void dateSerializationTest() {
        serializer.setCurrentAuthentication(currentAuthentication);
        PivotTableModel model = getPivotTableModel();
        PivotJsonSerializationContext ctx = new PivotJsonSerializationContext(model, new Gson());
        JsonArray serialize = serializer.serialize(dataItems, ctx);

        JsonObject jsonObject = serialize.getAsJsonArray().get(0).getAsJsonObject();
        JsonElement id = jsonObject.get("id");
        JsonElement name = jsonObject.get("name");
        JsonElement localDate = jsonObject.get("localDate");
        JsonElement localDateTime = jsonObject.get("localDateTime");
        JsonElement date = jsonObject.get("date");

        //All values should be serialized
        Assert.that(id.isJsonPrimitive(), "Long values should be serialized");
        Assert.that(name.isJsonPrimitive(), "String values should be serialized");
        Assert.that(localDate.isJsonPrimitive(), "LocalDate values should be serialized");
        Assert.that(localDateTime.isJsonPrimitive(), "LocalDateTime values should be serialized");
        Assert.that(date.isJsonPrimitive(), "Date values should be serialized");
    }

    private PivotTableModel getPivotTableModel() {
        Map<String, String> props = new HashMap<>();
        props.put("id", "id");
        props.put("name", "name");
        props.put("localDate", "localDate");
        props.put("localDateTime", "localDateTime");
        props.put("date", "date");

        Aggregation aggregation = new Aggregation();
        aggregation.setMode(AggregationMode.COUNT);

        PivotTableModel model = new PivotTableModel();
        model.setProperties(props);
        model.addCols("localDate");
        model.addRows("name");
        model.addAggregationProperties("id");
        model.setAggregation(aggregation);
        return model;
    }

}
