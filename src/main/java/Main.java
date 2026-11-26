import Classes.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Main {
//Classes, Functions, Rules
    //Classes: FileInfo, Heartbeat, Logs, PathManager, Rule
    //Functions: addRule()
    //Rules (defined in Main): <14 Main, 14-30 Archived, 31-999 DeleteCheck

    public static void main(String[] args) {
        //depth
        int depth = 3;

        //get path function
        Path userPath = PathManager.getDocumentsPath();
        //initialise file info
        List<FileInfo> fileInfoList = new ArrayList<>();

        //start heartbeat here
        Path heartbeatPath = userPath.resolve("00_Logs").resolve("Heartbeat.log");
        Heartbeat heartbeat = new Heartbeat(heartbeatPath, 5);
        try {
            heartbeat.start();

            //call getPaths to get the paths to check
            List<Path> mainPaths = PathManager.getPathsList();
            //get the folders in the main paths to check and move, includes depth to search
            List<Path> paths = PathManager.getPathsToMove(mainPaths, depth);

            //Rules
            List<Rule> ruleEngine = new ArrayList<>();
            addRule(ruleEngine, new Rule("Current", userPath.resolve("01_Main"), 0, 13));
            addRule(ruleEngine, new Rule("Archived", userPath.resolve("02_Archived"), 14, 30));
            addRule(ruleEngine, new Rule("Older Items", userPath.resolve("03_DeleteCheck"), 31, 999));


            //for each path get the fileInfo and add to list
            for (Path p : paths) {
                try {
                    BasicFileAttributes f = Files.readAttributes(p, BasicFileAttributes.class);
                    FileInfo info = new FileInfo(p, f.lastAccessTime(), f.lastModifiedTime(), f.isDirectory());
                    fileInfoList.add(info);
                }
                //if errors log it
                catch (IOException e) {
                    Logs.writeToError(e);
                }
            }
            moveItems(ruleEngine, fileInfoList);
            //overview log
            Logs.writeToOverview();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            heartbeat.stop();
        }
    }

    // === METHODS === //

    /**
     * Moves the items in the fileInfoList to their respective destinations based on the rules defined in the ruleEngine.
     * It gets the active rule for each item based on its days since last activity and moves it to the corresponding destination.
     * Writes to the log files for any errors or movements made.
     * When an item is moved, it also sets the last modified and last accessed times to match the original item.
     *
     * @param ruleEngine The list of rules that determine the destination for each item based on its days since last activity.
     * @param fileInfoList The list of FileInfo objects representing the items to be moved.
     * @throws IOException If an I/O error occurs during the moving of items or writing to log files.
     */
    private static void moveItems(List<Rule> ruleEngine, List<FileInfo> fileInfoList) throws IOException {
        //for each file move if necessary and log
        for (FileInfo info : fileInfoList) {
            Path des = null;
            //get active rule
            for (Rule rule : ruleEngine) {
                if (info.getDaysSinceActivity() >= rule.getMinDays() &&
                        info.getDaysSinceActivity() <= rule.getMaxDays()) {
                    des = rule.getParentDestination();
                }
            }
            //check if folder needs moving
            if (des == null) {
                throw new IllegalArgumentException("No active rule.");
            }
            //check current path doesn't start with des
            else if (!info.getPath().startsWith(des)) {
                //make path for destination
                Path destination = PathManager.getDestinationPath(info, des);
                //check if destination already exists just in case
                if (Files.exists(destination)) {
                    //log it in a new log file
                    Logs.writeToPathExists(destination);
                    //continue after log so other paths checked
                    continue;
                }
                try {
                    //create any missing directories
                    Files.createDirectories(destination.getParent());
                    //move path and set lastModifiedTime and lastAccessed
                    Files.move(info.getPath(), destination);
                    Files.setLastModifiedTime(destination, info.getLastModified());
                    Files.setAttribute(destination, "basic:lastAccessTime", info.getLastAccessed());
                    //Folder moved details
                    Logs.writeFolderMovement(info, destination);
                } catch (IOException e) {
                    Logs.writeToError(e);
                }
            }
        }
    }

    /**
     * Checks if the new rule has an overlap with the activity period (MinDays and MaxDays) or if the destination already exists.
     * If so an illegal argument error is thrown. If not then the rule is added.
     *
     * @param rules   The list of existing rules
     * @param newRule The new rule to validate and add
     */
    private static void addRule(List<Rule> rules, Rule newRule) {
        //Checks if rules have no overlap in min and max rules and destination is unique then add the rule
        for (Rule existingRule : rules) {
            if (newRule.getMinDays() <= existingRule.getMaxDays() &&
                    newRule.getMaxDays() >= existingRule.getMinDays()) {

                throw new IllegalArgumentException(
                        "Rule overlaps with: " + existingRule.getRuleName()
                );
            }
            if (newRule.getParentDestination().equals(existingRule.getParentDestination())) {
                throw new IllegalArgumentException(
                        "This parent destination is already in use: " + existingRule.getParentDestination()
                );
            }
        }
        rules.add(newRule);
    }

}