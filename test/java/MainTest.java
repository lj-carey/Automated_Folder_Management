import Classes.Rule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MainTest {

    private final Path mainPath = Path.of("01_Main");
    private final Path archivedPath = Path.of("02_Archived");


    //Test to check that rules without overlapping ranges can be added to the list of rules successfully.
    @Test
    void acceptsRulesWithAdjacentNonOverlappingRanges() {
        List<Rule> rules = new ArrayList<>();

        Rule current = new Rule("Current", mainPath, 0, 13);
        Rule archived = new Rule("Archived", archivedPath, 14, 30);

        addRule(rules, current);
        addRule(rules, archived);

        assertEquals(2, rules.size());
        assertSame(current, rules.get(0));
        assertSame(archived, rules.get(1));
    }

    //Test to check that rules with overlapping ranges cannot be added to the list of rules,
    //and that an IllegalArgumentException is thrown.
    @Test
    void rejectsRangesThatOverlapAtTheirInclusiveBoundary() {
        List<Rule> rules = new ArrayList<>();

        addRule(rules, new Rule("Current", mainPath, 0, 13));

        assertThrows(IllegalArgumentException.class,
                () -> addRule(rules,
                        new Rule("Overlapping", archivedPath, 13, 30)));

        assertEquals(1, rules.size());
    }

    //Test to verify that rules with duplicate destination paths cannot be added to the list of rules,
    //even if their ranges do not overlap, and that an IllegalArgumentException is thrown.
    @Test
    void rejectsDuplicateDestinationsEvenWhenRangesDoNotOverlap() {
        List<Rule> rules = new ArrayList<>();

        addRule(rules, new Rule("Current", mainPath, 0, 13));

        assertThrows(IllegalArgumentException.class, () -> addRule(rules,
                        new Rule("Archived", mainPath, 14, 30)));

        assertEquals(1, rules.size());
    }

    //Test to verify that rules with duplicate destination paths cannot be added to the list of rules,
    //even if their ranges do overlap, and that the correct exceptions are thrown.
    @Test
    void rejectsDuplicateDestinationWhenRangesDoOverlap() {
        List<Rule> overlapRules = new ArrayList<>();
        overlapRules.add(new Rule("Current", mainPath, 0, 13));

        IllegalArgumentException overlapException = assertThrows(IllegalArgumentException.class,
                () -> addRule(overlapRules,
                        new Rule("Overlapping", archivedPath, 5, 12)));
        assertEquals("Rule overlaps with: Current", overlapException.getMessage());
        assertEquals(1, overlapRules.size());

        List<Rule> duplicateDestinationRules = new ArrayList<>();
        duplicateDestinationRules.add(new Rule("Current", mainPath, 0, 13));

        IllegalArgumentException destinationException = assertThrows(IllegalArgumentException.class,
                () -> addRule(duplicateDestinationRules,
                        new Rule("Archived", mainPath, 14, 30)));
        assertEquals("This parent destination is already in use: 01_Main", destinationException.getMessage());
        assertEquals(1, duplicateDestinationRules.size());
    }

    //Method to add a rule to the list of rules using reflection to access the private addRule method in the Main class.
    private static void addRule(List<Rule> rules, Rule rule) {
        try {
            //finds the addRule method in Main and sets accessibility to true to be able to use that method then invoke it
            Method method = Main.class.getDeclaredMethod("addRule", List.class, Rule.class);
            method.setAccessible(true);
            method.invoke(null, rules, rule);
        //catches exceptions and throws errors with messages and cause to help with debugging
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new AssertionError("Unexpected exception", cause);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not invoke Main.addRule", exception);
        }
    }
}
