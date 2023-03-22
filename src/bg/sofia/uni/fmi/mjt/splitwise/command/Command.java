package bg.sofia.uni.fmi.mjt.splitwise.command;

import java.util.Arrays;
import java.util.Objects;

public record Command(String command, String[] arguments) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command1 = (Command) o;
        return Objects.equals(command, command1.command) && Arrays.equals(arguments, command1.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(command);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}

