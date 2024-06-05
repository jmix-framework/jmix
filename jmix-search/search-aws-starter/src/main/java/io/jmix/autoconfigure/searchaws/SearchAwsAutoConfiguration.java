/*
 * Copyright 2021 Haulmont.
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

package io.jmix.autoconfigure.searchaws;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.amazonaws.auth.*;
import com.google.common.base.Strings;
import io.jmix.autoconfigure.searchelasticsearch.SearchElasticsearchAutoConfiguration;
import io.jmix.search.SearchConfiguration;
import io.jmix.search.SearchProperties;
import io.jmix.search.utils.SslConfigurer;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;

@AutoConfiguration
@AutoConfigureBefore(SearchElasticsearchAutoConfiguration.class)
@ConfigurationPropertiesScan
public class SearchAwsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SearchConfiguration.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected SearchAwsProperties searchAwsProperties;
    @Autowired
    protected SslConfigurer sslConfigurer;

    /*@Bean("search_RestHighLevelClient")
    @ConditionalOnProperty(name = "jmix.search.server.aws.iam-auth", matchIfMissing = true)
    public RestHighLevelClient elasticSearchClient() {
        log.debug("Create ES Client with AWS IAM Authentication");
        String esUrl = searchProperties.getConnectionUrl();
        HttpHost esHttpHost = HttpHost.create(esUrl);
        RestClientBuilder restClientBuilder = RestClient.builder(esHttpHost);

        HttpRequestInterceptor interceptor = createHttpRequestInterceptor();
        SSLContext sslContext = sslConfigurer.createSslContext();
        restClientBuilder.setHttpClientConfigCallback(builder -> {
            builder.addInterceptorLast(interceptor);
            if (sslContext != null) {
                builder.setSSLContext(sslContext);
            }
            return builder;
        });

        return new RestHighLevelClientBuilder(restClientBuilder.build())
                .setApiCompatibilityMode(searchProperties.isRestHighLevelClientApiCompatibilityModeEnabled())
                .build();
    }*/

    @Bean("search_ElasticsearchClient")
    @ConditionalOnProperty(name = "jmix.search.server.aws.iam-auth", matchIfMissing = true)
    public ElasticsearchClient elasticsearchClient() {
        log.debug("Create ES Client with AWS IAM Authentication");
        String esUrl = searchProperties.getServerUrl();
        HttpRequestInterceptor interceptor = createHttpRequestInterceptor();
        SSLContext sslContext = sslConfigurer.createSslContext();


        RestClient restClient = RestClient
                .builder(HttpHost.create(esUrl))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.addInterceptorLast(interceptor);
                    if (sslContext != null) {
                        httpClientBuilder.setSSLContext(sslContext);
                    }
                    return httpClientBuilder;
                })
                .build();


        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    protected HttpRequestInterceptor createHttpRequestInterceptor() {
        String region = searchAwsProperties.getAwsRegion();
        String serviceName = searchAwsProperties.getAwsServiceName();

        AWS4Signer signer = createAWS4Signer(serviceName, region);
        AWSCredentialsProvider credentialsProvider = createAWSCredentialsProvider();
        return new AwsRequestSigningInterceptor(serviceName, signer, credentialsProvider);
    }

    protected AWS4Signer createAWS4Signer(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        return signer;
    }

    protected AWSCredentialsProvider createAWSCredentialsProvider() {
        AWSCredentialsProvider credentialsProvider;
        if (Strings.isNullOrEmpty(searchAwsProperties.getAwsAccessKey())) {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        } else {
            AWSCredentials credentials = new BasicAWSCredentials(
                    searchAwsProperties.getAwsAccessKey(),
                    searchAwsProperties.getAwsSecretKey()
            );

            credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        }
        return credentialsProvider;
    }
}