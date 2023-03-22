package bg.sofia.uni.fmi.mjt.splitwise.command;

import bg.sofia.uni.fmi.mjt.splitwise.manager.MoneyManager;
import bg.sofia.uni.fmi.mjt.splitwise.memory.MemoryHandler;
import bg.sofia.uni.fmi.mjt.splitwise.users.User;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class CommandExecutor {
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String ADD = "add-friend";
    private static final String CREATE = "create-group";
    private static final String SPLIT = "split";
    private static final String SPLIT_GROUP = "split-group";
    private static final String STATUS = "get-status";
    private static final String PAYED = "payed";
    private static final String PAYED_GROUP = "payed-group";
    private static final String HISTORY = "payment-history";
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private Map<SelectionKey, User> loggedUsers;
    private MoneyManager moneyManager;
    private MemoryHandler memoryHandler;

    public CommandExecutor(MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
        this.loggedUsers = new HashMap<>();
        this.memoryHandler = new MemoryHandler();
    }

    public void reload() throws IOException {
        moneyManager.reload(memoryHandler.loadRegisteredUsers("memory.txt"), memoryHandler.loadGroups());
    }
    private String register(String[] args) {
        if (args.length != TWO) {
            return "In order to register you need to provide your username and password";
        }
        User newUser = new User(args[0], args[1]);
        return moneyManager.register(newUser);
    }

    private String login(String[] args, SelectionKey key) {
        if (args.length != TWO) {
            return "In order to login you need to provide your username and password";
        }

        User user = new User(args[0], args[1]);
        boolean successful = moneyManager.login(user);
        if (successful) {
            loggedUsers.put(key, user);
            return "Login successful!" + System.lineSeparator() + getStatus(key);
        }
        return "Login unsuccessful. Wrong username or password.";
    }

    private String addFriend(String[] args, SelectionKey key) {
        if (args.length != ONE) {
            return "In order to add a friend, you must provide only their username";
        }
        User commandCaller = loggedUsers.get(key);
        return moneyManager.addFriend(args[0], commandCaller);
    }

    private String createGroup(String[] args, SelectionKey key) {
        if (args.length < THREE) {
            return "In order to create a group you must provide at least 3 arguments: group name and 2 member names";
        }
        Set<String> memberNames = new HashSet<>();
        String creatorName = loggedUsers.get(key).getUsername();
        memberNames.add(creatorName);

        memberNames.addAll(Arrays.asList(args).subList(1, args.length));
        return moneyManager.createGroup(args[0], memberNames);
    }

    private String split(String[] args, SelectionKey key) {
        if (args.length != THREE) {
            return "In order to split with a friend you must provide 3 arguments: amount, friend's name and reason";
        }
        User commandCaller = loggedUsers.get(key);
        try {
            return moneyManager.split(Double.parseDouble(args[0]), args[1], args[2], commandCaller);
        } catch (NumberFormatException e) {
            return "The first argument <amount> must be a number";
        }
    }

    private String splitGroup(String[] args, SelectionKey key) {
        if (args.length != THREE) {
            return "In order to split with a group, you must provide 3 arguments: amount, group name, reason";
        }
        User commandCaller = loggedUsers.get(key);
        try {
            return moneyManager.splitGroup(Double.parseDouble(args[0]), args[1], args[2], commandCaller);
        } catch (NumberFormatException e) {
            return "The first argument <amount> must be a number";
        }
    }

    private String getStatus(SelectionKey key) {
        User commandCaller = loggedUsers.get(key);
        return moneyManager.getStatus(commandCaller);
    }

    private String payed(String[] args, SelectionKey key) {
        if (args.length != TWO) {
            return "You must provide 2 arguments: amount and friend's name";
        }

        User commandCaller = loggedUsers.get(key);

        try {
            return moneyManager.payed(Double.parseDouble(args[0]), args[1], commandCaller);
        } catch (NumberFormatException e) {
            return "The first argument <amount> must be a number";
        }
    }

    private String payedGroup(String[] args, SelectionKey key) {

        if (args.length != THREE) {
            return "You must provide 3 arguments: amount, group name and member name";
        }
        User commandCaller = loggedUsers.get(key);
        try {
            return moneyManager.payedGroup(Double.parseDouble(args[0]), args[1], args[2], commandCaller);
        } catch (NumberFormatException e) {
            return "The first argument <amount> must be a number";
        }
    }

    private String paymentHistory(SelectionKey key) {
        return moneyManager.getPaymentHistory(loggedUsers.get(key));
    }

    public String execute(Command cmd, SelectionKey key) {
        return switch (cmd.command()) {
            case REGISTER -> register(cmd.arguments());
            case LOGIN -> login(cmd.arguments(), key);
            case ADD -> addFriend(cmd.arguments(), key);
            case CREATE -> createGroup(cmd.arguments(), key);
            case SPLIT -> split(cmd.arguments(), key);
            case SPLIT_GROUP -> splitGroup(cmd.arguments(), key);
            case STATUS -> getStatus(key);
            case PAYED -> payed(cmd.arguments(), key);
            case PAYED_GROUP -> payedGroup(cmd.arguments(), key);
            case HISTORY -> paymentHistory(key);
            default -> "Unknown command";
        };
    }
}