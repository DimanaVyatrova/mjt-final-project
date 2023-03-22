package bg.sofia.uni.fmi.mjt.splitwise.users;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Objects;

public class User {
    private String username;
    private String password;
    private Set<String> friends;
    private Map<String, Double> groups;
    private Map<String, Double> friendCreditors;
    private Map<String, Double> friendDebtors;
    private Set<String> paymentHistory;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.friends = new HashSet<>();
        this.groups = new HashMap<>();
        this.friendDebtors = new HashMap<>();
        this.friendCreditors = new HashMap<>();
        this.paymentHistory = new HashSet<>();
    }

    public void addFriendToUser(User friend) {
        friends.add(friend.getUsername());
    }

    public void addGroupToUser(Group group) {
        groups.put(group.getName(), 0.0);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public Map<String, Double> getGroups() {
        return groups;
    }

    public Map<String, Double> getFriendCreditors() {
        return friendCreditors;
    }

    public Map<String, Double> getFriendDebtors() {
        return friendDebtors;
    }

    public Set<String> getPaymentHistory() {
        return paymentHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
