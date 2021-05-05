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

package io.jmix.search;

import com.amazonaws.auth.*;
import com.google.common.base.Strings;
import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import io.jmix.search.aws.AwsRequestSigningInterceptor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
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
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class})
@EnableTransactionManagement
@EnableScheduling
public class SearchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchConfiguration.class);

    @Autowired
    protected SearchApplicationProperties searchApplicationProperties;

    @Bean("search_RestHighLevelClient")
    public RestHighLevelClient elasticSearchClient() {
        String esUrl = searchApplicationProperties.getElasticsearchUrl();
        HttpHost esHttpHost = HttpHost.create(esUrl);
        RestClientBuilder restClientBuilder = RestClient.builder(esHttpHost);

        if (searchApplicationProperties.isElasticsearchAwsIamAuthentication()) {
            log.debug("Use ES {} with AWS IAM authentication", esUrl);

            String region = searchApplicationProperties.getElasticsearchAwsRegion();
            String serviceName = searchApplicationProperties.getElasticsearchAwsServiceName();

            AWS4Signer signer = new AWS4Signer();
            signer.setServiceName(serviceName);
            signer.setRegionName(region);

            AWSCredentialsProvider credentialsProvider;
            if (Strings.isNullOrEmpty(searchApplicationProperties.getElasticsearchAwsAccessKey())) {
                credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
            } else {
                AWSCredentials credentials = new BasicAWSCredentials(
                        searchApplicationProperties.getElasticsearchAwsAccessKey(),
                        searchApplicationProperties.getElasticsearchAwsSecretKey()
                );

                credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            }

            HttpRequestInterceptor interceptor = new AwsRequestSigningInterceptor(serviceName, signer, credentialsProvider);
            restClientBuilder.setHttpClientConfigCallback(builder -> builder.addInterceptorLast(interceptor));
        } else {
            log.debug("Use ES {} with common basic authentication", esUrl);
            if (!Strings.isNullOrEmpty(searchApplicationProperties.getElasticsearchLogin())) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(searchApplicationProperties.getElasticsearchLogin(), searchApplicationProperties.getElasticsearchPassword())
                );
                restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
            }
        }

        return new RestHighLevelClient(restClientBuilder);
    }
}
