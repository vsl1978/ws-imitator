package xyz.vsl.wsimitator;

import org.apache.log4j.Logger;
import xyz.vsl.wsimitator.imitator.Config;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by vsl on 14.05.2016.
 */
class ConfigWatcher extends Thread {
    private Config config;
    private static volatile boolean stop = false;

    public ConfigWatcher(Config config) {
        this.config = config;
        setDaemon(true);
        setName(config.getConfigFile().getName() + "-watcher");
    }

    @Override
    public void run() {
        File file = config.getConfigFile().getAbsoluteFile();
        Path path = file.toPath().getParent();
        String configFileName = file.getName();
        Logger logger = Logger.getLogger(getClass());

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            int delayCountdown = 0;
            while (!stop) {
                if (delayCountdown > 0) {
                    delayCountdown--;
                    if (delayCountdown == 0) {
                        logger.info("Reloading "+file);
                        config.parse();
                    }
                }
                WatchKey key;
                try {
                    key = watcher.poll(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path filename = ((WatchEvent<Path>) event).context();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            Thread.yield();
                            continue;
                        } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY && filename.toString().equals(file.getName())) {
                            delayCountdown = 5;
                        }
                        if (!key.reset())
                            break;
                    }
                }
                Thread.yield();
            }
        } catch (Exception e) {
            logger.error(file.toString(), e);
        }
    }

    public static void stopWatching() {
        stop = true;
    }
}
