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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * An input stream that calculates the {@link FileDigest} of the bytes read through it, including the skipped ones.
 * The stream does not support mark and reset.
 */
@NullMarked
public class FileDigestInputStream extends FilterInputStream {

    private static final int SKIP_BUFFER_SIZE = 8192;

    private final String algorithm;
    private final MessageDigest messageDigest;

    /**
     * Creates a stream that calculates the {@link FileDigest#SHA_256} digest.
     */
    public FileDigestInputStream(InputStream in) {
        this(in, FileDigest.SHA_256);
    }

    /**
     * Creates a stream that calculates the digest with the given algorithm.
     *
     * @param in        the underlying stream
     * @param algorithm algorithm name from the IANA registry, only {@link FileDigest#SHA_256} is supported
     * @throws IllegalArgumentException if the algorithm is not supported
     */
    public FileDigestInputStream(InputStream in, String algorithm) {
        super(in);
        checkNotNullArgument(in);
        checkNotNullArgument(algorithm);
        this.algorithm = algorithm;
        this.messageDigest = FileDigest.createMessageDigest(algorithm);
    }

    /**
     * Returns the digest of every byte read so far. The stream stays usable, so the method can be called
     * repeatedly.
     */
    public FileDigest getDigest() {
        try {
            MessageDigest snapshot = (MessageDigest) messageDigest.clone();
            return FileDigest.of(algorithm, HexFormat.of().formatHex(snapshot.digest()));
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Digest algorithm does not support snapshots: " + algorithm, e);
        }
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            messageDigest.update((byte) b);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int count = in.read(b, off, len);
        if (count > 0) {
            messageDigest.update(b, off, count);
        }
        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        byte[] buffer = new byte[(int) Math.min(n, SKIP_BUFFER_SIZE)];
        long remaining = n;
        while (remaining > 0) {
            int count = read(buffer, 0, (int) Math.min(remaining, buffer.length));
            if (count < 0) {
                break;
            }
            remaining -= count;
        }
        return n - remaining;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        // Not supported.
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
