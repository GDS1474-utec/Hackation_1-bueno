package com.tropelcare.sector;

import com.tropelcare.common.Climate;
import com.tropelcare.tropel.Tropel;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sectors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sector_code", columnNames = "sectorCode")
})
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sectorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Climate climate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentLoad;

    @Column(nullable = false)
    private Integer stabilityLevel;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "sector")
    private List<Tropel> tropels = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public Climate getClimate() {
        return climate;
    }

    public void setClimate(Climate climate) {
        this.climate = climate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(Integer currentLoad) {
        this.currentLoad = currentLoad;
    }

    public Integer getStabilityLevel() {
        return stabilityLevel;
    }

    public void setStabilityLevel(Integer stabilityLevel) {
        this.stabilityLevel = stabilityLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Tropel> getTropels() {
        return tropels;
    }

    public void setTropels(List<Tropel> tropels) {
        this.tropels = tropels;
    }
}
