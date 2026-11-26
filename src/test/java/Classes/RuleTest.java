
package Classes;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RuleTest {
    //Test to verify that the Rule constructor correctly stores the provided properties
    //and that the getter methods return the expected values.
    @Test
    void constructorStoresRuleProperties() {
        //Arrange
        String name = "Archived";
        Path destination = Path.of("Documents", "02_Archived");
        int minDays = 14;
        int maxDays = 30;

        //Act
        Rule rule = new Rule(name, destination, minDays, maxDays);

        //Assert
        assertEquals(name, rule.getRuleName());
        assertEquals(destination, rule.getParentDestination());
        assertEquals(minDays, rule.getMinDays());
        assertEquals(maxDays, rule.getMaxDays());
    }

    //Test to check rules can have a single day range, where minDays and maxDays are both set to the same value.
    @Test
    void ruleCanHaveSingleDayRange() {
        Rule rule = new Rule(
                "Today",
                Path.of("01_Main"),
                0,
                0
        );
        assertEquals(0, rule.getMinDays());
        assertEquals(0, rule.getMaxDays());
    }
}