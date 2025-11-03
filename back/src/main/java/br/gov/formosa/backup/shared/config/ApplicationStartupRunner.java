package br.gov.formosa.backup.shared.config;

import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.user.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationStartupRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin-password}")
    private String defaultPassword;
    private final String DEFAULT_ADMIN_EMAIL = "admin@admin.com";

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User adminUser = User.builder()
                    .email(DEFAULT_ADMIN_EMAIL)
                    .password(passwordEncoder.encode(defaultPassword))
                    .mustChangePassword(true)
                    .build();
            userRepository.save(adminUser);

            LOG.info("************************************************************");
            LOG.info("NENHUM USUÁRIO ENCONTRADO. CRIADO USUÁRIO PADRÃO:");
            LOG.info("Usuário: {}", DEFAULT_ADMIN_EMAIL);
            LOG.info("Senha:   {}", defaultPassword);
            LOG.info("************************************************************");
        }
    }
}