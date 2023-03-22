package bg.sofia.uni.fmi.mjt.splitwise.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandCreatorTest {
    @Test
    void testNewCommand() {
        String[] args = {"mimi"};
        Command cmd = new Command("add-friend", args);
        assertEquals(cmd, CommandCreator.newCommand("add-friend mimi"),
                "New commands are not assembled correctly.");
    }
}
