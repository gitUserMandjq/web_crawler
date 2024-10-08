package net.sf.expectit;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 Alexey Gavrilov and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.sf.expectit.Utils.toDebugString;

/**
 * Performs copy from an input stream to a WritableByteChannel.
 */
class InputStreamCopier implements Callable<Object> {
    private static final Logger LOG = Logger.getLogger(InputStreamCopier.class.getName());

    private final InputStream from;
    private final WritableByteChannel to;
    private final int bufferSize;
    private final Appendable echo;
    private final Charset charset;
    private final boolean autoFlushEcho;

    InputStreamCopier(
            final WritableByteChannel to,
            final InputStream from,
            final int bufferSize,
            final Appendable echo,
            final Charset charset,
            final boolean autoFlushEcho) {
        this.from = from;
        this.to = to;
        this.bufferSize = bufferSize;
        this.echo = echo;
        this.charset = charset;
        this.autoFlushEcho = autoFlushEcho;
    }

    @Override
    public Object call() throws Exception {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        try {
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                if (LOG.isLoggable(Level.FINE) && bytesRead > 0) {
                    LOG.fine(
                            String.format(
                                    "Received from %s: %s",
                                    from,
                                    toDebugString(buffer, bytesRead, charset)));
                }
                if (echo != null && bytesRead > 0) {
                    printEcho(buffer, bytesRead);
                }
            }
        } finally {
            to.close();
        }
        return null;
    }

    private void printEcho(final byte[] buffer, final int bytesRead) throws IOException {
        if (charset == null) {
            if (echo instanceof OutputStream) {
                ((OutputStream) echo).write(buffer, 0, bytesRead);
            } else {
                echo.append(new String(buffer, 0, bytesRead, Charset.defaultCharset()));
            }
        } else {
            echo.append(new String(buffer, 0, bytesRead, charset));
        }
        if (autoFlushEcho) {
            Utils.flushAppendable(echo);
        }
    }

}
