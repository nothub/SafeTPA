package not.hub.safetpa;

import org.bukkit.configuration.file.FileConfiguration;

import java.nio.file.Path;

public final class Config {
    private static boolean initialized = false;
    private static synchronized void assertInitialized() {
        if (!initialized) throw new IllegalStateException("Config access prior to initialization!");
    }

    public static synchronized void load(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();

        config.addDefault("allow-multi-target-request", true);
        config.addDefault("request-timeout-seconds", 60);
        config.addDefault("unvanish-delay-ticks", 1);
        config.addDefault("spawn-tp-deny", true);
        config.addDefault("spawn-tp-deny-radius", 1500);
        config.addDefault("distance-limit", false);
        config.addDefault("distance-limit-radius", 10000);
        config.addDefault("tp-delay-seconds", 0);
        config.addDefault("ignores-path", Ignores.defaultPath.apply(plugin));
        config.addDefault("debug", false);
        config.options().copyDefaults(true);
        plugin.saveConfig();


        allowMultiTargetRequest = config.getBoolean("allow-multi-target-request");

        if (config.getInt("request-timeout-seconds") < 10) {
            config.set("request-timeout-seconds", 10);
            plugin.saveConfig();
        }
        requestTimeoutSeconds = config.getInt("request-timeout-seconds");

        if (config.getInt("unvanish-delay-ticks") < 1) {
            config.set("unvanish-delay-ticks", 1);
            plugin.saveConfig();
        }
        unvanishDelayTicks = config.getInt("unvanish-delay-ticks");

        spawnTpDeny = config.getBoolean("spawn-tp-deny");

        if (config.getInt("spawn-tp-deny-radius") < 16) {
            config.set("spawn-tp-deny-radius", 16);
            plugin.saveConfig();
        }
        spawnTpDenyRadius = config.getInt("spawn-tp-deny-radius");

        distanceLimit = config.getBoolean("distance-limit");

        if (config.getInt("distance-limit-radius") < 16) {
            config.set("distance-limit-radius", 16);
            plugin.saveConfig();
        }
        distanceLimitRadius = config.getInt("distance-limit-radius");

        if (config.getInt("tp-delay-seconds") < 0) {
            config.set("tp-delay-seconds", 0);
            plugin.saveConfig();
        }
        tpDelaySeconds = config.getInt("tp-delay-seconds");

        if (config.getString("ignores-path") == null || config.getString("ignores-path").isBlank()) {
            config.set("ignores-path", Ignores.defaultPath.apply(plugin));
            plugin.saveConfig();
        }
        if (!Paths.isValid(config.getString("ignores-path"))) {
            config.set("ignores-path", Ignores.defaultPath.apply(plugin));
            plugin.saveConfig();
        }
        ignoresPath = Path.of(config.getString("ignores-path"));

        debug = config.getBoolean("debug");


        initialized = true;
    }

    private static boolean allowMultiTargetRequest;
    public static boolean allowMultiTargetRequest() {
        assertInitialized();
        return allowMultiTargetRequest;
    }

    private static int requestTimeoutSeconds;
    public static int requestTimeoutSeconds() {
        assertInitialized();
        return requestTimeoutSeconds;
    }

    private static int unvanishDelayTicks;
    public static int unvanishDelayTicks() {
        assertInitialized();
        return unvanishDelayTicks;
    }

    private static boolean spawnTpDeny;
    public static boolean spawnTpDeny() {
        assertInitialized();
        return spawnTpDeny;
    }

    private static int spawnTpDenyRadius;
    public static int spawnTpDenyRadius() {
        assertInitialized();
        return spawnTpDenyRadius;
    }

    private static boolean distanceLimit;
    public static boolean distanceLimit() {
        assertInitialized();
        return distanceLimit;
    }

    private static int distanceLimitRadius;
    public static int distanceLimitRadius() {
        assertInitialized();
        return distanceLimitRadius;
    }

    private static int tpDelaySeconds;
    public static int tpDelaySeconds() {
        assertInitialized();
        return tpDelaySeconds;
    }

    private static Path ignoresPath;
    public static Path ignoresPath() {
        assertInitialized();
        return ignoresPath;
    }

    private static boolean debug;
    public static boolean debug() {
        assertInitialized();
        return debug;
    }

}
