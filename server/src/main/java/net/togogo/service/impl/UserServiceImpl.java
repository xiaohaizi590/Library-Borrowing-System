package net.togogo.service.impl;

import lombok.RequiredArgsConstructor;
import net.togogo.common.BusinessException;
import net.togogo.common.ResultCode;
import net.togogo.dto.LoginRequest;
import net.togogo.dto.LoginResponse;
import net.togogo.dto.PageResponse;
import net.togogo.dto.RegisterRequest;
import net.togogo.dto.UpdateUserRequest;
import net.togogo.dto.UserDTO;
import net.togogo.entity.User;
import net.togogo.repository.UserRepository;
import net.togogo.security.JwtUtil;
import net.togogo.service.UserService;
import net.togogo.util.PasswordUtil;
import net.togogo.service.LoginAttemptService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Service("userServiceImpl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    // 注入 UserRepository
    private final UserRepository userRepository;
    // 注入 JwtUtil
    private final JwtUtil jwtUtil;
    // 注入 LoginAttemptService
    private final LoginAttemptService loginAttemptService;
    // 注入 RedisTemplate
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXIST);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(PasswordUtil.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ip) {
        String username = request.getAccount();
        String captchaKey = request.getCaptchaKey();
        String captchaText = request.getCaptcha();
    

        // 检查验证码是否正确
        String cachedCaptcha = redisTemplate.opsForValue().get("captcha:" + captchaKey);
        if (!cachedCaptcha.equals(captchaText)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        // 验证码正确，删除 Redis 中的验证码
        redisTemplate.delete("captcha:" + captchaKey);

        // 检查账号是否被锁定
        if (loginAttemptService.isExceeded(username, ip)) {
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 密码校验
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.incrementAttempts(username, ip);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 登录成功，清除失败记录
        loginAttemptService.resetAttempts(username, ip);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    @Override
    @Cacheable(value = "users", key = "'all:' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PageResponse<UserDTO> getAllUsers(Pageable pageable) {
        Page<UserDTO> page = userRepository.findAll(pageable).map(this::convertToDTO);
        return PageResponse.from(page);
    }

    @Override
    @Cacheable(value = "users", key = "'id:' + #id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    @Cacheable(value = "users", key = "'username:' + #username")
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    @Cacheable(value = "users", key = "'phone:' + #phone")
    public UserDTO getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(User.Role.valueOf(request.getRole()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public boolean isOwnUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return false;
        }
        
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .createTime(user.getCreateTime())
                .role(user.getRole())
                .build();
    }
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .createTime(user.getCreateTime())
                .role(user.getRole())
                .build();
    }
}
