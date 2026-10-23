package Classes;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FileInfo {
    //will store the file path, either the access & modification date whichever is most recent, is a directory
    private final Path path;
    private final FileTime lastActivity;
    private final boolean isDirectory;

    /**
     * Initialiser for FileInfo.
     * @param path The path of the file
     * @param lastAccessed The file time the file was last accessed
     * @param lastModified The file time the file was modified
     * @param directory If the path is a directory or not
     */
    public FileInfo(Path path, FileTime lastAccessed,
                    FileTime lastModified, boolean directory) {
        this.path = path;
        this.isDirectory = directory;
        if(lastAccessed.compareTo(lastModified) > 0)
        {
            this.lastActivity = lastAccessed;
        }
        else
        {
            this.lastActivity = lastModified;
        }
    }

    // === Getters === //
    public Path getPath(){
        return path;
    }
    public FileTime getLastActivity(){
        return lastActivity;
    }
    public boolean getIsDirectory(){
        return isDirectory;
    }

    //Returns the activity as a string
    public String getActivityString(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MM yyyy hh:mm");
        String activityString = lastActivity.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
        return activityString;
    }

    /**
     * Uses the activity date and dates from today to compare and return which parent folder the file should be moved to.
     * Creates instant data types to compare dates, one with minus 15 and one with minus 31.
     * @return the name of the destination path- 01_Main, 02_Archived or 03_DeleteCheck
     */
    public String getDestination(){
        Instant todayMinus15 = Instant.now().minus(15, ChronoUnit.DAYS);
        Instant todayMinus31 = Instant.now().minus(31, ChronoUnit.DAYS);
        if(this.getLastActivity().toInstant().isAfter(todayMinus15)) {
            return "01_Main";
        }
        else if(this.getLastActivity().toInstant().isBefore(todayMinus31)) {
            return "03_DeleteCheck";
        }
        else { //between 14 and 30 (inclusive)
            return "02_Archived";
        }
    }
}
