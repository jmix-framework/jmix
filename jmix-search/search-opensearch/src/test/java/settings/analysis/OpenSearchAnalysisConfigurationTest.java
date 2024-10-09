/*
 * Copyright 2024 Haulmont.
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


package settings.analysis;

import io.jmix.core.*;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.searchopensearch.index.OpenSearchIndexSettingsProvider;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opensearch.client.opensearch.indices.IndexSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {OpenSearchIndexingTestConfiguration.class, OpenSearchIndexSettingsProvider.class, OpenSearchAnalysisIndexSettingsConfigurer.class}
)
public class OpenSearchAnalysisConfigurationTest {

    @Autowired
    protected EntityIndexer entityIndexer;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected SystemAuthenticator authenticator;
    @Autowired
    protected IdSerialization idSerialization;
    @Autowired
    protected TestFileStorage fileStorage;
    @Autowired
    protected OpenSearchIndexSettingsProvider configProvider;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;

    @Test
    @DisplayName("OpenSearch configuration values test")
    public void testOpenSearchAnalysisConfiguration() {
        IndexSettings rootEntitySettings = configProvider.getSettingsForIndex(indexConfigurationManager.getIndexConfigurationByEntityName("test_RootEntity"));

        Assert.assertTrue(rootEntitySettings.index().maxResultWindow() == 15000);

        Assert.assertTrue(rootEntitySettings.analysis().analyzer().get("customized_standard").standard().maxTokenLength() == 150);

        IndexSettings rootEntityHDSettings = configProvider.getSettingsForIndex(indexConfigurationManager.getIndexConfigurationByEntityName("test_RootEntityHD"));
        Assert.assertTrue(rootEntityHDSettings.index().maxRegexLength() == 2000);
    }

}

