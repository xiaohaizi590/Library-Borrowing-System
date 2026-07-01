package net.togogo.controller;

import net.togogo.common.Result;
import net.togogo.dto.*;
import net.togogo.entity.User;
import net.togogo.repository.UserRepository;
import net.togogo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = userService.register(request);
        return Result.success("注册成功", userDTO);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserDTO> users = userService.getAllUsers(pageable);
        return Result.success(users);
    }

    @GetMapping("/getUserById/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.isOwnUser(#id)")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return Result.success(userDTO);
    }

    @GetMapping("/getUserByUsername/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public Result<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO userDTO = userService.getUserByUsername(username);
        return Result.success(userDTO);
    }

    @GetMapping("/getUserByPhone/{phone}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDTO> getUserByPhone(@PathVariable String phone) {
        UserDTO userDTO = userService.getUserByPhone(phone);
        return Result.success(userDTO);
    }

    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userServiceImpl.isOwnUser(#id)")
    public Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && request.getRole() != null) {
            return Result.error(403, "普通用户不能修改角色");
        }
        
        UserDTO userDTO = userService.updateUser(id, request);
        return Result.success("更新成功", userDTO);
    }

    @DeleteMapping("/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<UserDTO> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        UserDTO userDTO = userService.getUserByUsername(username);
        return Result.success(userDTO);
    }
}
