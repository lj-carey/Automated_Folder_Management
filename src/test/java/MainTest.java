import Classes.Rule;
import Classes.FileInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private final Path mainPath = Path.of("01_Main");
    private final Path archivedPath = Path.of("02_Archived");

    @TempDir
    static Path temporaryHome;

    private String originalUserHome;
    private Path documents;

    @BeforeEach
    void setUpDocumentsDirectory() throws IOException {
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryHome.toString());
        documents = Files.createDirectories(temporaryHome.resolve("Documents"));
        Files.createDirectories(documents.resolve("00_Logs"));
    }

    @AfterEach
    void restoreUserHome() {
        if (originalUserHome == null) {
            System.clearProperty("user.home");
        } else {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void skipsMovementWhenDestinationAlreadyExists() throws IOException {
        Path sourceRoot = Files.createDirectory(documents.resolve("ExistingSource"));
        Path sourceFile = Files.writeString(sourceRoot.resolve("report.txt"), "source");
        Path destinationRoot = Files.createDirectories(documents.resolve("01_Main"));
        Path existingDestination = Files.writeString(destinationRoot.resolve("report.txt"), "existing");

        moveItems(List.of(activeRule(destinationRoot)), List.of(fileInfo(sourceFile)));

        assertTrue(Files.exists(sourceFile));
        assertEquals("existing", Files.readString(existingDestination));
    }

    @Test
    void movesEmptyFoldersAndKeepsTheirSourceParent() throws IOException {
        Path sourceRoot = Files.createDirectory(documents.resolve("EmptySource"));
        Path emptyFolder = Files.createDirectory(sourceRoot.resolve("EmptyFolder"));
        Path destinationRoot = documents.resolve("01_Main");

        moveItems(List.of(activeRule(destinationRoot)), List.of(fileInfo(emptyFolder)));

        Path movedEmptyFolder = destinationRoot.resolve("EmptyFolder");
        assertTrue(Files.isDirectory(movedEmptyFolder));
        try (var contents = Files.list(movedEmptyFolder)) {
            assertTrue(contents.findAny().isEmpty());
        }
        assertTrue(Files.isDirectory(sourceRoot));
        try (var contents = Files.list(sourceRoot)) {
            assertTrue(contents.findAny().isEmpty());
        }
    }

    @Test
    void continuesMovingItemsAfterAnEarlierMovementFails() throws IOException {
        Path sourceRoot = Files.createDirectory(documents.resolve("PartialFailureSource"));
        Path missingSource = sourceRoot.resolve("missing.txt");
        Path validSource = Files.writeString(sourceRoot.resolve("valid.txt"), "content");
        Path destinationRoot = documents.resolve("01_Main");

        moveItems(
                List.of(activeRule(destinationRoot)),
                List.of(fileInfo(missingSource), fileInfo(validSource)));

        assertTrue(Files.exists(destinationRoot.resolve("valid.txt")));
        assertFalse(Files.exists(validSource));
    }

    @Test
    void preservesNestedFolderStructureWhenMovingFiles() throws IOException {
        Path sourceRoot = Files.createDirectory(documents.resolve("NestedSource"));
        Path nestedFolder = Files.createDirectories(sourceRoot.resolve("nested").resolve("deeper"));
        Path sourceFile = Files.writeString(nestedFolder.resolve("report.txt"), "content");
        Path destinationRoot = documents.resolve("01_Main");

        moveItems(List.of(activeRule(destinationRoot)), List.of(fileInfo(sourceFile)));

        Path movedFile = destinationRoot.resolve("nested").resolve("deeper").resolve("report.txt");
        assertTrue(Files.exists(movedFile));
        assertEquals("content", Files.readString(movedFile));
        assertFalse(Files.exists(sourceFile));
    }

    //Test to check that rules without overlapping ranges can be added to the list of rules successfully.
    @Test
    void acceptsRulesWithAdjacentNonOverlappingRanges() {
        List<Rule> rules = new ArrayList<>();

        Rule current = new Rule("Current", mainPath, 0, 13);
        Rule archived = new Rule("Archived", archivedPath, 14, 30);

        addRule(rules, current);
        addRule(rules, archived);

        assertEquals(2, rules.size());
        assertSame(current, rules.get(0));
        assertSame(archived, rules.get(1));
    }

    //Test to check that rules with overlapping ranges cannot be added to the list of rules,
    //and that an IllegalArgumentException is thrown.
    @Test
    void rejectsRangesThatOverlapAtTheirInclusiveBoundary() {
        List<Rule> rules = new ArrayList<>();

        addRule(rules, new Rule("Current", mainPath, 0, 13));

        assertThrows(IllegalArgumentException.class,
                () -> addRule(rules,
                        new Rule("Overlapping", archivedPath, 13, 30)));

        assertEquals(1, rules.size());
    }

    //Test to verify that rules with duplicate destination paths cannot be added to the list of rules,
    //even if their ranges do not overlap, and that an IllegalArgumentException is thrown.
    @Test
    void rejectsDuplicateDestinationsEvenWhenRangesDoNotOverlap() {
        List<Rule> rules = new ArrayList<>();

        addRule(rules, new Rule("Current", mainPath, 0, 13));

        assertThrows(IllegalArgumentException.class, () -> addRule(rules,
                        new Rule("Archived", mainPath, 14, 30)));

        assertEquals(1, rules.size());
    }

    //Test to verify that rules with duplicate destination paths cannot be added to the list of rules,
    //even if their ranges do overlap, and that the correct exceptions are thrown.
    @Test
    void rejectsDuplicateDestinationWhenRangesDoOverlap() {
        List<Rule> overlapRules = new ArrayList<>();
        overlapRules.add(new Rule("Current", mainPath, 0, 13));

        IllegalArgumentException overlapException = assertThrows(IllegalArgumentException.class,
                () -> addRule(overlapRules,
                        new Rule("Overlapping", archivedPath, 5, 12)));
        assertEquals("Rule overlaps with: Current", overlapException.getMessage());
        assertEquals(1, overlapRules.size());

        List<Rule> duplicateDestinationRules = new ArrayList<>();
        duplicateDestinationRules.add(new Rule("Current", mainPath, 0, 13));

        IllegalArgumentException destinationException = assertThrows(IllegalArgumentException.class,
                () -> addRule(duplicateDestinationRules,
                        new Rule("Archived", mainPath, 14, 30)));
        assertEquals("This parent destination is already in use: 01_Main", destinationException.getMessage());
        assertEquals(1, duplicateDestinationRules.size());
    }

    //Method to add a rule to the list of rules using reflection to access the private addRule method in the Main class.
    private static void addRule(List<Rule> rules, Rule rule) {
        try {
            //finds the addRule method in Main and sets accessibility to true to be able to use that method then invoke it
            Method method = Main.class.getDeclaredMethod("addRule", List.class, Rule.class);
            method.setAccessible(true);
            method.invoke(null, rules, rule);
        //catches exceptions and throws errors with messages and cause to help with debugging
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new AssertionError("Unexpected exception", cause);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not invoke Main.addRule", exception);
        }
    }

    private static void moveItems(List<Rule> rules, List<FileInfo> fileInfoList) throws IOException {
        try {
            Method method = Main.class.getDeclaredMethod("moveItems", List.class, List.class);
            method.setAccessible(true);
            method.invoke(null, rules, fileInfoList);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new AssertionError("Unexpected exception", cause);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not invoke Main.moveItems", exception);
        }
    }

    private static FileInfo fileInfo(Path path) throws IOException {
        FileTime timestamp = Files.exists(path)
                ? Files.getLastModifiedTime(path)
                : FileTime.from(Instant.now());
        return new FileInfo(path, timestamp, timestamp, Files.isDirectory(path));
    }

    private static Rule activeRule(Path destination) {
        return new Rule("Active", destination, 0, 999);
    }
}
