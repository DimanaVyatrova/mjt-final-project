package bg.sofia.uni.fmi.mjt.splitwise.manager;

import bg.sofia.uni.fmi.mjt.splitwise.users.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitwiseTest {
    private static MoneyManager splitwise = new Splitwise();
    static User userOne = new User("test_user1", "pass1");
    static User userTwo = new User("test_user2", "pass2");
    static User userThree = new User("test_user3", "pass2");

    @BeforeAll
    static void setUp() {
        splitwise.register(userOne);
        splitwise.register(userTwo);
        splitwise.register(userThree);
        splitwise.addFriend("test_user2", userOne);
        splitwise.createGroup("group1", Set.of("test_user1", "test_user2", "test_user3"));
    }

    @Test
    void testRegisterInvalidArguments() {
        User user = new User(null, "pass");
        assertThrows(IllegalArgumentException.class, () -> splitwise.register(user),
                "Incorrect validation of username when registering.");
    }

    @Test
    void testRegisterUserAlreadyExists() {
        assertEquals("The username <test_user1> is taken. Please provide a different one.",
                splitwise.register(new User("test_user1", "pass")),
                "Two users can't have the same username.");
    }

    @Test
    void testRegisterUserSuccessful() {
        User user = new User("test", "pass");
        assertEquals("You registered successfully with username <test>", splitwise.register(user),
                "Users with unique usernames and valid password should be able to register successfully.");
    }

    @Test
    void testAddFriendDoesntExist() {
        assertEquals("There is no user with username <gichka>", splitwise.addFriend("gichka", userOne),
                "Users who aren't registered can't be added as friends.");
    }

    @Test
    void testAddFriendYourself() {
        assertEquals("You cannot add yourself as a friend", splitwise.addFriend("test_user1", userOne),
                "Users cannot add themselves as friends.");
    }

    @Test
    void testAddFriendSuccessful() {
        assertEquals("You and test_user1 are now friends!", splitwise.addFriend("test_user1", userTwo),
                "Two registered users should be able to become friends successfully.");
    }

    @Test
    void testCreateGroupUnregistered() {
        assertEquals("User test_user4 couldn't be added to the group," +
                        " because there is no such registered user" + System.lineSeparator() +
                        "all others were added successfully",
                splitwise.createGroup("test_group", Set.of("test_user1", "test_user2", "test_user3", "test_user4")),
                "Only registered users can be added to groups");
    }
    @Test
    void testCreateGroupSuccessful() {
        assertEquals("All users were added successfully to a group!",
                splitwise.createGroup("test_group", Set.of("test_user1", "test_user2", "test_user3")),
                "All registered users should be added successfully to a group.");
    }

    @Test
    void testSplitWithFriendSuccessful(){
        assertEquals("test_user2 owes you 20.0",
                splitwise.split(40, "test_user2", "reason", userOne),
                "Payment is not split correctly between friends.");
    }

    @Test
    void testSplitWithFriendNotFriends(){
        assertEquals("User <test_user3> is not on your friend list",
                splitwise.split(40, "test_user3", "reason", userOne),
                "Users can't split money with users who aren't their friends.");
    }

    @Test
    void testSplitInvalidAmount(){
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.split(-90, "name", "reason", userOne),
                "The amount to be split must be a positive number");
    }


    @Test
    void testSplitGroup(){
        String message = "group1:" + System.lineSeparator()
                + "test_user3 owes 30.0 LV to group group1" + System.lineSeparator()
                + "test_user2 owes 30.0 LV to group group1" + System.lineSeparator();
        assertEquals(message, splitwise.splitGroup(90, "group1", "reason", userOne),
                "Money aren't correctly split between group members.");
    }

    @Test
    void testSplitGroupInvalidAmount(){
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.splitGroup(-90, "name", "reason", userOne),
                "The amount to be split between groups must be a positive number.");
    }

    @Test
    void testSplitGroupNoSuchGroup(){
        assertEquals("Group with name <no_group> doesn't exist",
                splitwise.splitGroup(90, "no_group", "reason", userOne),
                "Users can't split money with groups that don't exist.");
    }

    @Test
    void testSplitGroupNotPartOfGroup(){
        User userEight = new User("test_user8", "pass8");
        splitwise.register(userEight);
        assertEquals("You are not a part of a group with name <group1>",
                splitwise.splitGroup(90, "group1", "reason", userEight),
                "Users can only split money with group they are a part of.");
    }

    @Test
    void testGetStatus() {
        User userFour = new User("test_user4", "pass4");
        splitwise.register(userFour);
        splitwise.addFriend("test_user3", userFour);
        splitwise.split(40, "test_user3", "reason", userFour);
        splitwise.createGroup("group2", Set.of("test_user1","test_user3", "test_user4"));
        splitwise.splitGroup(90, "group2", "reason", userFour);
        String message = "Friends:" + System.lineSeparator() + "* test_user3: Owes you 20.0 LV."
                + System.lineSeparator() + "Groups:" + System.lineSeparator()
                + "* Group group2: You owe 0.0" + System.lineSeparator()
                + "* Group group2: test_user3 owes 30.0 LV." + System.lineSeparator()
                + "* Group group2: test_user1 owes 30.0 LV." + System.lineSeparator();
        assertEquals(message, splitwise.getStatus(userFour),
                "Current status is incorrectly provided.");
    }

    @Test
    void testPayedNotFriend() {
        assertEquals("User with username <test_user3> is not on you friend list."
                ,splitwise.payed(50, "test_user3", userOne),
                "Users can approve the payments only of their friends.");
    }

    @Test
    void testPayedInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.payed(-90, "name", userOne),
                "The amount for a payment should be a positive number.");
    }

    @Test
    void testPayedInvalidUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.payed(90, "", userOne),
                "The username of the friend shouldn't be null, empty or blank.");
    }

    @Test
    void testPayedDoesntOwe() {
        User userFive = new User("test_user5", "pass5");
        splitwise.register(userFive);
        splitwise.addFriend("test_user5", userOne);
        assertEquals("test_user5 doesn't owe you anything",
                splitwise.payed(50, "test_user5", userOne),
                "Users can't approve the payments of users who don't owe them anything.");
    }

    @Test
    void testPayedSuccessful() {
        User userSix = new User("test_user6", "pass6");
        splitwise.register(userSix);
        splitwise.addFriend("test_user6", userOne);
        splitwise.split(50, "test_user6", "reason", userOne);
        assertEquals("test_user6 now owes you 5.0",
                splitwise.payed(20, "test_user6", userOne),
                "Payment approval should be successful.");
    }

    @Test
    void testPayedGroupSuccessful() {
        splitwise.createGroup("group2", Set.of("test_user1", "test_user2", "test_user3"));
        splitwise.splitGroup(90, "group2", "reason", userOne);
        assertEquals("test_user3 now owns 15.0 LV to group <group2>",
                splitwise.payedGroup(15, "group2", "test_user3", userOne),
                "Payment approval for a group should be successful.");
    }

    @Test
    void testPayedGroupUserOwesLess() {
        splitwise.createGroup("group3", Set.of("test_user1", "test_user2", "test_user3"));
        splitwise.splitGroup(90, "group3", "reason", userOne);
        assertEquals("test_user3 owns less than 1500.0",
                splitwise.payedGroup(1500, "group3", "test_user3", userOne),
                "Users can approve payments from their friends only for" +
                        " amounts equal or less than the currently owed.");
    }

    @Test
    void testPayedGroupInvalidAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.payedGroup(-1500, "group3", "test_user3", userOne),
                "The amount for a group payment should be a positive number.");
    }

    @Test
    void testPayedGroupInvalidGroupName() {
        assertThrows(IllegalArgumentException.class,
                () -> splitwise.payedGroup(1500, "  ", "test_user3", userOne),
                "The group name shouldn't be null, empty or blank when approving payment.");
    }

    @Test
    void testPayedGroupUserDoesntOweAnything() {
        splitwise.createGroup("group4", Set.of("test_user1", "test_user2", "test_user3"));
        splitwise.splitGroup(90, "group4", "reason", userOne);
        assertEquals("The user <test_user1> doesn't owe anything to the group <group4>",
                splitwise.payedGroup(15, "group4", "test_user1", userTwo),
                "Payments can be approved only for users who owe less  or equal to the specified amount.");
    }

    @Test
    void testPayedGroupNoSuchGroup() {
        assertEquals("There is no group named <no_such>",
                splitwise.payedGroup(15, "no_such", "test_user1", userTwo),
                "Payments for groups can only be approved for groups that exist.");
    }

    @Test
    void testPayedGroupNoSuchRegisteredUser() {
        assertEquals("There is no registered user with username <no_such_user>",
                splitwise.payedGroup(15, "group1", "no_such_user", userTwo),
                "Payments for groups can only be approved for users that are registered.");
    }

    @Test
    void testPayedGroupNoSuchUser() {
        User userSeven = new User("test_user7", "pass7");
        splitwise.register(userSeven);
        assertEquals("The user <test_user7> is not a part of the group <group1>",
                splitwise.payedGroup(15, "group1", "test_user7", userTwo),
                "Payments for groups can only be approved for users that are part of the group.");
    }

    @Test
    void testGetPaymentHistory() {
        User userTen = new User("test_user10", "pass10");
        splitwise.register(userTen);
        splitwise.addFriend("test_user10", userOne);
        splitwise.split(40, "test_user1", "reason", userTen);
        assertEquals("Receiver:test_user1 amount:20.0" + System.lineSeparator(),
                splitwise.getPaymentHistory(userTen),
                "Incorrectly displaying payment history.");
    }

    @Test
    void testLoginUnsuccessful() {
        User userNine = new User("test_user9", "pass9");
        assertFalse(splitwise.login(userNine), "Only registered users can log into the system.");
    }

    @Test
    void testLoginSuccessful() {
        assertTrue(splitwise.login(userOne), "Registered users must be able to login successfully.");
    }

    @AfterAll
    static void cleanUp() {
        for (int i = 1; i <= 10; i++) {
            File file = new File("test_user" + i + ".txt");
            file.delete();
        }
        File file = new File("test.txt");
        file.delete();
    }
}