/*
 * Copyright (c) 2008-2018 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.samples.rest.service;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FileLoaderTestService {
    @GET("test/file-loader/text-file")
    Call<String> loadTextFile();

    @GET("test/file-loader/removed-text-file")
    Call<String> loadRemovedTextFile();

    @GET("test/file-loader/not-found-text-file")
    Call<String> loadNotFoundTextFile();

    @GET("test/file-loader/non-committed-text-file")
    Call<String> loadNonCommittedTextFile();
}
