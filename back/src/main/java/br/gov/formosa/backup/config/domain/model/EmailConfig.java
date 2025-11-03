package br.gov.formosa.backup.config.domain.model;

import br.gov.formosa.backup.user.domain.model.User;
import br.gov.formosa.backup.shared.config.CryptoConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "email_config")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmailConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 1000)
    @Convert(converter = CryptoConverter.class)
    private String password;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}