package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlTransient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NaturalId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String passwordHash;

    @XmlTransient
    public String getPasswordHash() {
        return passwordHash;
    }

    @XmlTransient
    public void setPasswordHash(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }

    public String hashPassword(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean passwordIsValid(String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return StringUtils.isEmpty(this.getPasswordHash()) ||
                bCryptPasswordEncoder.matches(password, this.getPasswordHash());
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
