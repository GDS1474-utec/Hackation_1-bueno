package com.tropelcare.signal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TropelSignalRepository extends JpaRepository<TropelSignal, Long>, JpaSpecificationExecutor<TropelSignal> {
}
