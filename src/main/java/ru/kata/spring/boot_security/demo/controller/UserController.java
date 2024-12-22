package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.entitys.Role;
import ru.kata.spring.boot_security.demo.entitys.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "registration";
        }
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/user")
    public String userPage(Model model, @AuthenticationPrincipal UserDetails loggedUser) {

        User user = userService.findByUsername(loggedUser.getUsername());
        model.addAttribute("user", user);
        return "user-page";
    }

    @GetMapping("/user/edit")
    public String editUserPage(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User loggedUser) {
        User user = userService.findByUsername(loggedUser.getUsername());
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/user/edit")
    public String editUser(@ModelAttribute("user") User user,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User loggedUser) {

        User currentUser = userService.findByUsername(loggedUser.getUsername());

        currentUser.setName(user.getName());
        currentUser.setSurname(user.getSurname());
        currentUser.setEmail(user.getEmail());

        if (!user.getPassword().isEmpty() && !user.getPassword().equals(currentUser.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.update(currentUser.getId(), currentUser);

        return "redirect:/user";
    }

    @PostMapping("/user/update")
    public String updateUser(@ModelAttribute("user") User user) {
        userService.update(user.getId(), user);
        return "redirect:/user";
    }

    @GetMapping("/admin/user-list")
    public String printUsers(Model model) {
        List<User> users = userService.findAll();
        System.out.println("Users retrieved from database: " + users); // Отладочный вывод
        model.addAttribute("users", users);
        return "admin";
    }

    @GetMapping("/admin/addUser")
    public String addUserPage(Model model) {
        List<Role> roles = roleService.findAllRoles();
        roles.forEach(role -> System.out.println("Role: " + role.getRoleName())); // Логирование
        model.addAttribute("user", new User());
        model.addAttribute("roles", roles);
        return "add-user";
    }

    @PostMapping("/admin/addUser")
    public String addUser(@ModelAttribute User user) {
        Set<Role> userRoles = new HashSet<>();
        for (Long roleId : user.getRoles().stream().map(Role::getId).toList()) {
            userRoles.add(roleService.findRoleById(roleId));
        }
        user.setRoles(userRoles);
        userService.save(user);
        return "redirect:/admin/user-list";
    }

    @GetMapping("/admin/edit/{id}")
    public String editUser(Model model, @PathVariable("id") Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.findAllRoles());
        return "admin-edit-user";
    }

    @PostMapping("/admin/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User user) {
        userService.update(id,user);
        return "redirect:/admin/user-list";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/user-list";
    }
}