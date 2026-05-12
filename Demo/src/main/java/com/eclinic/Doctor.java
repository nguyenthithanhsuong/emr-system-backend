package com.eclinic;

import com.eclinic.UserService.UserRegistrationRequest;

//Concrete Product cho Doctor.
final class Doctor extends User {
    Doctor(Database database, int id, String fullName, String specialty) {
        super(database, id, fullName, specialty);
    }

    //Định nghĩa role riêng cho Doctor.
    @Override
    public String getRole() {
        return "Doctor";
    }
}

//Concrete Creator cho nhánh Doctor.
final class DoctorCreator extends UserFactory {
    private final Database database;

    public DoctorCreator(Database database) {
        this.database = database;
    }

    //Override validateRequest để thêm business rule riêng cho Doctor.
    @Override
    protected void validateRequest(UserRegistrationRequest request) {
        super.validateRequest(request);
        if (isBlank(request.userInformation())) {
            throw new IllegalArgumentException("Specialty is required for doctor");
        }
    }

    //Override createUser để tạo Doctor mới.
    @Override
    protected User createUser(UserRegistrationRequest request) {
        int id = database.nextUserId();
        return new Doctor(
                database,
                id,
                request.fullName(),
                request.userInformation());
    }
}