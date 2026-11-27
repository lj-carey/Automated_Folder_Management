package Classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

//needs import for PathManager to scan

public class Logs {
    //needs to include the logs
    //Run details (overview), errors, Folder Manager (what was moved)

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
    private static final Instant today = Instant.now();

    /**
     * Returns the path to the log directory within the user's Documents folder.
     *
     * @return Path to the log directory.
     */
    public static Path getLogPath(){
        return PathManager.getDocumentsPath().resolve("00_logs");
    }

    /**
     * Writes an overview log file containing information about the root paths and items scanned.
     *
     * @throws Exception if an error occurs while generating the overview log.
     */
    public static void writeToOverview() throws Exception {
        String overview = overviewLog();
        String logName = today.atZone(ZoneId.systemDefault()).format(formatter) + " Overview.log";
        Path overviewPath = getLogPath().resolve(logName);
        writeToLog(overviewPath, overview);
    }

    /**
     * Writes a log entry for a folder movement operation.
     *
     * @param info The information about the folder being moved.
     * @param destination The destination path where the folder is moved.
     * @throws IOException if an error occurs while writing to the log file.
     */
    public static void writeFolderMovement(FileInfo info, Path destination) throws IOException {
        String details = FolderMoveDetails(info, destination);
        String logName = today.atZone(ZoneId.systemDefault()).format(formatter) + " Movement.log";
        Path movementPath = getLogPath().resolve(logName);
        writeToLog(movementPath, details);
    }

    /**
     * Writes a log entry for a path existence check.
     *
     * @param destination The path to check.
     * @throws IOException if an error occurs while writing to the log file.
     */
    public static void writeToPathExists(Path destination) throws IOException {
        String details = pathExists(destination);
        writeToLog(getErrorPath(), details);
    }

    /**
     * Writes a log entry for an error that occurs during execution.
     *
     * @param e The exception that occurred.
     * @throws IOException if an error occurs while writing to the log file.
     */
    public static void writeToError(Exception e) throws IOException {
        writeToLog(getErrorPath(), e.getMessage());
    }

    /**
     * Writes a log entry to the specified path.
     *
     * @param path The path to the log file.
     * @param log The log message to write.
     * @throws IOException if an error occurs while writing to the log file.
     */
    private static void writeToLog(Path path, String log) throws IOException {
        //create the parent path in case it doesn't exist
        Files.createDirectories(path.getParent());

        Files.writeString(path, log, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * Generates a log entry for folder movement details.
     *
     * @param info The information about the folder being moved.
     * @param destination The destination path where the folder is moved.
     * @return A string containing the folder movement details.
     */
    private static String FolderMoveDetails(FileInfo info, Path destination) {
        return "Folder: " + info.getPath().toString() + " | Directory: " + info.getIsDirectory()
                + " | Moved to: " + destination
                + " | Last accessed/modified: " + info.getActivityString() + "\n";
    }

    /**
     * Generates an overview log entry containing information about the root paths and items scanned.
     *
     * @return A string containing the overview log entry.
     * @throws Exception if an error occurs while generating the overview log.
     */
    private static String overviewLog() throws Exception {
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatterTime);

        List<Path> rootPaths = PathManager.getPathsList();
        String rootPathsLog = "Root paths: "+ rootPaths.size() + "\n";
        for (Path path : rootPaths){
            rootPathsLog +=  path + "\n";
        }
        rootPathsLog += "(Check ErrorWithPaths.log to see if any paths were skipped)\n\n";
        List<Path> paths = PathManager.getPathsToMove(rootPaths, 3);
        //get total items scanned and how many Folders and Files
        int totalItems = paths.size();
        int folderCount = 0;
        int fileCount = 0;
        for(Path item : paths){
            if(Files.isDirectory(item)){
                folderCount += 1;
            } else if(Files.isRegularFile(item)){
                fileCount += 1;
            }
        }
        String itemsLog = "Total items: " + totalItems + "\nFolders: " + folderCount
                + "  |  Files: " + fileCount;

        return "Overview @ " + timestamp + "\n" + rootPathsLog + itemsLog + "\n\n";
    }

    /**
     * Generates a log entry for a path existence check.
     *
     * @param destination The path to check.
     * @return A string containing the path existence log entry.
     */
    private static String pathExists(Path destination) {
    return "The destination path: " + destination + "  Already exists - Unable to move item.";
    }

    /**
     * Returns the path to the error log file for the current date.
     *
     * @return Path to the error log file.
     */
    private static Path getErrorPath() {
        return getLogPath().resolve(today.atZone(ZoneId.systemDefault()).format(formatter) + " Errors.log");
    }

}
