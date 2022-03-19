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
public class RepairJsonTransformerFromVersion extends AbstractEntityJsonTransformer {

    public RepairJsonTransformerFromVersion() {
        super("ref$OldRepair", "ref$Repair", "1.0", JsonTransformationDirection.FROM_VERSION);
    }

    @Override
    protected void doCustomTransformations(ObjectNode rootObjectNode, ObjectMapper objectMapper) {
        JsonNode dateNode = rootObjectNode.get("date");
        if (dateNode != null) {
            String dateNodeValue = dateNode.asText();
            if (!Strings.isNullOrEmpty(dateNodeValue))
                rootObjectNode.put("date", dateNodeValue.substring(0, 10));
        }
    }

}
