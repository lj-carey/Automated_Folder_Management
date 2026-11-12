package Classes;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FileInfo {
    //will store the file path, either the access & modification date whichever is most recent, is a directory
    private final Path path;
    private final FileTime lastAccessed;
    private final FileTime lastModified;
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
        this.lastAccessed = lastAccessed;
        this.lastModified = lastModified;
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
    public FileTime getLastAccessed(){
        return lastAccessed;
    }
    public FileTime getLastModified(){
        return lastModified;
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

    //Get the activity as days from today (long)
    public long getDaysSinceActivity(){
        LocalDate today = LocalDate.now(); //today's date
        LocalDate activityDate = lastActivity.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
        //Get the difference using ChronoUnit DAYS
        long activityInt = ChronoUnit.DAYS.between(activityDate, today);
        return activityInt;
    }

}
