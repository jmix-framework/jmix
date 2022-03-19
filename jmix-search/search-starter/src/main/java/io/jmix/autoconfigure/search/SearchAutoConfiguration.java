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

package io.jmix.autoconfigure.search;

import com.google.common.base.Strings;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.utils.ElasticsearchSslConfigurer;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, SearchConfiguration.class})
public class SearchAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchAutoConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected ElasticsearchSslConfigurer elasticsearchSslConfigurer;

    @Bean("search_RestHighLevelClient")
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    public RestHighLevelClient elasticSearchClient() {
        log.debug("Create simple ES Client");

        String esUrl = searchProperties.getElasticsearchUrl();
        HttpHost esHttpHost = HttpHost.create(esUrl);
        RestClientBuilder restClientBuilder = RestClient.builder(esHttpHost);

        CredentialsProvider credentialsProvider = createCredentialsProvider();
        SSLContext sslContext = elasticsearchSslConfigurer.createSslContext();

        restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (credentialsProvider != null) {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            if (sslContext != null) {
                httpClientBuilder.setSSLContext(sslContext);
            }
            return httpClientBuilder;
        });

        return new RestHighLevelClient(restClientBuilder);
    }

    @Nullable
    protected CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider = null;
        if (!Strings.isNullOrEmpty(searchProperties.getElasticsearchLogin())) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            searchProperties.getElasticsearchLogin(),
                            searchProperties.getElasticsearchPassword()
                    )
            );
        }
        return credentialsProvider;
    }
}
