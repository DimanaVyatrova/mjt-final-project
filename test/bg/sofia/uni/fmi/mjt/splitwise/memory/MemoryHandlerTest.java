package bg.sofia.uni.fmi.mjt.splitwise.memory;

import bg.sofia.uni.fmi.mjt.splitwise.users.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryHandlerTest {
    MemoryHandler memoryHandler = new MemoryHandler();
    @BeforeAll
    static void setUp() throws IOException {
        File file = new File("memory_test.txt");
        file.createNewFile();
        Writer writer = new FileWriter("memory_test.txt");
        writer.write("user1:pass1" + System.lineSeparator() + "user2:pass2" + System.lineSeparator());
        writer.close();
        File fileOne = new File("user1.txt");
        File fileTwo = new File("user2.txt");
        fileOne.createNewFile();
        fileTwo.createNewFile();
        Writer writerOne = new FileWriter("user1.txt");
        writerOne.write("friend:user2:0.0:0.0" + System.lineSeparator() +
                "group:group1:0.0" + System.lineSeparator());
        writerOne.close();
        Writer writerTwo = new FileWriter("user2.txt");
        writerTwo.write("friend:user1:0.0:0.0" + System.lineSeparator() +
                "group:group1:0.0" + System.lineSeparator());
        writerTwo.close();
    }
    @Test
    void testLoadUsers() throws IOException {
        Map<String, User> registeredUsers = new HashMap<>();
        registeredUsers.put("user1", new User("user1", "pass1"));
        registeredUsers.put("user2", new User("user2", "pass2"));
        assertEquals(registeredUsers, memoryHandler.loadRegisteredUsers("memory_test.txt"),
                "Previously registered users must be restored by the system.");
    }

    @Test
    void testLoadUsersFileNotExist() {
        assertThrows(IOException.class, () -> memoryHandler.loadRegisteredUsers("no_such_file.txt"),
                "Can't restore users from a file that doesn't exist.");
    }

    @Test
    void testRestoreUser() {
        assertThrows(IOException.class, () -> memoryHandler.restoreUser("alabala","pass"),
                "Unregistered users can't be restored by the system.");
    }

    @AfterAll
    static void cleanUp() {
        File file = new File("memory_test.txt");
        file.delete();
        File fileOne = new File("user1.txt");
        File fileTwo = new File("user2.txt");
        fileOne.delete();
        fileTwo.delete();
    }

}
