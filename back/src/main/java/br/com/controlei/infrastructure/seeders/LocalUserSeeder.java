package br.com.controlei.infrastructure.seeders;

import br.com.controlei.domain.models.enums.Role;
import br.com.controlei.infrastructure.persistence.entities.FamilyEntity;
import br.com.controlei.infrastructure.persistence.entities.UserEntity;
import br.com.controlei.infrastructure.repositories.FamilyRepository;
import br.com.controlei.infrastructure.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Profile("local")
public class LocalUserSeeder implements CommandLineRunner {

    private final FamilyRepository familyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String familyName;
    private final String superAdminName;
    private final String superAdminEmail;
    private final String superAdminPassword;
    private final String memberName;
    private final String memberEmail;
    private final String memberPassword;

    public LocalUserSeeder(FamilyRepository familyRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seed.enabled:false}") boolean enabled,
                           @Value("${app.seed.family-name:Familia Controlei}") String familyName,
                           @Value("${app.seed.super-admin-name:Super Admin}") String superAdminName,
                           @Value("${app.seed.super-admin-email:superadmin@controlei.local}") String superAdminEmail,
                           @Value("${app.seed.super-admin-password:Controlei@123}") String superAdminPassword,
                           @Value("${app.seed.member-name:Gilvan Borges}") String memberName,
                           @Value("${app.seed.member-email:gilvan.borges@controlei.local}") String memberEmail,
                           @Value("${app.seed.member-password:Controlei@123}") String memberPassword) {
        this.familyRepository = familyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.familyName = familyName;
        this.superAdminName = superAdminName;
        this.superAdminEmail = superAdminEmail;
        this.superAdminPassword = superAdminPassword;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberPassword = memberPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        UserEntity superAdmin = userRepository.findByEmailAndDeletedAtIsNull(superAdminEmail)
                .orElseGet(this::createSeedFamilyWithSuperAdmin);

        ensureMemberExists(superAdmin.getFamilyId());
        ensureFamilyResponsible(superAdmin);
    }

    private UserEntity createSeedFamilyWithSuperAdmin() {
        FamilyEntity family = new FamilyEntity();
        family.setId(UUID.randomUUID());
        family.setName(familyName);
        family = familyRepository.save(family);

        UserEntity superAdmin = createUser(
                family.getId(),
                superAdminName,
                superAdminEmail,
                superAdminPassword,
                Role.RESPONSIBLE
        );

        family.setResponsibleUserId(superAdmin.getId());
        familyRepository.save(family);

        return superAdmin;
    }

    private void ensureMemberExists(UUID familyId) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(memberEmail)) {
            return;
        }

        createUser(familyId, memberName, memberEmail, memberPassword, Role.MEMBER);
    }

    private void ensureFamilyResponsible(UserEntity superAdmin) {
        Optional<FamilyEntity> family = familyRepository.findByIdAndDeletedAtIsNull(superAdmin.getFamilyId());
        family.ifPresent(existingFamily -> {
            if (existingFamily.getResponsibleUserId() == null) {
                existingFamily.setResponsibleUserId(superAdmin.getId());
                familyRepository.save(existingFamily);
            }
        });
    }

    private UserEntity createUser(UUID familyId, String name, String email, String password, Role role) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setFamilyId(familyId);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }
}
