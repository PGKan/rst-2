package org.pgstyle.rst2.application.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.pgstyle.rst2.application.ApplicationException;

public final class RstResources {

    private static final Pattern PLACEHOLDER;
    private static final Pattern ESCAPED_PLACEHOLDER;
    private static final Properties resources;
    private static final Map<String, byte[]> binaries;

    static {
        PLACEHOLDER = Pattern.compile("(?<!\\$)\\$\\{[^\\$\\{\\}]+\\}");
        ESCAPED_PLACEHOLDER = Pattern.compile("\\$\\$\\{[^\\$\\{\\}]+\\}");
        binaries = new HashMap<>();
        resources = RstResources.loadResources();
    }

    public static boolean exist(String key) {
        return RstResources.resources.containsKey(key);
    }

    public static String get(String key) {
        return RstResources.exist(key) ? RstResources.resources.getProperty(key) : ("missing resource " + key);
    }

    public static InputStream getStream(String key) {
        String uuid = RstResources.resources.getProperty(key);
        return RstResources.binaries.containsKey(uuid) ? new ByteArrayInputStream(RstResources.binaries.get(uuid)) : null;
    }

    private static Properties loadResources() {
        try {
            Properties resources = new Properties();
            resources.load(new StringReader(RstUtils.loadResourceAsString("/META-INF/org/pgstyle/rst/resources")));
            RstResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, RstResources.binary(k.toString(), v.toString())));
            RstResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, RstResources.extern(k.toString(), v.toString())));
            RstResources.resolve(resources);
            resources.forEach((k, v) -> resources.put(k, RstResources.descape(v.toString())));
            return resources;
        } catch (IOException | IllegalArgumentException e) {
            throw new ApplicationException("fail to load resource entries", e);
        }
    }

    private static String descape(String value) {
        Matcher placeholders = RstResources.PLACEHOLDER.matcher(value);
        while (placeholders.find()) {
            String source = placeholders.group();
            value = value.replace(source, "");
        }
        Matcher escaped = RstResources.ESCAPED_PLACEHOLDER.matcher(value);
        while (escaped.find()) {
            String source = escaped.group();
            value = value.replace(source, source.substring(1));
        }
        return value;
    }

    private static String extern(String key, String value) {
        try {
            return Optional.ofNullable(value).filter(v -> v.startsWith("&extern ")).map(v -> RstResources.loadExtern(v.substring(8))).orElse(value);
        }
        catch (RuntimeException e) {
            throw new ApplicationException("fail to load extern resource: " + key, e);
        }
    }

    private static String binary(String key, String value) {
        try {
            return Optional.ofNullable(value).filter(v -> v.startsWith("&binary ")).map(v -> {
                String uuid = UUID.randomUUID().toString();
                String path = v.substring(8);
                try {
                    RstResources.binaries.put(uuid, RstUtils.read(RstUtils.class.getResourceAsStream("/META-INF/" + path)));
                } catch (IOException e) {
                    throw new ApplicationException("fail to load resource: " + path, e);
                }
                return uuid;
            }).orElse(value);
        }
        catch (RuntimeException e) {
            throw new ApplicationException("fail to load binary resource: " + key, e);
        }
    }

    private static String loadExtern(String string) {
        return Arrays.stream(string.split("(?<!\\\\)[ ;]+")).filter(s -> !s.isEmpty())
            .map(p -> RstResources.loadFromResource("/META-INF/" + p)).collect(Collectors.joining());
    }

    private static String loadFromResource(String path) {
        try {
            return RstUtils.loadResourceAsString(path);
        } catch (IOException | IllegalArgumentException e) {
            throw new ApplicationException("failed to load Jar resource: " + path, e);
        }
    }

    private static Properties resolve(Properties properties) {
        boolean resolved = false;
        String identity = properties.toString();
        while (!resolved) {
            properties.forEach((k, v) -> properties.put(k, RstResources.resolve(properties, v.toString())));
            String newIdentity = properties.toString();
            resolved = identity.equals(newIdentity);
            identity = newIdentity;
        }
        return properties;
    }

    private static String resolve(Properties properties, String value) {
        Matcher placeholders = RstResources.PLACEHOLDER.matcher(value);
        while (placeholders.find()) {
            String source = placeholders.group();
            String sourceValue = properties.getProperty(source.substring(2, source.length() - 1), "");
            if (!sourceValue.isEmpty() && !sourceValue.startsWith("&extern ")) {
                value = value.replace(source, sourceValue);
            }
        }
        return value;
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private RstResources() {
        throw new UnsupportedOperationException("unnewable");
    }

}
