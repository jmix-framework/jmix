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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.search.elasticsearch.aws")
@ConstructorBinding
public class SearchAwsProperties {

    /**
     * Whether AWS IAM user is used to access to Elasticsearch service, false if common base authentication is used.
     */
    protected final boolean iamAuth;

    /**
     * AWS Elasticsearch region. It's used to sign requests if IAM authentication is enabled.
     */
    protected final String region;

    /**
     * AWS Elasticsearch service name. It's used to sign requests if IAM authentication is enabled.
     */
    protected final String serviceName;

    /**
     * Access Key of AWS IAM user that is used to access to Elasticsearch service if IAM authentication is enabled.
     */
    protected final String accessKey;

    /**
     * Secret Key of AWS IAM user that is used to access to Elasticsearch service if IAM authentication is enabled.
     */
    protected final String secretKey;

    public SearchAwsProperties(
            @DefaultValue("true") boolean iamAuth,
            String region,
            @DefaultValue("es") String serviceName,
            String accessKey,
            String secretKey) {
        this.iamAuth = iamAuth;
        this.region = region;
        this.serviceName = serviceName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /**
     * @see #iamAuth
     */
    public boolean isElasticsearchAwsIamAuthentication() {
        return iamAuth;
    }

    /**
     * @see #region
     */
    public String getElasticsearchAwsRegion() {
        return region;
    }

    /**
     * @see #serviceName
     */
    public String getElasticsearchAwsServiceName() {
        return serviceName;
    }

    /**
     * @see #accessKey
     */
    public String getElasticsearchAwsAccessKey() {
        return accessKey;
    }

    /**
     * @see #secretKey
     */
    public String getElasticsearchAwsSecretKey() {
        return secretKey;
    }
}