package com.eclinic;

import com.eclinic.UserService.UserRegistrationRequest;

//Creator
public abstract class UserFactory {
    //Gọi hàm validate, create, save và audit theo flow chung.
    //User sẽ chỉ cần gọi hàm này
    public final User registerAccount(UserRegistrationRequest request) {
        validateRequest(request);
        User user = createUser(request);
        user.saveProfile();
        audit(user);
        return user;
    }

    //Lệnh createProduct() được thể hiện ở đây
    protected abstract User createUser(UserRegistrationRequest request);

    //Các business rule chung cho cả Doctor và Patient có thể đặt ở đây.
    protected void validateRequest(UserRegistrationRequest request) {
        if (isBlank(request.fullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    //Hàm tiện ích để kiểm tra chuỗi rỗng hoặc null.
    protected static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    //Thông báo đã tạo 1 product mới
    protected void audit(User user) {
        System.out.println("Created account: " + user.getRole() + " - " + user.getFullName());
    }
}



