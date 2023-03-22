package bg.sofia.uni.fmi.mjt.splitwise.users;

import java.util.Set;
import java.util.HashSet;

public class Group {
    private String name;
    private Set<String> members;

    public Group(String name) {
        this.name = name;
        this.members = new HashSet<>();
    }

    public void addMember(String member, Double debt) {
        members.add(member);
    }
    public String getName() {
        return name;
    }
    public Set<String> getMembers() {
        return members;
    }
}
