/*
 * Copyright 2017 Haulmont.
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

package test_support.transformer;


import com.google.common.base.Strings;
import io.jmix.rest.transform.AbstractEntityJsonTransformer;
import io.jmix.rest.transform.JsonTransformationDirection;
import org.jspecify.annotations.NullMarked;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 *
 */
@NullMarked
public class RepairJsonTransformerToVersion extends AbstractEntityJsonTransformer {

    public RepairJsonTransformerToVersion() {
        super("ref$Repair", "ref$OldRepair", "1.0", JsonTransformationDirection.TO_VERSION);
    }

    @Override
    protected void doCustomTransformations(ObjectNode rootObjectNode, ObjectMapper objectMapper) {
        JsonNode dateNode = rootObjectNode.get("date");
        if (dateNode != null) {
            String dateNodeValue = dateNode.asText();
            if (!Strings.isNullOrEmpty(dateNodeValue))
                rootObjectNode.put("date", dateNodeValue + " 00:00:00.000");
        }
    }
}
