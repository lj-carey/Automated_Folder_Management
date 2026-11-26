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
     * Constructs a Heartbeat instance with the specified log path and interval.
     *
     * @param logPath  The path to the log file where heartbeat messages will be written.
     * @param interval The interval in seconds between heartbeat messages.
     */
    public Heartbeat(Path logPath, long interval) {
        this.logPath = logPath;
        this.interval = interval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /** Method to start the heartbeat logging process.
     * It schedules a task to write a heartbeat message to the log file at fixed intervals.
     */
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

    /** Method to write a heartbeat message to the heartbeat log file.
     *
     */
    private void writeMessage() {
        String timestamp = LocalDateTime.now().format(formatter);
        String message = timestamp + " - HEARTBEAT - Application is alive\n";
        logMessage(message);
    }

    /** Method to stop the heartbeat logging process.
     * It shuts down the scheduler and writes a final message to the log file.
     */
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

    /** Logs a message to the heartbeat log file.
     * If an IOException occurs, it prints an error message to the standard error stream.
     *
     * @param message The message to be logged.
     */
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
