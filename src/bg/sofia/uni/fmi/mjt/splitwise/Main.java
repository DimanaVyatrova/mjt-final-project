package bg.sofia.uni.fmi.mjt.splitwise;

import bg.sofia.uni.fmi.mjt.splitwise.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.splitwise.manager.MoneyManager;
import bg.sofia.uni.fmi.mjt.splitwise.manager.Splitwise;
import bg.sofia.uni.fmi.mjt.splitwise.server.Server;

import java.io.IOException;

public class Main {
    static final int PORT = 5555;

    public static void main(String[] args) throws IOException {
        MoneyManager splitwise = new Splitwise();
        CommandExecutor commandExecutor = new CommandExecutor(splitwise);
        Server server = new Server(PORT, commandExecutor);
        server.start();
    }
}
