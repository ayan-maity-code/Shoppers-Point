package com.shopperspoint;

import com.shopperspoint.entity.Role;
import com.shopperspoint.entity.User;
import com.shopperspoint.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class ShoppersPointApplication implements CommandLineRunner {
	private final UserRepo userRepo;
	private final PasswordEncoder encoder;

	@Autowired
	public ShoppersPointApplication(PasswordEncoder encoder, UserRepo userRepo) {
		this.encoder = encoder;
		this.userRepo = userRepo;
	}

	public static void main(String[] args) {
		SpringApplication.run(ShoppersPointApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		String adminEmail = "admin@gmail.com";
		Optional<User> adminUser = userRepo.findByEmail(adminEmail);

		if (adminUser.isEmpty()) {
			User admin = new User();
			admin.setEmail("admin@gmail.com");
			admin.setFirstName("Admin");
			admin.setLastName("Roy");
			admin.setPassword(encoder.encode("Admin@1234"));
			admin.setIsActive(true);
			admin.setIsDeleted(false);
			admin.setIsLocked(false);
			admin.setIsExpired(false);
			admin.setInvalidAttemptCount(0);
			admin.setPasswordUpdatedDate(LocalDateTime.now());

			Set<Role> roles = new HashSet<>();

			Role role = new Role();
			role.setAuthority("ADMIN");
			roles.add(role);
			admin.setRoles(roles);
			userRepo.save(admin);
		}

	}
}
