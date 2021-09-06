package org.pgstyle.rst2.application.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RstUtils {

    public static final String NEWLINE = System.lineSeparator();
    public static final String BACK_SLASH = "\\";
    public static final String ESCAPABLE = "\\.:;";
    public static final String SEPARATORS = ":;";

    public static String[] safeSplit(String string, char[] delimiters) {
        Objects.requireNonNull(string, "string == null");
        Objects.requireNonNull(delimiters, "delimiters == null");
        String dels = String.valueOf(delimiters);
        if (dels.contains(RstUtils.BACK_SLASH)) {
            // since backward slash can escape a delimiter,
            // the backward slash itself is forbidden as a delimiter
            throw new IllegalArgumentException("invalid delimiter");
        }

        List<String> splitted = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            Character current = string.charAt(i);
            if (current.equals('\\')) {
                // escaped character, do not consume the backward slash
                builder.append(current).append(string.charAt(++i));
            }
            else if (dels.chars().anyMatch(Integer.valueOf(string.charAt(i))::equals)) {
                // matched delimiter, store the string segment and reset the buffer
                splitted.add(builder.toString());
                builder.setLength(0);
            }
            else {
                builder.append(current);
            }
        }
        splitted.add(builder.toString());
        return splitted.size() == 1 && splitted.get(0).isEmpty() ?
               new String[0] : splitted.stream().toArray(String[]::new);
    }

    public static Stream<Character> charStream(String string) {
        return string.chars().mapToObj(i -> (char) i);
    }

    public static String loadResourceAsString(String path) throws IOException {
        Objects.requireNonNull(path, "path == null");
        return new String(RstUtils.read(RstUtils.class.getResourceAsStream(path)), StandardCharsets.UTF_8);
    }

    public static byte[] merge(byte[] array1, byte[] array2) {
        Objects.requireNonNull(array1, "array1 == null");
        Objects.requireNonNull(array2, "array2 == null");
        byte[] array = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }

    public static byte[] partition(byte[] array, int start, int length) {
        return RstUtils.partition(array, start, length, false);
    }

    public static byte[] partition(byte[] array, int start, int length, boolean padding) {
        Objects.requireNonNull(array, "argument array must not null");
        return padding ? Arrays.copyOfRange(array, start, start + length)
            : Arrays.copyOfRange(array, start, Math.min(start + length, array.length));
    }

    public static byte[] read(InputStream inputStream) throws IOException {
        return RstUtils.read(inputStream, Short.MAX_VALUE + 1 << 1);
    }

    public static int read(InputStream inputStream, byte[] buffer) throws IOException {
        int i = 0;
        inputStream = RstUtils.bufferedWrapper(inputStream);
        int b;
        while (i < buffer.length && (b = inputStream.read()) != -1) {
            buffer[i++] = (byte) b;
        }
        return i;
    }

    public static byte[] read(InputStream inputStream, int bufferSize) throws IOException {
        byte[] bytes = new byte[0];
        byte[] buffer = new byte[bufferSize];
        inputStream = RstUtils.bufferedWrapper(inputStream);
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            bytes = RstUtils.merge(bytes, RstUtils.partition(buffer, 0, length));
        }
        return bytes;
    }

    public static boolean sysin() {
        try {
            ((BufferedInputStream) System.in).available();
        } catch (IOException e) {
            // no system is available
            return false;
        }
        return true;
    }

    private static InputStream bufferedWrapper(InputStream inputStream) {
        if (inputStream instanceof BufferedInputStream) {
            return inputStream;
        }
        return new BufferedInputStream(inputStream);
    }

    public static String messageOf(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable == null");
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
    }

    public static String stackTraceOf(Throwable throwable) {
        Objects.requireNonNull(throwable, "throwable == null");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        throwable.printStackTrace(ps);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static final List<Entry<String, Integer>> dissect(String raw) {
        List<Entry<String, Integer>> list = new ArrayList<>();
        for (String statement : RstUtils.safeSplit(raw, new char[]{ ';' })) {
            if (statement.isEmpty()) {
                continue;
            }
            if (!statement.contains(":")) {
                list.add(new SimpleEntry<>(statement, Integer.MIN_VALUE));
            }
            else {
                String weight = statement.substring(0, statement.indexOf(":")).trim();
                String characters = statement.substring(statement.indexOf(":") + 1).trim();
                try {
                    list.add(new SimpleEntry<>(characters, Integer.parseInt(weight)));
                }
                catch (NumberFormatException e) {
                    list.add(new SimpleEntry<>(statement, 0));
                }
            }
        }
        return list;
    }

    public static final Entry<String, Integer> normalise(Entry<String, Integer> weight) {
        Objects.requireNonNull(weight, "weight == null");
        return new SimpleEntry<>(RstUtils.normalise(weight.getKey()), weight.getValue());
    }

    public static String normalise(String statement) {
        Objects.requireNonNull(statement, "statement == null");
        try {
            return RstUtils.normaliseUnbounded(statement);
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("unexpected encounter of EOS at column: " + e.getMessage().substring(e.getMessage().indexOf(": ") + 2), e);
        }
    }

    private static String normaliseUnbounded(String statement) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < statement.length(); i++) {
            boolean escape = RstUtils.requireNonSeperator(statement, i) == '\\' && RstUtils.isEscapable(statement.charAt(++i));
            if (escape) {
                // only necessary escape will keep the backward slash
                string.append(RstUtils.BACK_SLASH);
            }
            string.append(statement.charAt(i));
            // look forward for range operator
            if (statement.indexOf("..", i + 1) - i == 1) {
                char end = statement.charAt(i += 3);
                string.append("..").append(end);
                if (end == '\\') {
                    string.append(statement.charAt(++i));
                }
                else {
                    RstUtils.requireNonEscapable(statement, i);
                }
            }
            else if (!escape && statement.charAt(i) == '.') {
                // uncaptured and unescaped range operator
                throw new IllegalArgumentException("unexpected range operator at column: " + i);
            }
        }
        // recompressing will merge ranged sequences and convert new sequences
        return RstUtils.compress(RstUtils.expand(string.toString()));
    }

    public static String compress(String characters) {
        if (characters.isEmpty()) {
            return characters;
        }
        // clean up characters sequence, remove duplicate and sort by code
        characters = RstUtils.charStream(characters).sorted().distinct().map(String::valueOf).collect(Collectors.joining());
        StringBuilder compressed = new StringBuilder();
        int start = 0;
        int end = 0;
        for (int i = 0; i < characters.length(); i++) {
            if (start != i && characters.charAt(end) != (characters.charAt(i) - 1)) {
                // continuous sequence is broken, compress current sequence
                compressed.append(RstUtils.compress(characters.charAt(start), characters.charAt(end)));
                start = end = i;
            }
            else {
                end = i;
            }
        }
        // compress current unbroken sequence until end of string
        compressed.append(RstUtils.compress(characters.charAt(start), characters.charAt(end)));
        return compressed.toString();
    }

    private static String compress(char start, char end) {
        if (end < start) {
            throw new IllegalArgumentException(String.format("invalid ranged sequence: start:'%c', end:'%c'", start, end));
        }
        StringBuilder string = new StringBuilder(4);
        string.append(RstUtils.escape(start));
        if (end - start >= 2) {
            // only creates ranged sequence when the two characters are far enough
            string.append("..");
        }
        if (end - start >= 1) {
            string.append(RstUtils.escape(end));
        }
        return string.toString();
    }

    public static String expand(String characters) {
        StringBuilder expanded = new StringBuilder();
        for (int i = 0; i < characters.length(); i++) {
            char current = characters.charAt(i);
            if (current == '\\') {
                // check for unexpected escape characters
                current = RstUtils.requireEscapable(characters, ++i);
            }
            if (characters.indexOf("..", i) - i == 1) {
                // capture ranged statement and expand it
                char start = current;
                char last = characters.charAt(i += 3);
                last = last == '\\' ? RstUtils.requireEscapable(characters, ++i) : RstUtils.requireNonEscapable(characters, i);
                expanded.append(RstUtils.expand(start, last));
            }
            else {
                expanded.append(current);
            }
        }
        return expanded.toString();
    }

    private static String expand(char start, char end) {
        if (end < start) {
            throw new IllegalArgumentException(String.format("invalid ranged sequence: %c, %c", start, end));
        }
        StringBuilder expanded = new StringBuilder(end - start + 1);
        for (; start <= end; start++) {
            expanded.append(start);
        }
        return expanded.toString();
    }

    public static String escape(String string) {
        return RstUtils.charStream(string).map(RstUtils::escape).collect(Collectors.joining());
    }

    private static String escape(char c) {
        return RstUtils.isEscapable(c) ? RstUtils.BACK_SLASH + c : String.valueOf(c);
    }

    private static boolean isEscapable(char c) {
        return RstUtils.ESCAPABLE.contains(String.valueOf(c));
    }

    private static char requireEscapable(String characters, int index) {
        char target = characters.charAt(index);
        if (!RstUtils.isEscapable(target)) {
            throw new IllegalArgumentException(String.format("unexpected escaped character: \"\\%c\", at column: %d", target, index));
        }
        return target;
    }

    private static char requireNonEscapable(String characters, int index) {
        char target = characters.charAt(index);
        if (RstUtils.isEscapable(target)) {
            throw new IllegalArgumentException(String.format("unexpected control character: '%c', at column: %d", target, index));
        }
        return target;
    }

    private static char requireNonSeperator(String characters, int index) {
        char target = characters.charAt(index);
        if (RstUtils.SEPARATORS.contains(String.valueOf(target))) {
            throw new IllegalArgumentException(String.format("unexpected seperator: %c, at column: %d", target, index));
        }
        return target;
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private RstUtils() {
        throw new UnsupportedOperationException("unnewable");
    }

}
