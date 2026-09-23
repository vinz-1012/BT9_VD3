package vn.iotstar;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.config.DatabaseInitializer;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class ShopSpringboot411Application {

    public static void main(String[] args) {
        DatabaseInitializer.initDatabase();

        SpringApplication.run(ShopSpringboot411Application.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            if (!userRepository.existsByUsername("admin")) {
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(roleUser);
                adminRoles.add(roleAdmin);

                User admin = User.builder()
                        .username("admin")
                        .email("admin@shop.vn")
                        .fullName("Quản Trị Viên")
                        .phone("0901234567")
                        .password(passwordEncoder.encode("123456"))
                        .enabled(true)
                        .roles(adminRoles)
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
