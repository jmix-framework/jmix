/*
 * Copyright 2020 Haulmont.
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

package transformations;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.jmix.rest.impl.config.RestJsonTransformations;
import io.jmix.rest.transform.AbstractEntityJsonTransformer;
import io.jmix.rest.transform.JsonTransformationDirection;
import io.jmix.rest.impl.StandardEntityJsonTransformer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RestJsonTransformationTest {
    @Test
    public void renameEntityAndAttributes() throws Exception {

        Map<String, String> carAttributesToRename = new HashMap<>();
        carAttributesToRename.put("oldVin", "vin");
        StandardEntityJsonTransformer carTransformer = new StandardEntityJsonTransformer("ref$OldCar", "ref_Car", "1.0", JsonTransformationDirection.FROM_VERSION);
        carTransformer.setAttributesToRename(carAttributesToRename);

        Map<String, String> modelAttributesToRename = new HashMap<>();
        modelAttributesToRename.put("oldName", "name");
        StandardEntityJsonTransformer modelTransformer = new StandardEntityJsonTransformer("ref$OldModel", "ref$Model", "1.0", JsonTransformationDirection.FROM_VERSION);
        modelTransformer.setAttributesToRename(modelAttributesToRename);

        RestJsonTransformations restJsonTransformations = Mockito.mock(RestJsonTransformations.class);

        Mockito.when(restJsonTransformations.getTransformer("ref$OldCar", "1.0", JsonTransformationDirection.FROM_VERSION))
                .thenReturn(carTransformer);

        initStandardTransformer(carTransformer, restJsonTransformations);

        Mockito.when(restJsonTransformations.getTransformer("ref$OldModel", "1.0", JsonTransformationDirection.FROM_VERSION))
                .thenReturn(modelTransformer);

        initStandardTransformer(modelTransformer, restJsonTransformations);

        assertEquals("ref_Car", carTransformer.getTransformedEntityName());

        String srcJson = getFileContent("renameEntityAndAttributes.json");
        String resultJson = carTransformer.transformJson(srcJson);

        DocumentContext context = JsonPath.parse(resultJson);
        assertEquals("ref_Car", context.read("$._entityName"));
        assertEquals("VIN-01", context.read("$.vin"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.oldVin"));

        assertEquals("ref$Model", context.read("$.model._entityName"));
        assertEquals("Audi", context.read("$.model.name"));
        assertEquals("Audi Manufacturer", context.read("$.model.manufacturer"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.model.oldName"));
    }

    @Test
    public void renameEntityAndAttributeInArray() throws Exception {
        Map<String, String> attributesToRename = new HashMap<>();
        attributesToRename.put("lastName", "familyName");
        StandardEntityJsonTransformer standardEntityJsonTransformer = new StandardEntityJsonTransformer("app$OldEntity", "app$NewEntity", "1.0", JsonTransformationDirection.TO_VERSION);
        standardEntityJsonTransformer.setAttributesToRename(attributesToRename);

        assertEquals("app$NewEntity", standardEntityJsonTransformer.getTransformedEntityName());

        String srcJson = getFileContent("renameEntityAndAttributeInArray.json");
        String resultJson = standardEntityJsonTransformer.transformJson(srcJson);

        DocumentContext context = JsonPath.parse(resultJson);
        assertEquals("app$NewEntity", context.read("$.[0]._entityName"));
        assertEquals("Bob", context.read("$.[0].firstName"));
        assertEquals("Smith", context.read("$.[0].familyName"));

        assertEquals("app$NewEntity", context.read("$.[1]._entityName"));
        assertEquals("Jack", context.read("$.[1].firstName"));
        assertEquals("Daniels", context.read("$.[1].familyName"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.[0].lastName"));
        assertThrows(PathNotFoundException.class, () -> context.read("$.[1].lastName"));
    }

    @Test
    public void transformCompositionAttribute() throws Exception {
        Map<String, String> attributesToRename = new HashMap<>();
        attributesToRename.put("oldDescription", "description");
        StandardEntityJsonTransformer repairTransformer = new StandardEntityJsonTransformer("ref$OldRepair", "ref$Repair", "1.0", JsonTransformationDirection.FROM_VERSION);
        repairTransformer.setAttributesToRename(attributesToRename);

        RestJsonTransformations restJsonTransformations = Mockito.mock(RestJsonTransformations.class);

        Mockito.when(restJsonTransformations.getTransformer("ref$OldRepair", "1.0", JsonTransformationDirection.FROM_VERSION))
                .thenReturn(repairTransformer);

        initStandardTransformer(repairTransformer, restJsonTransformations);

        String srcJson = getFileContent("transformCompositionAttribute.json");
        String resultJson = repairTransformer.transformJson(srcJson);

        DocumentContext context = JsonPath.parse(resultJson);
        assertEquals("ref_Car", context.read("$._entityName"));
        assertEquals("VIN-01", context.read("$.vin"));

        assertEquals("ref$Repair", context.read("$.repairs.[0]_entityName"));
        assertEquals("Repair 1", context.read("$.repairs.[0].description"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.repairs.[0].oldDescription"));
    }

    private void initStandardTransformer(StandardEntityJsonTransformer transformer,
                                         RestJsonTransformations restJsonTransformations) throws NoSuchFieldException, IllegalAccessException {
        Field jsonTransformationsField = AbstractEntityJsonTransformer.class.getDeclaredField("jsonTransformations");
        jsonTransformationsField.setAccessible(true);
        jsonTransformationsField.set(transformer, restJsonTransformations);
    }

    @Test
    public void removeAttributes() throws Exception {
        Set<String> attributesToRemove = new HashSet<>();
        attributesToRemove.add("description");
        attributesToRemove.add("model");
        StandardEntityJsonTransformer carTransformer = new StandardEntityJsonTransformer("ref_Car", "ref_Car", "1.0", JsonTransformationDirection.FROM_VERSION);
        carTransformer.setAttributesToRemove(attributesToRemove);

        String srcJson = getFileContent("removeAttributes.json");
        String resultJson = carTransformer.transformJson(srcJson);

        DocumentContext context = JsonPath.parse(resultJson);
        assertEquals("ref_Car", context.read("$._entityName"));
        assertEquals("VIN-01", context.read("$.vin"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.model"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.description"));
    }

    protected String getFileContent(String fileName) throws IOException {
        File resource = new ClassPathResource("test_support/data/transform/" + fileName).getFile();
        return new String(Files.readAllBytes(resource.toPath()));
    }
}
