package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

import java.util.Optional;

public interface UserService {

    Page<UserDTO> getAllUsers(String keyword, Pageable pageable);

    UserDTO getUserById(Long id);

    User findEntityById(Long id);

    User findByUsername(String username);

    User findByEmail(String email);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    long countTotalUsers();

    long countProductsByUserId(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
