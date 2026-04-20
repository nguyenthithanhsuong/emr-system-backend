package com.eclinic;

//Product class - Sử dụng chung
abstract class User {
    private final Database database;
    private final int id;
    private final String fullName;
    private final String userInformation;

    User(Database database, int id, String fullName, String userInformation) {
        this.database = database;
        this.id = id;
        this.fullName = fullName;
        this.userInformation = userInformation;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserInformation() {
        return userInformation;
    }

    public abstract String getRole();

    public void saveProfile() {
        String roleKey = getRole() + " - User";
        database.addUserRecord(new UserRecord(id, fullName, roleKey, getRole(), userInformation));
        System.out.println("Saved to Users table only.");
    }

    static final class UserRecord {
        private final int id;
        private final String fullName;
        private final String roleKey;
        private final String role;
        private final String userInformation;

        UserRecord(int id, String fullName, String roleKey, String role, String userInformation) {
            this.id = id;
            this.fullName = fullName;
            this.roleKey = roleKey;
            this.role = role;
            this.userInformation = userInformation;
        }

        int id() {
            return id;
        }

        String fullName() {
            return fullName;
        }

        String roleKey() {
            return roleKey;
        }

        String role() {
            return role;
        }

        String userInformation() {
            return userInformation;
        }

        @Override
        public String toString() {
            return id + " | " + fullName + " | " + roleKey + " | " + role + " | " + userInformation;
        }
    }
}
