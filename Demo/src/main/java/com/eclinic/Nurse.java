package com.eclinic;

import com.eclinic.UserService.UserRegistrationRequest;

//Concrete Product cho Nurse
final class Nurse extends User {
    Nurse(Database database, int id, String fullName, String department) {
        super(database, id, fullName, department);
    }

    @Override
    public String getRole() {
        return "Nurse";
    }
}

final class NurseCreator extends UserFactory {
    private final Database database;

    public NurseCreator(Database database) {
        this.database = database;
    }

    @Override
    protected void validateRequest(UserRegistrationRequest request) {
        super.validateRequest(request);
        if (isBlank(request.userInformation())) {
            throw new IllegalArgumentException("Department is required for nurse");
        }
    }

    @Override
    protected User createUser(UserRegistrationRequest request) {
        int id = database.nextUserId();
        return new Nurse(
                database,
                id,
                request.fullName(),
                request.userInformation());
    }
}