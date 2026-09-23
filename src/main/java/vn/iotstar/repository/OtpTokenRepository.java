package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.iotstar.entity.OtpToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndTokenTypeAndUsedFalseOrderByCreatedAtDesc(String email, String tokenType);

    List<OtpToken> findByEmailAndTokenTypeAndUsedFalse(String email, String tokenType);
}
