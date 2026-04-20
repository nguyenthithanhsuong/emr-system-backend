package com.eclinic;

import com.eclinic.UserService.UserRegistrationRequest;

//Concrete Product cho Patient.
final class Patient extends User {
    Patient(Database database, int id, String fullName, String medicalCondition) {
        super(database, id, fullName, medicalCondition);
    }

    @Override
    public String getRole() {
        return "Patient";
    }
}

//Concrete Creator cho nhánh Patient.
final class PatientCreator extends UserFactory {
    private final Database database;

    public PatientCreator(Database database) {
        this.database = database;
    }

    @Override
    protected void validateRequest(UserRegistrationRequest request) {
        super.validateRequest(request);
        if (isBlank(request.userInformation())) {
            throw new IllegalArgumentException("Medical condition is required for patient");
        }
    }

    @Override
    protected User createUser(UserRegistrationRequest request) {
        int id = database.nextUserId();
        return new Patient(
                database,
                id,
                request.fullName(),
                request.userInformation());
    }
}