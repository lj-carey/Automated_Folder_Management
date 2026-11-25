package Classes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathManagerTest {
    private static final String userHome = "user.home";

    //Create a temporary directory to use as the user's home directory for testing
    @TempDir
    Path temporaryHome;

    //Create variables to hold the user's home and the documents path
    private String originalUserHome;
    private Path documents;

    //Create the document directory before each test
    @BeforeEach
    void setUpDocumentsDirectory() throws IOException {
        originalUserHome = System.getProperty(userHome);
        System.setProperty(userHome, temporaryHome.toString());
        documents = Files.createDirectories(temporaryHome.resolve("Documents"));
    }
    //Restore the user's home directory after each test
    @AfterEach
    void restoreUserHome() {
        if (originalUserHome == null) {
            System.clearProperty(userHome);
        } else {
            System.setProperty(userHome, originalUserHome);
        }
    }

    //Test to verify that the getDocumentsPath method returns the correct path based on the current user's home directory
    @Test
    void getDocumentsPathUsesTheCurrentUserHome() {
        assertEquals(documents, PathManager.getDocumentsPath());
    }

    //Test to verify that the getPathsList method returns only eligible direct child directories of the user's Documents
    // directory, excluding directories containing "locked" or "logs" in their names, as well as any files.
    @Test
    void listsOnlyEligibleDirectChildDirectories() throws Exception {
        Path ordinary = Files.createDirectory(documents.resolve("Projects"));
        Files.createDirectory(documents.resolve("Locked Items"));
        Files.createDirectory(documents.resolve("my-logs"));
        Files.createFile(documents.resolve("readme.txt"));

        assertEquals(List.of(ordinary), PathManager.getPathsList());
    }

    //Test to verify that the getPathsToMove method correctly scans a directory tree up to a specified depth, returning
    //only eligible files while skipping directories and files containing "locked", "logs", or ".ini" in their names.
    @Test
    void scansFilesToSetDepthAndCheckSkippedItems() throws IOException {
        Path root = Files.createDirectory(documents.resolve("Projects"));
        Path nested = Files.createDirectory(root.resolve("nested"));
        Path deeper = Files.createDirectory(nested.resolve("deeper"));
        Path rootFile = Files.createFile(root.resolve("root.txt"));
        Path nestedFile = Files.createFile(nested.resolve("child.txt"));
        Path deeperFile = Files.createFile(deeper.resolve("grandchild.txt"));
        Files.createFile(root.resolve("settings.ini"));
        Files.createFile(root.resolve("preferences.INI"));
        Files.createFile(nested.resolve("child.ini"));
        Files.createFile(deeper.resolve("locked-secret.txt"));
        Files.createFile(deeper.resolve("logs-output.txt"));

        List<Path> paths = PathManager.getPathsToMove(List.of(root), 3);

        assertEquals(Set.of(rootFile, nestedFile, deeperFile), new HashSet<>(paths));
        assertEquals(3, paths.size());
    }

    //Test to verify that the getPathsToMove method returns an empty list when the specified depth is zero,
    // and that it still initializes the scan log file.
    @Test
    void zeroDepthReturnsNoPathsAndStillInitializesScanLog() throws IOException {
        Path root = Files.createDirectory(documents.resolve("Projects"));

        assertTrue(PathManager.getPathsToMove(List.of(root), 0).isEmpty());
        assertTrue(Files.exists(documents.resolve("00_Logs").resolve("ErrorWithPath.log")));
    }

    //Test to verify that the getDestinationPath method correctly constructs a destination path for a given file
    @Test
    void destinationPreservesNestedPathWithoutTheOriginalRootFolder() throws IOException {
        Path sourceFolder = Files.createDirectory(documents.resolve("Projects"));
        Path nestedFolder = Files.createDirectory(sourceFolder.resolve("nested"));
        Path file = Files.createFile(nestedFolder.resolve("report.txt"));
        var timestamp = Files.getLastModifiedTime(file);
        FileInfo info = new FileInfo(file, timestamp, timestamp, false);
        Path destinationRoot = documents.resolve("01_Main");

        Path destination = PathManager.getDestinationPath(info, destinationRoot);

        assertEquals(destinationRoot.resolve("nested").resolve("report.txt"), destination);
        assertFalse(destination.startsWith(sourceFolder));
    }
}
