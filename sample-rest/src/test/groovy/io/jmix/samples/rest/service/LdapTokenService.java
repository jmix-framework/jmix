/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LdapTokenService {
    @FormUrlEncoded
    @POST("ldap/token")
    Call<AccessToken> token(@Field("username") String username, @Field("password") String password,
                            @Field("grant_type") String grantType);
}
