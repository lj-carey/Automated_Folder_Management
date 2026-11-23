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

    private static Path logPath = getLogPath();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");
    private static final Instant today = Instant.now();
    private static final Path errorPath = logPath.
            resolve(today.atZone(ZoneId.systemDefault()).format(formatter) + " Errors.log");

    public static Path getLogPath(){
        return PathManager.getDocumentsPath().resolve("00_logs");
    }

    public static void writeToOverview() throws Exception {
        String overview = overviewLog();
        String logName = today.atZone(ZoneId.systemDefault()).format(formatter) + " Overview.log";
        logPath = logPath.resolve(logName);
        writeToLog(logPath, overview);
    }

    public static void writeFolderMovement(FileInfo info, Path destination) throws IOException {
        String details = FolderMoveDetails(info, destination);
        String logName = today.atZone(ZoneId.systemDefault()).format(formatter) + " Movement.log";
        logPath = logPath.resolve(logName);
        writeToLog(logPath, details);
    }

    public static void writeToPathExists(Path destination) throws IOException {
        String details = pathExists(destination);
        writeToLog(errorPath, details);
    }

    public static void writeToError(Exception e) throws IOException {
        writeToLog(errorPath, e.getMessage());
    }

    private static void writeToLog(Path path, String log) throws IOException {
        Files.writeString(path, log, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static String FolderMoveDetails(FileInfo info, Path destination) {
        return "Folder: " + info.getPath().toString() + " | Directory: " + info.getIsDirectory()
                + " | Moved to: " + destination
                + " | Last accessed/modified: " + info.getActivityString() + "\n";
    }

    private static String overviewLog() throws Exception {
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatterTime);

        List<Path> rootPaths = PathManager.getPathsList();
        String rootPathsLog = "Root paths: "+ rootPaths.size() + "\n";
        for (Path path : rootPaths){
            rootPathsLog +=  path + "\n";
        }
        rootPathsLog += "(Root paths includes skipped paths)\n\n";
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

    private static String pathExists(Path destination) {
    return "The destination path: " + destination + "  Already exists - Unable to move item.";
    }

}
