/*
 * Copyright 2025 Haulmont.
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

package element_collection;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.addon1.TestAddon1Configuration;
import test_support.app.TestAppConfiguration;
import test_support.app.entity.element_collection.EcAlpha;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CoreConfiguration.class, TestAddon1Configuration.class, TestAppConfiguration.class})
public class ElementCollectionTest {

    @Autowired
    Metadata metadata;
    @Autowired
    MetadataTools metadataTools;

    @Test
    void testMetadata() {
        MetaClass metaClass = metadata.getClass(EcAlpha.class);

        MetaProperty property = metaClass.findProperty("tags");
        assertThat(property).isNotNull();

        MetaProperty.Type propertyType = property.getType();
        assertThat(propertyType).isEqualTo(MetaProperty.Type.DATATYPE);

        Range propertyRange = property.getRange();
        assertThat(propertyRange.isDatatype()).isTrue();

        Range.Cardinality cardinality = propertyRange.getCardinality();
        assertThat(cardinality.isMany()).isTrue();

        assertThat(propertyRange.isOrdered()).isTrue();

        assertThat(metadataTools.isJpa(property)).isTrue();
    }

    @Test
    void testInitialization() {
        EcAlpha alpha = metadata.create(EcAlpha.class);

        assertThat(alpha.getTags()).isNotNull();
        assertThat(alpha.getTags()).isInstanceOf(List.class);
        assertThat(alpha.getTags()).isEmpty();

        assertThat(alpha.getNumbers()).isNotNull();
        assertThat(alpha.getNumbers()).isInstanceOf(Set.class);
        assertThat(alpha.getNumbers()).isEmpty();
    }
}
