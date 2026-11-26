package Classes;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class PathManager {
//needs getPath, getPaths, scanPaths

    public static List<Path> getPathsList() throws Exception {
        return getPaths();
    }
    public static Path getDestinationPath(FileInfo info, Path des){
        return createDestinationPath(info, des);
    }
    public static List<Path> getPathsToMove(List<Path> Folders, int depth) throws IOException {
        return scanPaths(Folders, depth);
    }
    /**
     * Get the user's Documents path.
     *
     * @return Path for user
     */
    public static Path getDocumentsPath() {
        return Path.of(System.getProperty("user.home")).resolve("Documents");
    }

    // ==== Private Methods ==== //
    /**
     * Get the parent folders to search through - only includes directories and
     * excludes directories containing the words "logs" and "locked".
     * Uses toLowerCase and trim for naming.
     *
     * @return List of parent folders to search through
     * @throws Exception error exception thrown and logged
     */
    private static List<Path> getPaths() throws Exception {
        List<Path> filePaths = new ArrayList<>();
        Path userPath = getDocumentsPath();
        try (var paths = Files.list(userPath)) {

            for (Path p : paths.toList()) {
                if (Files.isDirectory(p)) { //will not add .ini files
                    String name = p.toString().toLowerCase().trim();
                    //get parent folders to check
                    if (!name.contains("locked") && !name.contains("logs")) {
                        filePaths.add(p);
                    }
                }
            }
        } catch (Exception e) {
            Logs.writeToError(e);
            throw new Exception(e);
        }
        return filePaths;
    }

    /**
     * Gets the destination path and constructs it.
     *
     * @param info     The FileInfo data of the item
     * @param des      The destination path from the rule
     * @return The path to move the item to
     */
    private static Path createDestinationPath(FileInfo info, Path des) {
        Path userPath = getDocumentsPath();
        Path parentPath = userPath.resolve(des);
        Path relativePath = userPath.relativize(info.getPath());
        //ensures that the relative path does not include the old parent folder if move necessary
        int pathLength = relativePath.getNameCount(); //get the path length
        if(pathLength > 1){
            relativePath = relativePath.subpath(1, pathLength);
            return parentPath.resolve(relativePath);
        }
        else{
            return parentPath;
        }
    }

    /**
     * Recursive method to get the paths of the children of the parent folders - used in conjunction with getPaths().
     *
     * @param Folders The list of folders from getPaths()
     * @param depth   The depth of the paths to check
     * @return A list of the children of each path in the param
     * @throws IOException
     */
    private static List<Path> scanPaths(List<Path> Folders, int depth) throws IOException {
        //initialise allPaths, logDir & errorPath and create/clear previous scan log
        List<Path> allPaths = new ArrayList<>();
        Path logDir = PathManager.getDocumentsPath().resolve("00_Logs");
        Files.createDirectories(logDir);
        Path errorPath = logDir.resolve("ErrorWithPath.log");
        Files.writeString(errorPath, "");

        if (depth == 0) //base case
        {
            return allPaths;
        } else {
            for (Path mainDir : Folders) //for each parent path
            {
                try (var paths = Files.list(mainDir)) //try to get contents and add to filePaths
                {
                    for (Path f : paths.toList()) {
                        if (f.toString().toLowerCase().contains("locked") || f.toString().toLowerCase().contains("logs")
                                || f.getFileName().toString().toLowerCase().endsWith(".ini")) { //skip paths with locked, logs or .ini in file path
                            continue;
                        }
                        if (depth == 1 || Files.isRegularFile(f)) {
                            allPaths.add(f);
                        }
                        if (depth > 1 && Files.isDirectory(f)) {
                            List<Path> subFolders = List.of(f);
                            allPaths.addAll(scanPaths(subFolders, depth - 1));
                        }
                    }
                } catch (AccessDeniedException e) {
                    String errorString = "Skipped folder path: " + mainDir
                            + " | Due to access denied. \n";
                    Files.writeString(errorPath, errorString,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
                }
            }
        }
        return allPaths;
    }
}
