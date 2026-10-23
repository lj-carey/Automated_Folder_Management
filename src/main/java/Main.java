import Classes.FileInfo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
//Functions and Rules
    //Functions: GetPath(), GetPaths(), ScanFolders()
    //Rules (via FileInfo): <14 Main, 14-29 Archived, >=30 DeleteCheck, only checks 01_Main, 02_Archived, 03_DeleteCheck.

    public static void main(String[] args) throws IOException
    {
        //get path function
        Path userPath = getPath();
        //initialise file info
        List<FileInfo> fileInfoList = new ArrayList<>();
        //call getPaths to get the paths to check
        List<Path> mainPaths = getPaths(userPath);
        //get the folders in the main paths to check and move
        List<Path> paths = scanFolders(mainPaths);

        //for each path get the fileInfo and add to list
        for(Path p: paths)
        {
            try
            {
                BasicFileAttributes f = Files.readAttributes(p, BasicFileAttributes.class);
                FileInfo info = new FileInfo(p, f.lastAccessTime(), f.lastModifiedTime(), f.isDirectory());
                fileInfoList.add(info);
            }
            catch (IOException e) //if errors log it
            {
                Path errorPath = getPath().resolve("00_Logs").resolve("ErrorWithPath.txt");
                String errorString = "Skipped file path: " + p + "\n";
                Files.writeString(errorPath, errorString, StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND);
            }
        }
        //for each file getDestination, if destination not in current path, move folder to destination and log
        for (FileInfo info : fileInfoList)
        {
            String des = info.getDestination();
            if(!info.getPath().toString().contains(des)) //if not in right folder
            {
                //make path for destination
                Path mainFolder = userPath.resolve(des);
                Path fName = info.getPath().getFileName();
                Path destination = mainFolder.resolve(fName);
                //move path
                Files.move(info.getPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                //log details
                String log = info.getPath().toString() + "moved to " + destination.toString()
                                + " Was last accessed: " + info.getActivityString() + "\n";
                //make a file with today's date as part of path then append the log for the move
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy");
                Instant today = Instant.now();
                String logString = today.atZone(ZoneId.systemDefault()).format(formatter) + " FolderManager.txt";
                Path logPath = userPath.resolve("00_Logs").resolve(logString);
                Files.writeString(logPath, log, StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND);
            }
        }
    }

    // === METHODS === //
    /**
     * Get the user's Documents path.
     * @return Path for user
     */
    public static Path getPath()
    {
        return Path.of(System.getProperty("user.home")).resolve("Documents");
    }

    /**
     * Get the parent folders to search through - main, archived, deletecheck.
     * Uses toLowerCase and trim for naming.
     * @param userPath
     * @return List of parent folders to search through
     * @throws IOException
     */
    public static List<Path> getPaths(Path userPath) throws IOException
    {
        List<Path> filePaths = new ArrayList<>();
        try (var paths = Files.list(userPath))
        {

            for (Path p : paths.toList()){
                String name = p.toString().toLowerCase().trim();
                //get parent folders to check
                if(name.contains("main") ||  name.contains("archived") || name.contains("deletecheck"))
                {
                    filePaths.add(p);
                }
            }
        }
        return filePaths;
    }

    /**
     * Get the paths of the children of the parent folders - used in conjunction with getPaths().
     * @param Folders The list of folders from getPaths()
     * @return A list of the children of each path in the param
     * @throws IOException
     */
    public static List<Path> scanFolders(List<Path> Folders) throws IOException
    {
        List<Path> filePaths = new ArrayList<>();
        for (Path mainDir : Folders) //for each parent path
        {
            try (var paths = Files.list(mainDir)) //try to get contents and add to filePaths
            {
                for (Path f : paths.toList()){
                    filePaths.add(f);
                }
            }
        }
        return filePaths;
    }

}
