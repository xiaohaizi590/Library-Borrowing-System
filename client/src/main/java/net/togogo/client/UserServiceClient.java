package net.togogo.client;

import net.togogo.common.Result;
import net.togogo.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//feign 接口 - 使用直接URL，不依赖Nacos服务发现
@FeignClient(name = "nacon7-server", url = "http://localhost:8081", configuration = UserFeignConfig.class)
public interface UserServiceClient {

    @PostMapping("/api/users/register")
    Result<UserDTO> register(@RequestBody RegisterRequest request);

    @PostMapping("/api/users/login")
    Result<LoginResponse> login(@RequestBody LoginRequest request);

    @GetMapping("/api/users/getAllUsers")
    Result<PageResponse<UserDTO>> getAllUsers(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/api/users/getUserById/{id}")
    Result<UserDTO> getUserById(@PathVariable Long id);

    @GetMapping("/api/users/getUserByUsername/{username}")
    Result<UserDTO> getUserByUsername(@PathVariable String username);

    @PutMapping("/api/users/updateUser/{id}")
    Result<UserDTO> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request);

    @DeleteMapping("/api/users/deleteUser/{id}")
    Result<Void> deleteUser(@PathVariable Long id);

    @GetMapping("/api/users/profile")
    Result<UserDTO> getProfile();

}
