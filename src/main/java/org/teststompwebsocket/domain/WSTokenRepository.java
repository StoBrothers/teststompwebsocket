package org.teststompwebsocket.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WSTokenRepository extends JpaRepository<WSToken, Long> {

    Optional<WSToken> findOneById(Long id);

    Optional<WSToken> findOneByUserAndActive(User user, Boolean active);

    Optional<WSToken> findOneByToken(String token);

    Optional<WSToken> findOneByActive(Boolean active);

    Optional<WSToken> findOneByPrincipalNameAndActive(String principalName,
        Boolean active);

}
