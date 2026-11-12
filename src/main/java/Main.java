import Classes.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
//Classes, Functions, Rules
    //Classes: FileInfo, Rule
    //Functions: GetPath(), GetPaths(), ScanFolders(), addRule()
    //Rules (defined in Main): <14 Main, 14-30 Archived, >=31 DeleteCheck, only checks 01_Main, 02_Archived, 03_DeleteCheck.

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

        //Rules
        List<Rule> RuleEngine = new ArrayList<>();
        addRule(RuleEngine, new Rule("Current", userPath.resolve("01_Main"), 0, 13));
        addRule(RuleEngine, new Rule("Archived", userPath.resolve("02_Archived"), 14, 30));
        addRule(RuleEngine, new Rule("Older Items", userPath.resolve("03_DeleteCheck"), 31, 999));


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
                Path errorPath = userPath.resolve("00_Logs").resolve("ErrorWithPath.txt");
                String errorString = "Skipped file path: " + p + "\n";
                Files.writeString(errorPath, errorString, StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND);
            }
        }
        //for each file move if necessary and log
        for (FileInfo info : fileInfoList)
        {
            Path des = null;
            //get active rule
            for (Rule rule : RuleEngine)
            {
                if(info.getDaysSinceActivity() >= rule.getMinDays() &&
                        info.getDaysSinceActivity() <= rule.getMaxDays())
                {
                    des = rule.getParentDestination();
                }
            }
            //check if folder needs moving
            if(des == null)
            {
                throw new IllegalArgumentException(
                        "No active rule."
                );
            }
            else if(!info.getPath().getParent().equals(des))
            {
                //make path for destination
                Path fname = info.getPath().getFileName();
                Path destination = des.resolve(fname);
                //move path and set lastModifiedTime and lastAccessed
                Files.move(info.getPath(), destination);
                Files.setLastModifiedTime(destination, info.getLastModified());
                Files.setAttribute(destination, "basic:lastAccessTime", info.getLastAccessed());
                //log details
                String log = "Folder: " + info.getPath().toString() + " | Directory: " + info.getIsDirectory()
                                + " | Moved to: " + destination.toString()
                                + " | Last accessed/modified: " + info.getActivityString() + "\n";
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
                //get parent folders to check - changed to equals caused no paths to be added
                if(name.contains("01_main") ||  name.contains("02_archived") || name.contains("03_deletecheck"))
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

    /**
     * Checks if the new rule has an overlap with the activity period (MinDays and MaxDays) or if the destination already exists.
     * If so an illegal argument error is thrown. If not then the rule is added.
     * @param rules The list of existing rules
     * @param newRule The new rule tovalidate and add
     */
    public static void addRule(List<Rule> rules, Rule newRule){
        //Checks if rules have no overlap in min and max rules and destination is unique then add the rule
        for (Rule existingRule : rules) {
            if (newRule.getMinDays() <= existingRule.getMaxDays() &&
                    newRule.getMaxDays() >= existingRule.getMinDays()) {

                throw new IllegalArgumentException(
                        "Rule overlaps with: " + existingRule.getRuleName()
                );
            }
            if (newRule.getParentDestination().equals(existingRule.getParentDestination()))
            {
                throw new IllegalArgumentException(
                        "This parent destination is already in use: " + existingRule.getParentDestination()
                );
            }
        }
        rules.add(newRule);
    }

}
