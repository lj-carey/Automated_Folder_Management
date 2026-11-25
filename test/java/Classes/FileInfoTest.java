package Classes;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileInfoTest {

    //Test to verify that the FileInfo constructor correctly stores the provided properties
    //and that the getter methods return the expected values.
    @Test
    void usesMostRecentAccessOrModificationTimeAsActivity() {
        FileTime accessed = fileTime(LocalDate.now().minusDays(2));
        FileTime modified = fileTime(LocalDate.now().minusDays(8));

        FileInfo info = new FileInfo(Path.of("document.txt"), accessed, modified, false);

        assertEquals(accessed, info.getLastActivity());
        assertEquals(accessed, info.getLastAccessed());
        assertEquals(modified, info.getLastModified());
        assertFalse(info.getIsDirectory());
    }

    //Test to verify that the FileInfo constructor correctly calculates the number of days since the last activity
    @Test
    void calculatesDaysSinceActivityUsingTheMostRecentTimestamp() {
        FileTime accessed = fileTime(LocalDate.now().minusDays(12));
        FileTime modified = fileTime(LocalDate.now().minusDays(5));

        FileInfo info = new FileInfo(Path.of("archive"), accessed, modified, true);

        assertEquals(modified, info.getLastActivity());
        assertEquals(5, info.getDaysSinceActivity());
        assertTrue(info.getIsDirectory());
    }

    //Helper method to convert a LocalDate to a FileTime object
    private static FileTime fileTime(LocalDate date) {
        return FileTime.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
