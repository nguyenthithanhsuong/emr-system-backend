package com.eclinic;
import com.eclinic.util.JwtUtil;
public class TestToken {
    public static void main(String[] args) {
        System.out.println(JwtUtil.generateToken(1L, "admin", "ADMIN"));
    }
}
