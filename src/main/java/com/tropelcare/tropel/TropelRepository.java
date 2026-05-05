package com.tropelcare.tropel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TropelRepository extends JpaRepository<Tropel, Long>, JpaSpecificationExecutor<Tropel> {
    boolean existsByName(String name);
}
