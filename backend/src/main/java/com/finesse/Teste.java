package com.finesse;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Teste {

   
    public static void main(String[] args) {
       final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("Hello World!");
        System.out.println(passwordEncoder.encode("123456").toString());

    }
}
