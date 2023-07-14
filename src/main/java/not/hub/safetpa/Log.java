package not.hub.safetpa;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class Log {
    private static volatile @NotNull Logger logger = Logger.getAnonymousLogger();

    public static void debug(String msg) {
        if (Config.debug()) logger.fine(msg);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warning(msg);
    }

    public static void error(String msg) {
        logger.severe(msg);
    }

    public static void set(@NotNull Logger logger) {
        Log.logger = logger;
    }

}
