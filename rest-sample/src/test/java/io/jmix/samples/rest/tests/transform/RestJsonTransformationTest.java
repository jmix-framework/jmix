/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.tests.transform;

import com.haulmont.cuba.core.global.BeanLocator;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import io.jmix.rest.api.config.RestJsonTransformations;
import io.jmix.rest.api.transform.AbstractEntityJsonTransformer;
import io.jmix.rest.api.transform.JsonTransformationDirection;
import io.jmix.rest.api.transform.StandardEntityJsonTransformer;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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


/**
 *
 */
@Disabled
public class RestJsonTransformationTest {

    @Mocked
    protected BeanLocator beanLocator;

    @Mocked
    protected RestJsonTransformations restJsonTransformations;

    @Test
    public void renameEntityAndAttributes() throws Exception {

        Map<String, String> carAttributesToRename = new HashMap<>();
        carAttributesToRename.put("oldVin", "vin");
        StandardEntityJsonTransformer carTransformer = new StandardEntityJsonTransformer("ref$OldCar", "ref_Car", "1.0", JsonTransformationDirection.FROM_VERSION);
        carTransformer.setAttributesToRename(carAttributesToRename);
        initStandardTransformer(carTransformer);


        Map<String, String> modelAttributesToRename = new HashMap<>();
        modelAttributesToRename.put("oldName", "name");
        StandardEntityJsonTransformer modelTransformer = new StandardEntityJsonTransformer("ref$OldModel", "ref$Model", "1.0", JsonTransformationDirection.FROM_VERSION);
        modelTransformer.setAttributesToRename(modelAttributesToRename);
        initStandardTransformer(modelTransformer);

        new Expectations() {{
            restJsonTransformations.getTransformer("ref$OldCar", "1.0", JsonTransformationDirection.FROM_VERSION);
            result = carTransformer;
            minTimes = 0;
            restJsonTransformations.getTransformer("ref$OldModel", "1.0", JsonTransformationDirection.FROM_VERSION);
            result = modelTransformer;
            minTimes = 0;
        }};

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
        initStandardTransformer(repairTransformer);

        new Expectations() {{
            beanLocator.get(RestJsonTransformations.class);
            result = restJsonTransformations;
            minTimes = 0;
            beanLocator.get("jmix_RestJsonTransformations");
            result = restJsonTransformations;
            minTimes = 0;
            restJsonTransformations.getTransformer("ref$OldRepair", "1.0", JsonTransformationDirection.FROM_VERSION);
            result = repairTransformer;
            minTimes = 0;
        }};

        String srcJson = getFileContent("transformCompositionAttribute.json");
        String resultJson = repairTransformer.transformJson(srcJson);

        DocumentContext context = JsonPath.parse(resultJson);
        assertEquals("ref_Car", context.read("$._entityName"));
        assertEquals("VIN-01", context.read("$.vin"));

        assertEquals("ref$Repair", context.read("$.repairs.[0]_entityName"));
        assertEquals("Repair 1", context.read("$.repairs.[0].description"));

        assertThrows(PathNotFoundException.class, () -> context.read("$.repairs.[0].oldDescription"));
    }

    private void initStandardTransformer(StandardEntityJsonTransformer transformer) throws NoSuchFieldException, IllegalAccessException {
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
        initStandardTransformer(carTransformer);

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
