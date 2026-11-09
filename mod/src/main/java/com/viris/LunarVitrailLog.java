package com.viris;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Lunar keeps none of Vitrail's info and warning lines in its log, which leaves shader pack problems in Lunar
 * unexplained. Under Lunar (game directory inside .lunarclient) Vitrail's logger also writes to
 * logs/vitrail.log, newest run only.
 */
public final class LunarVitrailLog {

    private static final boolean LUNAR = FabricLoader.getInstance().getGameDir().toString().contains(".lunarclient")
        || Boolean.getBoolean("viris.vitrailLog");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static Logger wrapped;
    private static Logger original;
    private static PrintWriter out;

    private LunarVitrailLog() {
    }

    public static synchronized Logger wrap(Logger logger) {
        if (!LUNAR || logger == null) {
            return logger;
        }
        if (logger == original) {
            return wrapped;
        }
        if (out == null) {
            try {
                Path file = FabricLoader.getInstance().getGameDir().resolve("logs").resolve("vitrail.log");
                Files.createDirectories(file.getParent());
                out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8), true);
            } catch (IOException e) {
                return logger;
            }
        }
        original = logger;
        wrapped = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[] {Logger.class},
            (proxy, method, args) -> {
                String level = method.getName();
                if (args != null && args.length > 0 && args[0] instanceof String format
                    && (level.equals("info") || level.equals("warn") || level.equals("error"))) {
                    Object[] rest = java.util.Arrays.copyOfRange(args, 1, args.length);
                    if (rest.length == 1 && rest[0] instanceof Object[] array) {
                        rest = array;
                    }
                    var message = MessageFormatter.arrayFormat(format, rest);
                    synchronized (LunarVitrailLog.class) {
                        out.println("[" + LocalTime.now().format(TIME) + "] [" + Thread.currentThread().getName() + "/"
                            + level.toUpperCase() + "] " + message.getMessage());
                        if (message.getThrowable() != null) {
                            message.getThrowable().printStackTrace(out);
                        }
                    }
                }
                try {
                    return method.invoke(logger, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        return wrapped;
    }
}
