package bg.sofia.uni.fmi.mjt.splitwise.memory;

import bg.sofia.uni.fmi.mjt.splitwise.users.Group;
import bg.sofia.uni.fmi.mjt.splitwise.users.User;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class MemoryHandler {
    private static Map<String, User> registeredUsers = new HashMap<>();
    private static Map<String, Group> groups = new HashMap<>();
    private static final String FRIEND = "friend";
    private static final String GROUP = "group";
    private static final String RECEIVER = "Receiver";
    private static final String REGEX = ":";
    private static final String TXT = ".txt";
    private static final int THREE = 3;

    public MemoryHandler() {
    }

    public static void fileExists(File file) {
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException();
                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    public Map<String, User> loadRegisteredUsers(String memoryFileName) throws IOException {
        try (Reader reader = new FileReader(memoryFileName);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(REGEX);
                String username = data[0];
                String password = data[1];
                User user = this.restoreUser(username, password);
                registeredUsers.put(username, user);
            }
            return registeredUsers;
        }
    }

    public Map<String, Group> loadGroups() {
        return groups;
    }

    public void addNewUserToDatabase(String username, String password) {
        try (Writer writer = new FileWriter("memory.txt", true)) {
            writer.write(username + REGEX + password + System.lineSeparator());
            File file = new File(username + TXT);
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void rememberFriend(String username, String friendName) {
        try (Writer writer = new FileWriter(username + TXT, true)) {
            writer.write(FRIEND + REGEX + friendName + ":0:0");
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void rememberGroup(String username, String groupName) {
        try (Writer writer = new FileWriter(username + TXT, true)) {
            writer.write(GROUP + REGEX + groupName + ":0" + System.lineSeparator());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void rememberPaymentHistory(String username, String info) {
        try (Writer writer = new FileWriter(username + TXT, true)) {
            writer.write(info + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("There was a problem with recording payment history");
            throw new RuntimeException();
        }
    }

    public User restoreUser(String username, String password) throws IOException {
        User user = new User(username, password);
        try (Reader reader = new FileReader(username + TXT);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(REGEX);
                switch (data[0]) {
                    case FRIEND -> {
                        user.getFriends().add(data[1]);
                        if (Double.parseDouble(data[2]) != 0) {
                            user.getFriendCreditors().put(data[1], Double.parseDouble(data[2]));
                        }
                        if (Double.parseDouble(data[THREE]) != 0) {
                            user.getFriendDebtors().put(data[1], Double.parseDouble(data[THREE]));
                        }
                    }
                    case GROUP -> {
                        if (!groups.containsKey(data[1])) {
                            Group group = new Group(data[1]);
                            group.addMember(username, Double.parseDouble(data[2]));
                            groups.put(data[1], group);
                        } else {
                            groups.get(data[1]).addMember(username, Double.parseDouble(data[2]));
                        }
                        user.getGroups().put(data[1], Double.parseDouble(data[2]));
                    }
                    case RECEIVER -> user.getPaymentHistory().add(line);
                }
            }
            return user;
        }
    }

    public void updateUserGroupDebt(String username, String groupName, double debt) {
        StringBuilder contents = new StringBuilder();
        try (Reader reader = new FileReader(username + TXT);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(REGEX);
                if (data[0].equals(GROUP) && data[1].equals(groupName)) {
                    line = GROUP + REGEX + groupName + REGEX + debt;
                }
                contents.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }

        try (Writer writer = new FileWriter(username + TXT)) {
            writer.write(contents.toString());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private void readAndWrite(String username, String friendName,
                              double currentlyOwedFriend, double currentlyOwedCaller) {
        StringBuilder contents = new StringBuilder();
        try (Reader reader = new FileReader(username + TXT);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(REGEX);
                if (data[0].equals(FRIEND) && data[1].equals(friendName)) {
                    line = FRIEND + REGEX + friendName + REGEX + currentlyOwedCaller + REGEX + currentlyOwedFriend;
                }
                contents.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }

        try (Writer writer = new FileWriter(username + TXT)) {
            writer.write(contents.toString());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public void updateUserSplitFriend(String username, String friendName,
                                      double currentlyOwedFriend, double currentlyOwedCaller) {
        readAndWrite(username, friendName, currentlyOwedFriend, currentlyOwedCaller);
        readAndWrite(friendName, username, currentlyOwedCaller, currentlyOwedFriend);
    }
}
