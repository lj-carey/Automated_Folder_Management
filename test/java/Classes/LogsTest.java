package Classes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LogsTest {
    //These tests use @order to ensure tests are ran in the correct order,
    //as the log files are created and written to in a specific sequence.

    //Create temporary directory to use as the user's home directory for testing
    @TempDir
    static Path temporaryHome;

    //Static variables for storing the original user home and the log directory path
    private static String originalUserHome;
    private static Path logDirectory;

    //Before testing create the documents and logs directories
    @BeforeAll
    static void setUpDocumentsDirectory() throws IOException {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryHome.toString());
        logDirectory = Files.createDirectories(
                temporaryHome.resolve("Documents").resolve("00_logs"));
    }

    //After each test, reset the logPath field in the Logs class to ensure a clean state for subsequent tests
    @AfterEach
    void resetLogPath() throws ReflectiveOperationException {
        var field = Logs.class.getDeclaredField("logPath");
        field.setAccessible(true);
        field.set(null, logDirectory);
    }
    //After testing reset the user home variable
    @AfterAll
    static void restoreUserHome() {
        if (originalUserHome == null) {
            System.clearProperty("user.home");
        } else {
            System.setProperty("user.home", originalUserHome);
        }
    }

    //Test to check that the Document and Log directory have been correctly created.
    @Test
    @Order(1)
    void returnsTheDocumentsLogDirectory() {
        assertEquals(logDirectory, Logs.getLogPath());
    }

    //Test to verify that the writeToError method correctly writes an error message to the daily error log file.
    @Test
    @Order(2)
    void writesErrorsToTheDailyErrorLog() throws IOException {
        Logs.writeToError(new IOException("Test filesystem error"));

        Path errorLog = logDirectory.resolve(todayFileName(" Errors.log"));
        assertTrue(Files.readString(errorLog).contains("Test filesystem error"));
    }

    //Test to verify that the writeToPathExists method correctly writes a message about
    //a destination path conflict to the daily error log file.
    @Test
    @Order(3)
    void writesDestinationConflictsToTheDailyErrorLog() throws IOException {
        Path destination = Path.of("Documents", "01_Main", "report.txt");

        Logs.writeToPathExists(destination);

        String errors = Files.readString(logDirectory.resolve(todayFileName(" Errors.log")));
        assertTrue(errors.contains("The destination path: " + destination));
        assertTrue(errors.contains("Already exists"));
    }

    //Test to verify that the writeFolderMovement method correctly logs the details
    //of a folder movement to the daily movement log file.
    @Test
    @Order(4)
    void writesFolderMovementDetailsToTheDailyMovementLog() throws IOException {
        Path source = Path.of("Documents", "Projects", "report.txt");
        FileTime timestamp = FileTime.from(Instant.parse("2024-01-02T03:04:05Z"));
        FileInfo info = new FileInfo(source, timestamp, timestamp, false);
        Path destination = Path.of("Documents", "01_Main", "report.txt");

        Logs.writeFolderMovement(info, destination);

        Path movementLog = logDirectory.resolve(todayFileName(" Movement.log"));
        String movements = Files.readString(movementLog);
        assertTrue(movements.contains("Folder: " + source));
        assertTrue(movements.contains("Directory: false"));
        assertTrue(movements.contains("Moved to: " + destination));
        assertTrue(movements.contains("Last accessed/modified: " + info.getActivityString()));
    }

    //Helper method to aid in creating File names for the log files based on the current date and a given suffix.
    //E.g. if the suffix is " Errors.log" and the current date is 2024-06-15,
    //the method will return "2024 06 15 Errors.log".
    private static String todayFileName(String suffix) {
        return LocalDate.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy MM dd")) + suffix;
    }
}
