package bg.sofia.uni.fmi.mjt.splitwise.manager;

import bg.sofia.uni.fmi.mjt.splitwise.users.Group;
import bg.sofia.uni.fmi.mjt.splitwise.users.User;

import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.Set;

public interface MoneyManager {
    String register(User user);
    boolean login(User user);
    String addFriend(String friendName, User caller);
    String createGroup(String groupName, Set<String> members);
    String split(double amount, String name, String reason, User caller);
    String splitGroup(double amount, String groupName, String reason, User caller);
    String payed(double amount, String username, User caller);
    String payedGroup(double amount, String groupName, String username, User caller);
    String getStatus(User caller);
    String getPaymentHistory(User caller);
    void reload(Map<String, User> registeredUsers, Map<String, Group> groups);
}
