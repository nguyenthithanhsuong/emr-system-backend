package com.eclinic;

//Client code gọi đến service để tạo user mới. Không cần quan tâm đến chi tiết cách thức tạo user.
public class Client {
    private final UserService registrationService;

    public Client(UserService registrationService) {
        this.registrationService = registrationService;
    }

    public User createNewAccount(String userType, String fullName, String userInformation) {
        return registrationService.register(userType, fullName, userInformation);
    }
}
