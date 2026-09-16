package com.farm.finance.controller;

import com.farm.finance.model.User;
import com.farm.finance.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // ===== LOGIN PAGE =====
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // ===== PROCESS LOGIN =====
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        // Check if user exists
        User user = userRepository.findByUsername(username).orElse(null);


        if (user != null && user.getPassword().equals(password)) {
            // Login successful!
            session.setAttribute("user", user);
            return "redirect:/";
        } else {
            // Login failed
            model.addAttribute("error", "Invalid username or password!");
            return "login";
        }
    }

    // ===== REGISTER PAGE =====
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // ===== PROCESS REGISTRATION =====
    @PostMapping("/register")
    public String processRegistration(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String fullName,
                                      @RequestParam String email,
                                      Model model) {

        System.out.println("=========REGISTRATION ATTEMPT============");
        System.out.println("Username:" +username);
        System.out.println("Password:" +password);
        System.out.println("Full Name: "+fullName);
        System.out.println("Email:" +email);



try{
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            System.out.println("Username already exists!");
            model.addAttribute("error", "Username already taken!");
            return "register";
        }

        // Create new user
        User user = new User(username, password, fullName, email);
        userRepository.save(user);
        System.out.println("✅ User registered successfully!");

        model.addAttribute("success", "Registration successful! Please login.");
        return "login";
    }
    catch(Exception e){
        System.out.println("ERROR:" + e.getMessage());
        e.printStackTrace();
        model.addAttribute("error" ,"Register failed :" +e.getMessage());
        return "register";
    }
    }

    // ===== LOGOUT =====
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Clear session
        return "redirect:/login";
    }
}