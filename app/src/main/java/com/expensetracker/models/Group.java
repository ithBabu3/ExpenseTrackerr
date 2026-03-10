package com.expensetracker.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Group implements Serializable {
    private String groupId;
    private String groupName;
    private String joinCode;       // 6-digit code to join group
    private List<String> members;
    private String currentUser;

    public Group() {
        this.members = new ArrayList<>();
    }

    public Group(String groupName, String currentUser) {
        this();
        this.groupId = java.util.UUID.randomUUID().toString();
        this.groupName = groupName;
        this.currentUser = currentUser;
        this.joinCode = generateCode();
        this.members.add(currentUser);
    }

    private String generateCode() {
        Random r = new Random();
        int code = 100000 + r.nextInt(900000);
        return String.valueOf(code);
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    public void addMember(String member) {
        if (!members.contains(member)) members.add(member);
    }
}
