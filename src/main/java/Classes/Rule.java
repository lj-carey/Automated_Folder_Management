package Classes;

import java.nio.file.Path;

public class Rule {
    //rules need a name, min and max date (days int?) for activation, destination parent path
    private final String ruleName;
    private final Path parentDestination;
    private final int minDays;
    private final int maxDays;

    public Rule(String name, Path des, int minDay, int maxDay)
    {
        this.ruleName = name;
        this.parentDestination = des;
        this.minDays = minDay;
        this.maxDays = maxDay;
    }

   public String getRuleName() { return ruleName; }
    public Path getParentDestination() { return parentDestination; }
    public int getMinDays() { return minDays; }
    public int getMaxDays() { return maxDays; }
}
