package io.github.splotycode.mosaik.util.io;

import io.github.splotycode.mosaik.util.ExceptionUtil;
import io.github.splotycode.mosaik.util.collection.ArrayUtil;
import io.github.splotycode.mosaik.util.exception.ResourceNotFoundException;
import io.github.splotycode.mosaik.util.logger.Logger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IOUtil {

    public static final int THREAD_LOCAL_BUFFER_LENGTH = 1024 * 20;
    private static final ThreadLocal<byte[]> BUFFER = ThreadLocal.withInitial(() -> new byte[THREAD_LOCAL_BUFFER_LENGTH]);

    private static Logger logger = Logger.getInstance(IOUtil.class);

    public static String loadText(Reader reader, int length) {
        char[] chars = new char[length];
        int count = 0;
        while (count < chars.length) {
            int n = 0;
            try {
                n = reader.read(chars, count, chars.length - count);
            } catch (IOException e) {
                ExceptionUtil.throwRuntime(e);
            }
            if (n <= 0) break;
            count += n;
        }
        if (count == chars.length) {
            return new String(chars);
        }
        char[] newChars = new char[count];
        System.arraycopy(chars, 0, newChars, 0, count);
        return new String(newChars);
    }

    public static List<String> loadLines(InputStream stream) {
        return loadLines(stream, StandardCharsets.UTF_8);
    }

    public static List<String> loadLines(InputStream stream, Charset charset) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, charset))) {
            return loadLines(br);
        } catch (IOException e) {
            ExceptionUtil.throwRuntime(e);
        }
        return Collections.emptyList();
    }

    public static List<String> loadLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    public static byte[] loadBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        copy(stream, buffer);
        return buffer.toByteArray();
    }

    public static byte[] loadBytes(InputStream stream, int length) throws IOException {
        if (length == 0) {
            return ArrayUtil.EMPTY_BYTE_ARRAY;
        }
        byte[] bytes = new byte[length];
        int count = 0;
        while (count < length) {
            int n = stream.read(bytes, count, length - count);
            if (n <= 0) break;
            count += n;
        }
        return bytes;
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (inputStream instanceof FileInputStream && outputStream instanceof FileOutputStream) {
            final FileChannel fromChannel = ((FileInputStream)inputStream).getChannel();
            try {
                final FileChannel toChannel = ((FileOutputStream)outputStream).getChannel();
                try {
                    fromChannel.transferTo(0, Long.MAX_VALUE, toChannel);
                } finally {
                    toChannel.close();
                }
            } finally {
                fromChannel.close();
            }
        } else {
            final byte[] buffer = BUFFER.get();
            while (true) {
                int read = inputStream.read(buffer);
                if (read < 0) break;
                outputStream.write(buffer, 0, read);
            }
        }
    }

    public static String loadText(final InputStream input) throws IOException {
        return loadText(input, StandardCharsets.UTF_8);
    }

    public static String loadText(final InputStream input, final Charset encoding) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(encoding.name());
    }

    public static byte[] loadFirstAndClose(InputStream stream, int maxLength) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            IOUtil.copy(stream, maxLength, buffer);
        }
        finally {
            stream.close();
        }
        return buffer.toByteArray();
    }

    public static void copy(InputStream inputStream, long maxSize, OutputStream outputStream) throws IOException {
        final byte[] buffer = BUFFER.get();
        long toRead = maxSize;
        while (toRead > 0) {
            int read = inputStream.read(buffer, 0, (int)Math.min(buffer.length, toRead));
            if (read < 0) break;
            toRead -= read;
            outputStream.write(buffer, 0, read);
        }
    }

    public static String resourceToText(final String name) throws IOException {
        return resourceToText(name, StandardCharsets.UTF_8);
    }

    public static String resourceToText(final String name, final Charset encoding) throws IOException {
        return resourceToText(name, encoding, null);
    }

    public static String resourceToText(final String name, final Charset encoding, final ClassLoader classLoader) throws IOException {
        return loadText(resourceToURL(name, classLoader), encoding);
    }

    public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
        final URL resource = classLoader == null ? IOUtil.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) throw new IOException("Resource not found: " + name);
        return resource;
    }

    public static String loadText(final URL url, final Charset encoding) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return loadText(inputStream, encoding);
        }
    }

    public static InputStream toInputStream(final String input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(final String input, final Charset encoding) {
        return new ByteArrayInputStream(input.getBytes(encoding));
    }

    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(input, output);
            return output.toByteArray();
        }
    }

    public static Path getResourcePath(String path) {
        URL url = IOUtil.class.getResource(path);
        if (url == null) {
            throw new ResourceNotFoundException("Resource not found: " + path);
        }
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            ExceptionUtil.throwRuntime(e);
        }
        return null;
    }

}
