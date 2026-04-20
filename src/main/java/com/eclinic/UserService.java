package com.eclinic;

import java.util.EnumMap;
import java.util.Map;

// Lớp điều phối use case Create User.
// Vai trò: chọn Concrete Creator theo userType,
// sau đó gọi Creator flow registerAccount(...).
public class UserService {
    private final Map<UserType, UserFactory> factories;

    public UserService(Database database) {
        this.factories = new EnumMap<UserType, UserFactory>(UserType.class);
        this.factories.put(UserType.DOCTOR, new DoctorCreator(database));
        this.factories.put(UserType.PATIENT, new PatientCreator(database));
        // Khi mở rộng Nurse, thêm dòng sau:
        this.factories.put(UserType.NURSE, new NurseCreator(database));
    }

    public User register(String userType, String fullName, String userInformation) {
        UserFactory factory = createFactory(UserType.from(userType));
        UserRegistrationRequest request = new UserRegistrationRequest(fullName, userInformation);
        return factory.registerAccount(request);
    }

    private UserFactory createFactory(UserType userType) {
        UserFactory factory = factories.get(userType);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported user type: " + userType);
        }
        return factory;
    }

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
