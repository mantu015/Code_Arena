package com.codearena.repository;

import com.codearena.model.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface BattleRepository extends JpaRepository<Battle, Long> {
    Optional<Battle> findByBattleCode(String battleCode);
    List<Battle> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT b FROM Battle b WHERE (b.player1Id = ?1 OR b.player2Id = ?1) ORDER BY b.createdAt DESC")
    List<Battle> findUserBattles(Long userId);
    
    @Query("SELECT b FROM Battle b WHERE b.status = 'WAITING' AND b.player2Id IS NULL ORDER BY b.createdAt ASC")
    List<Battle> findAvailableBattles();
}
