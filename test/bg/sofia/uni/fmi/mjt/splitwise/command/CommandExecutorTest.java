package bg.sofia.uni.fmi.mjt.splitwise.command;

import bg.sofia.uni.fmi.mjt.splitwise.manager.Splitwise;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandExecutorTest {
    CommandExecutor commandExecutor = new CommandExecutor(new Splitwise());

    public CommandExecutorTest() throws IOException {
    }

    @Test
    void testLoginValidation() {
        assertEquals("In order to login you need to provide your username and password",
                commandExecutor.execute(new Command("login", new String[]{"name"}), null),
                "Validation for the arguments of the <login> command is incorrect");
    }
    @Test
    void testAddFriendValidation() {
        assertEquals("In order to add a friend, you must provide only their username",
                commandExecutor.execute(new Command("add-friend", new String[]{"name","thing"}), null),
                "Validation for the arguments of the <add-friend> command is incorrect");
    }

    @Test
    void testSplitFriendValidation() {
        assertEquals("In order to split with a friend you must provide 3 arguments: amount, friend's name and reason",
                commandExecutor.execute(new Command("split", new String[]{"name","thing"}), null),
                "Validation for the arguments of the <split> command is incorrect");
    }
    @Test
    void testSplitFriendValidationAmount() {
        assertEquals("The first argument <amount> must be a number",
                commandExecutor.execute(new Command("split", new String[]{"five", "name","thing"}), null),
                "Validation for the <amount> argument of the <login> command is incorrect");
    }

    @Test
    void testSplitGroupValidation() {
        assertEquals("In order to split with a group, you must provide 3 arguments: amount, group name, reason",
                commandExecutor.execute(new Command("split-group", new String[]{"name","thing"}), null),
                "Validation for the arguments of the <split-group> command is incorrect");
    }

    @Test
    void testSplitGroupValidationAmount() {
        assertEquals("The first argument <amount> must be a number",
                commandExecutor.execute(new Command("split-group", new String[]{"five", "name","thing"}), null),
                "Validation for the <amount> argument of the <split-group> command is incorrect");
    }

    @Test
    void testPayedValidation() {
        assertEquals("You must provide 2 arguments: amount and friend's name",
                commandExecutor.execute(new Command("payed", new String[]{"name"}), null),
                "Validation for the arguments of the <payed> command is incorrect");
    }

    @Test
    void testPayedValidationAmount() {
        assertEquals("The first argument <amount> must be a number",
                commandExecutor.execute(new Command("payed", new String[]{"five", "name"}), null),
                "Validation for the <amount> argument of the <payed> command is incorrect");
    }

    @Test
    void testPayedGroupValidation() {
        assertEquals("You must provide 3 arguments: amount, group name and member name",
                commandExecutor.execute(new Command("payed-group", new String[]{"name"}), null),
                "Validation for the arguments of the <payed-group> command is incorrect");
    }

    @Test
    void testPayedGroupValidationAmount() {
        assertEquals("The first argument <amount> must be a number",
                commandExecutor.execute(new Command("payed-group", new String[]{"five", "group", "name"}), null),
                "Validation for the <amount> argument of the <payed-group> command is incorrect");
    }

    @Test
    void testCreateGroupValidation() {
        assertEquals("In order to create a group you must provide at least 3 arguments: group name and 2 member names",
                commandExecutor.execute(new Command("create-group", new String[]{"name"}), null),
                "Validation for the arguments of the <create-group> command is incorrect");
    }
}
