package com.eclinic;

import java.util.EnumMap;
import java.util.Map;

// Lớp điều phối use case Create User.
// Vai trò: chọn Concrete Creator theo userType,
// sau đó gọi Creator flow registerAccount(...).
public class UserService {
    private final Map<UserType, UserFactory> factories;

    public UserService(Database database) {
        //Database
        this.factories = new EnumMap<UserType, UserFactory>(UserType.class);
        this.factories.put(UserType.DOCTOR, new DoctorCreator(database));
        this.factories.put(UserType.PATIENT, new PatientCreator(database));
        this.factories.put(UserType.NURSE, new NurseCreator(database));
    }

    //Luồng đăng ký user mới. Client sẽ gọi hàm này, không cần quan tâm đến chi tiết cách thức tạo user.
    public User register(String userType, String fullName, String userInformation) {
        UserFactory factory = createFactory(UserType.from(userType));
        UserRegistrationRequest request = new UserRegistrationRequest(fullName, userInformation);
        return factory.registerAccount(request);
    }

    //Lấy Creator tương ứng với userType.
    private UserFactory createFactory(UserType userType) {
        UserFactory factory = factories.get(userType);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported user type: " + userType);
        }
        return factory;
    }

    //Request object chứa thông tin cần thiết để tạo user mới. 
    public static final class UserRegistrationRequest {
        private final String fullName;
        private final String userInformation;

        public UserRegistrationRequest(String fullName, String userInformation) {
            this.fullName = fullName == null ? "" : fullName.trim();
            this.userInformation = userInformation == null ? "" : userInformation.trim();
        }

        public String fullName() {
            return fullName;
        }

        public String userInformation() {
            return userInformation;
        }
    }
}
