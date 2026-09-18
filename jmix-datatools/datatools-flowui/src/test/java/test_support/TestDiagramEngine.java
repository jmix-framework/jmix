/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.datatools.datamodel.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Diagram engine whose reachability and failures are set by the test.
 */
public class TestDiagramEngine implements DiagramEngine {

    protected boolean serviceReachable = true;

    @Nullable
    protected RuntimeException pingFailure;

    public void setServiceReachable(boolean serviceReachable) {
        this.serviceReachable = serviceReachable;
    }

    /**
     * Makes {@link #pingService()} throw instead of returning a value, as a remote call may.
     */
    public void setPingFailure(@Nullable RuntimeException pingFailure) {
        this.pingFailure = pingFailure;
    }

    @Override
    public String constructEntityDescription(String entityName, String dataStoreName,
                                             List<AttributeModel> attributeModelList) {
        return "entity " + entityName + " {\n}\n";
    }

    @Override
    public String constructRelationDescription(String currentEntityType, String refEntityType,
                                               RelationType relationType, String dataStoreName) {
        return currentEntityType + " -- " + refEntityType + "\n";
    }

    @Override
    public byte[] generateDiagram(String entitiesDescription, String relationsDescriptions) {
        return "diagram".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean pingService() {
        if (pingFailure != null) {
            throw pingFailure;
        }
        return serviceReachable;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
