package Classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {
    private final Path logPath;
    private final long interval;
    private final ScheduledExecutorService scheduler;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     *
     * @param logPath
     * @param interval
     */
    public Heartbeat(Path logPath, long interval) {
        this.logPath = logPath;
        this.interval = interval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = timestamp + " - Heartbeat application started\n";
        scheduler.scheduleAtFixedRate(
                () -> writeMessage(),
                0,
                interval,
                TimeUnit.SECONDS
        );
        //create file and write first message
        logMessage(message);
    }

    private void writeMessage() {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = timestamp + " - HEARTBEAT - Application is alive\n";
        logMessage(message);
    }

    public void stop() {
        scheduler.shutdown();

        String timestamp = LocalDateTime.now().format(formatter);
        String message = timestamp + " - Heartbeat application finished\n";
        try {
            if (scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logMessage(message);
            } else {
                scheduler.shutdownNow();
                logMessage(message);
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logMessage(message);
        }
    }

    private void logMessage(String message) {
        try {
            Files.writeString(
                    logPath,
                    message,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Heartbeat logging failed: " + e.getMessage());
        }
    }
}
