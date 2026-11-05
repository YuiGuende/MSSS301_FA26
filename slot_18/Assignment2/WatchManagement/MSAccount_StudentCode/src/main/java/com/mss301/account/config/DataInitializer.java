package com.mss301.account.config;

import com.mss301.account.entity.StoreAccount;
import com.mss301.account.repository.AccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// [TODO-3.1] Seeds the five required accounts once; passwords are stored as BCrypt hashes
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String SEED_PASSWORD = "@1";

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;
        }
        List<StoreAccount> accounts = List.of(
                account("adminsys", "admin@drawingpen.com.vn", 1, true),
                account("staff02", "staff02@drawingpen.com.vn", 2, true),
                account("member01", "member01@drawingpen.com.vn", 3, true),
                account("member02", "member02@drawingpen.com.vn", 3, false),
                account("staff01", "staff01@drawingpen.com.vn", 2, false));
        accountRepository.saveAll(accounts);
        log.info("Seeded {} accounts", accounts.size());
    }

    private StoreAccount account(String username, String email, int role, boolean active) {
        return StoreAccount.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(SEED_PASSWORD))
                .role(role)
                .isActive(active)
                .build();
    }
}
