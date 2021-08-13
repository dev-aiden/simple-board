package com.aiden.dev.simpleboard.modules.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    Account findByEmail(String email);

    Account findByLoginId(String loginId);

    Account findByNickname(String nickname);

    boolean existsByLoginIdAndEmail(String loginId, String email);
}
