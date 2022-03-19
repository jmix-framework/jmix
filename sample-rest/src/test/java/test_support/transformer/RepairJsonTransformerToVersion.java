/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package test_support.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import io.jmix.rest.transform.AbstractEntityJsonTransformer;
import io.jmix.rest.transform.JsonTransformationDirection;

/**
 *
 */
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
