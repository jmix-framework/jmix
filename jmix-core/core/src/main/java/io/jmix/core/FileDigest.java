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

package io.jmix.core;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * A digest of a file content: an algorithm and a value.
 * <p>
 * The digest of a file saved by a {@link FileStorage} is kept in a {@link FileRef} parameter. The parameter key
 * is {@value #RESERVED_PARAMETER_PREFIX} followed by the algorithm name from the IANA "Hash Algorithms for HTTP
 * Digest Fields" registry (RFC 9530), the value is the digest in lowercase hex, for example
 * {@code jmix-sha-256=ba7816bf...}. Only {@link #SHA_256} is supported.
 * <p>
 * Every key that starts with {@value #RESERVED_PARAMETER_PREFIX} is reserved for the framework.
 */
@NullMarked
public final class FileDigest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The name of the SHA-256 algorithm in the IANA registry.
     */
    public static final String SHA_256 = "sha-256";

    /**
     * The prefix of the {@link FileRef} parameter keys reserved for the framework.
     */
    public static final String RESERVED_PARAMETER_PREFIX = "jmix-";

    private static final Map<String, String> JCA_ALGORITHM_NAMES = Map.of(SHA_256, "SHA-256");

    private final String algorithm;
    private final String value;

    private FileDigest(String algorithm, String value) {
        this.algorithm = algorithm;
        this.value = value;
    }

    /**
     * Creates a digest. The value is converted to lowercase and is not validated otherwise.
     *
     * @param algorithm algorithm name from the IANA registry, only {@link #SHA_256} is supported
     * @param value     digest value in hex
     * @throws IllegalArgumentException if the algorithm is not supported
     */
    public static FileDigest of(String algorithm, String value) {
        checkNotNullArgument(algorithm);
        checkNotNullArgument(value);
        checkSupported(algorithm);
        return new FileDigest(algorithm, value.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the digest kept in the {@link FileRef} parameters or {@code null} if there is none.
     * The value is not validated, so a malformed one makes a digest that differs from any calculated.
     */
    public static @Nullable FileDigest fromFileRef(FileRef reference) {
        checkNotNullArgument(reference);
        String value = reference.getParameters().get(RESERVED_PARAMETER_PREFIX + SHA_256);
        return value == null ? null : of(SHA_256, value);
    }

    /**
     * Returns the algorithm name from the IANA registry.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the digest value in lowercase hex.
     */
    public String getValue() {
        return value;
    }

    /**
     * Adds this digest to the {@link FileRef} parameters.
     */
    public void addTo(FileRef reference) {
        checkNotNullArgument(reference);
        reference.addParameter(RESERVED_PARAMETER_PREFIX + algorithm, value);
    }

    /**
     * Returns {@code true} if the {@link FileRef} parameter key is reserved for the framework, that is starts with
     * {@value #RESERVED_PARAMETER_PREFIX}. The check is case-sensitive.
     */
    public static boolean isReservedParameter(String key) {
        checkNotNullArgument(key);
        return key.startsWith(RESERVED_PARAMETER_PREFIX);
    }

    /**
     * Checks that none of the {@link FileRef} parameter keys is reserved for the framework.
     *
     * @throws IllegalArgumentException if a key is reserved
     */
    public static void checkNoReservedParameters(Collection<String> keys) {
        checkNotNullArgument(keys);
        for (String key : keys) {
            if (isReservedParameter(key)) {
                throw new IllegalArgumentException("Parameter '" + key + "' is reserved: keys starting with '"
                        + RESERVED_PARAMETER_PREFIX + "' are used by the framework");
            }
        }
    }

    static MessageDigest createMessageDigest(String algorithm) {
        checkSupported(algorithm);
        try {
            return MessageDigest.getInstance(JCA_ALGORITHM_NAMES.get(algorithm));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Digest algorithm is not available: " + algorithm, e);
        }
    }

    private static void checkSupported(String algorithm) {
        if (!JCA_ALGORITHM_NAMES.containsKey(algorithm)) {
            throw new IllegalArgumentException("Unsupported digest algorithm: " + algorithm);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDigest that = (FileDigest) o;
        return algorithm.equals(that.algorithm) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, value);
    }

    @Override
    public String toString() {
        return algorithm + ":" + value;
    }
}
