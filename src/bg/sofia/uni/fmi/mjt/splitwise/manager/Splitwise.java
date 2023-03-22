package bg.sofia.uni.fmi.mjt.splitwise.manager;

import bg.sofia.uni.fmi.mjt.splitwise.memory.MemoryHandler;
import bg.sofia.uni.fmi.mjt.splitwise.users.Group;
import bg.sofia.uni.fmi.mjt.splitwise.users.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Splitwise implements MoneyManager {
    private Map<String, User> registeredUsers = new HashMap<>();
    private MemoryHandler memoryHandler = new MemoryHandler();
    private Map<String, Group> groups = new HashMap<>();

    public Splitwise() { }

    private void validateString(String string) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException();
        }
    }
    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String register(User user) {
        validateString(user.getUsername());
        validateString(user.getPassword());

        if (registeredUsers.containsKey(user.getUsername())) {
            return "The username <" + user.getUsername() + "> is taken. Please provide a different one.";
        }

        registeredUsers.put(user.getUsername(), user);
        memoryHandler.addNewUserToDatabase(user.getUsername(), user.getPassword());
        return "You registered successfully with username <" + user.getUsername() + ">";
    }

    @Override
    public boolean login(User user) {
        validateString(user.getUsername());
        validateString(user.getPassword());

        if (!registeredUsers.containsKey(user.getUsername())) {
            return false;
        }
        else return registeredUsers.get(user.getUsername()).getPassword().equals(user.getPassword());
    }

    @Override
    public String addFriend(String friendName, User caller) {
        validateString(friendName);

        if (!registeredUsers.containsKey(friendName)) {
            return "There is no user with username <" + friendName + ">";
        }
        if (friendName.equals(caller.getUsername())) {
            return "You cannot add yourself as a friend";
        }

        registeredUsers.get(caller.getUsername()).addFriendToUser(registeredUsers.get(friendName));
        registeredUsers.get(friendName).addFriendToUser(registeredUsers.get(caller.getUsername()));
        memoryHandler.rememberFriend(caller.getUsername(), friendName);
        memoryHandler.rememberFriend(friendName, caller.getUsername());

        return "You and " + friendName + " are now friends!";
    }

    private Set<String> getUnregisteredUsers(Set<String> members) {
        Set<String> unregistered = new HashSet<>();
        for (String memberName : members) {
            if (!registeredUsers.containsKey(memberName)) {
                unregistered.add(memberName);
            }
        }
        return unregistered;
    }

    private Group addUsersToGroup(String groupName, Set<String> members) {
        Group group = new Group(groupName);
        for (String memberName : members) {
            if (registeredUsers.containsKey(memberName)) {
                group.addMember(memberName, 0.0);
            }
        }

        return group;
    }

    private String buildCreateGroupErrorMessage(Set<String> unregistered) {
        StringBuilder errorMessage = new StringBuilder();
        for (String memberName : unregistered) {
            errorMessage.append("User ").append(memberName)
                    .append(" couldn't be added to the group, because there is no such registered user")
                    .append(System.lineSeparator());
        }
        errorMessage.append("all others were added successfully");
        return errorMessage.toString();
    }

    @Override
    public String createGroup(String groupName, Set<String> members) {
        Set<String> unregistered = getUnregisteredUsers(members);
        Group group = addUsersToGroup(groupName, members);

        groups.put(groupName, group);

        for (String memberName : group.getMembers()) {
            registeredUsers.get(memberName).addGroupToUser(group);
            memoryHandler.rememberGroup(memberName, groupName);
        }

        if (unregistered.isEmpty()) {
            return "All users were added successfully to a group!";
        }
        else {
            return buildCreateGroupErrorMessage(unregistered);
        }
    }

    private double getCurrentlyOwedFriend(String friendName, User caller) {
        double currentlyOwedFriend = 0;
        if (registeredUsers.get(friendName).getFriendCreditors().get(caller.getUsername()) != null) {
            currentlyOwedFriend = registeredUsers.get(friendName).getFriendCreditors().get(caller.getUsername());
        }
        return currentlyOwedFriend;
    }
    private double getCurrentlyOwedCaller(String friendName, User caller) {
        double currentlyOwedCaller = 0;
        if (registeredUsers.get(caller.getUsername()).getFriendCreditors().get(friendName) != null) {
            currentlyOwedCaller = registeredUsers.get(caller.getUsername()).getFriendCreditors().get(friendName);
        }
        return currentlyOwedCaller;
    }

    private void updateData(String friendName, User caller, double currentlyOwedFriend,
                            double currentlyOwedCaller, double amount) {
        registeredUsers.get(friendName).getFriendCreditors().put(caller.getUsername(), currentlyOwedFriend);
        registeredUsers.get(friendName).getFriendDebtors().put(caller.getUsername(), currentlyOwedCaller);

        registeredUsers.get(caller.getUsername()).getFriendCreditors().put(friendName, currentlyOwedCaller);
        registeredUsers.get(caller.getUsername()).getFriendDebtors().put(friendName, currentlyOwedFriend);
        registeredUsers.get(caller.getUsername()).getPaymentHistory()
                .add("Receiver:" + friendName + " amount:" + amount / 2);

        memoryHandler.rememberPaymentHistory(caller.getUsername(), "Receiver:" + friendName + " amount:" + amount / 2);
        memoryHandler.updateUserSplitFriend(caller.getUsername(), friendName, currentlyOwedFriend, currentlyOwedCaller);
    }
    @Override
    public String split(double amount, String friendName, String reason, User caller) {
        validateString(friendName);
        validateString(reason);
        validateAmount(amount);
        if (!registeredUsers.get(caller.getUsername()).getFriends().contains(friendName)) {
            return "User <" + friendName + "> is not on your friend list";
        }

        double currentlyOwedFriend = getCurrentlyOwedFriend(friendName, caller);
        double currentlyOwedCaller = getCurrentlyOwedCaller(friendName, caller);

        if (currentlyOwedCaller >= amount / 2) {
            currentlyOwedCaller = currentlyOwedCaller - amount / 2;
        }
        else {
            currentlyOwedFriend = currentlyOwedFriend + amount / 2 - currentlyOwedCaller;
            currentlyOwedCaller = 0;
        }

        updateData(friendName, caller, currentlyOwedFriend, currentlyOwedCaller, amount);
        return friendName + " owes you " + currentlyOwedFriend;
    }

    private String validateGroup(String groupName, User caller) {
        if (!groups.containsKey(groupName)) {
            return "Group with name <" + groupName + "> doesn't exist";
        }
        else if (!groups.get(groupName).getMembers().contains(caller.getUsername())) {
            return "You are not a part of a group with name <" + groupName + ">";
        }
        return null;
    }

    private String buildSplitGroupMessage(String groupName, User caller) {
        StringBuilder message = new StringBuilder(groupName + ":" + System.lineSeparator());
        for (String member : groups.get(groupName).getMembers()) {
            if (!member.equals(caller.getUsername())) {
                message.append(member).append(" owes ")
                        .append(registeredUsers.get(member).getGroups().get(groupName))
                        .append(" LV to group ").append(groupName).append(System.lineSeparator());
            }
        }

        return message.toString();
    }
    @Override
    public String splitGroup(double amount, String groupName, String reason, User caller) {
        validateString(groupName);
        validateString(reason);
        validateAmount(amount);
        if (validateGroup(groupName, caller) != null) {
            return validateGroup(groupName, caller);
        }

        for (String member : groups.get(groupName).getMembers()) {
            if (!member.equals(caller.getUsername())) {
                double currentlyOwed = 0;
                if (registeredUsers.get(member).getGroups().get(groupName) != null) {
                    currentlyOwed = registeredUsers.get(member).getGroups().get(groupName);
                }
                registeredUsers.get(member).getGroups()
                        .put(groupName, currentlyOwed + amount / (groups.get(groupName).getMembers().size()));
                memoryHandler.updateUserGroupDebt(member, groupName,
                        currentlyOwed + amount / (groups.get(groupName).getMembers().size()));
            }
            else {
                double currentlyOwedCaller = registeredUsers.get(caller.getUsername()).getGroups().get(groupName);
                if (currentlyOwedCaller >= amount / (groups.get(groupName).getMembers().size())) {
                    currentlyOwedCaller = currentlyOwedCaller - amount / (groups.get(groupName).getMembers().size());
                } else {
                    currentlyOwedCaller = 0;
                }
                registeredUsers.get(member).getGroups().put(groupName, currentlyOwedCaller);
                String history = "Receiver: group " + groupName
                        + " Amount:" + amount / (groups.get(groupName).getMembers().size());
                registeredUsers.get(member).getPaymentHistory().add(history);
                memoryHandler.rememberPaymentHistory(member, history);
                memoryHandler.updateUserGroupDebt(member, groupName, currentlyOwedCaller);
            }
        }

        return buildSplitGroupMessage(groupName, caller);
    }

    @Override
    public String payed(double amount, String username, User caller) {
        validateString(username);
        validateAmount(amount);
        if (!registeredUsers.get(caller.getUsername()).getFriends().contains(username)) {
            return "User with username <" + username + "> is not on you friend list.";
        }
        if (registeredUsers.get(username).getFriendCreditors().get(caller.getUsername()) == null
                || registeredUsers.get(username).getFriendCreditors().get(caller.getUsername()) == 0) {
            return username + " doesn't owe you anything";
        }

        double currentlyOwed = registeredUsers.get(username).getFriendCreditors().get(caller.getUsername());
        registeredUsers.get(username).getFriendCreditors().put(caller.getUsername(), currentlyOwed - amount);
        registeredUsers.get(username).getPaymentHistory().add("Receiver:" + caller.getUsername() + " Amount:" + amount);
        memoryHandler.rememberPaymentHistory(username, "Receiver:" + caller.getUsername() + " Amount:" + amount);
        registeredUsers.get(caller.getUsername()).getFriendDebtors().put(username, currentlyOwed - amount);
        memoryHandler.updateUserSplitFriend(caller.getUsername(), username,
                currentlyOwed - amount, 0);

        return username + " now owes you " + (currentlyOwed - amount);
    }

    private String validatePayedGroup(String username, String groupName) {
        if (!registeredUsers.containsKey(username)) {
            return "There is no registered user with username <" + username + ">";
        }
        if (!groups.containsKey(groupName)) {
            return "There is no group named <" + groupName + ">";
        }
        if (!groups.get(groupName).getMembers().contains(username)) {
            return "The user <" + username + "> is not a part of the group <" + groupName + ">";
        }
        if (registeredUsers.get(username).getGroups().get(groupName) == 0) {
            return "The user <" + username + "> doesn't owe anything to the group <" + groupName + ">";
        }
        return null;
    }

    @Override
    public String payedGroup(double amount, String groupName, String username, User caller) {
        validateString(groupName);
        validateString(username);
        validateAmount(amount);
        if (validatePayedGroup(username, groupName) != null) {
            return validatePayedGroup(username, groupName);
        }

        double currentlyOwed = 0;
        if (registeredUsers.get(username).getGroups().get(groupName) != null) {
            currentlyOwed = registeredUsers.get(username).getGroups().get(groupName);
        }
        if (currentlyOwed - amount < 0) {
            return username + " owns less than " + amount;
        }
        registeredUsers.get(username).getGroups().put(groupName, currentlyOwed - amount);
        registeredUsers.get(username).getPaymentHistory().add("Receiver: group " + groupName + " Amount:" + amount);
        memoryHandler.rememberPaymentHistory(username, "Receiver: group " + groupName + " Amount:" + amount);
        memoryHandler.updateUserGroupDebt(username, groupName, currentlyOwed - amount);

        return username + " now owns " + (currentlyOwed - amount) + " LV to group <" + groupName + ">";
    }

    private StringBuilder getFriendsStatus(User caller) {
        StringBuilder message = new StringBuilder("Friends:" + System.lineSeparator());

        for (Map.Entry<String, Double> friend :
                registeredUsers.get(caller.getUsername()).getFriendCreditors().entrySet()) {
            if (friend.getValue() != 0) {
                message.append("* ").append(friend.getKey()).append(": You owe ")
                        .append(friend.getValue()).append(" LV.").append(System.lineSeparator());
            }
        }

        for (Map.Entry<String, Double> friend :
                registeredUsers.get(caller.getUsername()).getFriendDebtors().entrySet()) {
            if (friend.getValue() != 0) {
                message.append("* ").append(friend.getKey()).append(": Owes you ")
                        .append(friend.getValue()).append(" LV.").append(System.lineSeparator());
            }
        }

        return message;
    }

    private StringBuilder getGroupsStatus(User caller) {
        StringBuilder message = new StringBuilder();
        message.append("Groups:").append(System.lineSeparator());

        for (Map.Entry<String, Double> group : registeredUsers.get(caller.getUsername()).getGroups().entrySet()) {
            message.append("* Group ").append(group.getKey()).append(": You owe ")
                    .append(group.getValue()).append(System.lineSeparator());
            for (String member : groups.get(group.getKey()).getMembers()) {
                if (!member.equals(caller.getUsername())) {
                    message.append("* Group ").append(group.getKey()).append(": ")
                            .append(member).append(" owes ")
                            .append(registeredUsers.get(member).getGroups().get(group.getKey()))
                            .append(" LV.").append(System.lineSeparator());
                }
            }
        }

        return message;
    }

    @Override
    public String getStatus(User caller) {
        if (caller == null) {
            return "In order to see your status, you need to log in first.";
        }
        StringBuilder messageFriends = getFriendsStatus(caller);
        StringBuilder messageGroups = getGroupsStatus(caller);
        StringBuilder message = messageFriends.append(messageGroups);

        if (message.toString().equals("Friends:" + System.lineSeparator() + "Groups:" + System.lineSeparator())) {
            return "You have no new notification.";
        }

        return message.toString();
    }

    @Override
    public String getPaymentHistory(User caller) {
        StringBuilder content = new StringBuilder();
        for (String item : registeredUsers.get(caller.getUsername()).getPaymentHistory()) {
            content.append(item).append(System.lineSeparator());
        }
        return content.toString();
    }

    @Override
    public void reload(Map<String, User> registeredUsers, Map<String, Group> groups) {
        this.registeredUsers = registeredUsers;
        this.groups = groups;
    }
}
