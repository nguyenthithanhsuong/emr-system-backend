package com.eclinic;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


//Database cho ứng dụng. Lưu trữ dữ liệu tạm thời trong bộ nhớ.
//Có thể được thay bằng các Database siêu việt hơn như là MySQL, MongoDB,... mà không cần thay đổi code ở tầng trên.
public class Database {
    private final AtomicInteger currentUserId = new AtomicInteger(0);
    private final List<User.UserRecord> users = Collections.synchronizedList(new ArrayList<User.UserRecord>());
    private final List<DoctorUser.DoctorRecord> doctors = Collections.synchronizedList(new ArrayList<DoctorUser.DoctorRecord>());
    private final List<PatientUser.PatientRecord> patients = Collections.synchronizedList(new ArrayList<PatientUser.PatientRecord>());

    public int nextUserId() {
        return currentUserId.incrementAndGet();
    }

    public List<User.UserRecord> getUsers() {
        return users;
    }

    public List<DoctorUser.DoctorRecord> getDoctors() {
        return doctors;
    }

    public List<PatientUser.PatientRecord> getPatients() {
        return patients;
    }

    public void printSnapshot() {
        System.out.println("Users:");
        for (User.UserRecord user : users) {
            System.out.println("- " + user);
        }

        System.out.println("Doctors:");
        for (DoctorUser.DoctorRecord doctor : doctors) {
            System.out.println("- " + doctor);
        }

        System.out.println("Patients:");
        for (PatientUser.PatientRecord patient : patients) {
            System.out.println("- " + patient);
        }
    }
}
