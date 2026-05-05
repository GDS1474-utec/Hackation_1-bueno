package com.tropelcare.tropel;

import com.tropelcare.common.Climate;
import com.tropelcare.common.Species;
import com.tropelcare.exception.BusinessException;
import com.tropelcare.guardian.Guardian;
import com.tropelcare.guardian.GuardianRepository;
import com.tropelcare.sector.Sector;
import com.tropelcare.sector.SectorRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TropelServiceTest {

    @Test
    void registeringTropelInFullSectorThrowsBusinessExceptionThatMapsTo400() {
        TropelRepository tropelRepository = mock(TropelRepository.class);
        SectorRepository sectorRepository = mock(SectorRepository.class);
        GuardianRepository guardianRepository = mock(GuardianRepository.class);

        TropelService service = new TropelService(tropelRepository, sectorRepository, guardianRepository);

        Sector fullSector = new Sector();
        fullSector.setId(1L);
        fullSector.setSectorCode("SECTOR-FULL");
        fullSector.setClimate(Climate.RETRO_ARCADE);
        fullSector.setCapacity(1);
        fullSector.setCurrentLoad(1);
        fullSector.setStabilityLevel(100);
        fullSector.setCreatedAt(Instant.now());

        Guardian guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("team@gmail.com");
        guardian.setCreatedAt(Instant.now());

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(fullSector));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(tropelRepository.existsByName("ByteByte")).thenReturn(false);

        TropelRequestDTO request = new TropelRequestDTO(
                "ByteByte",
                Species.CHISPA,
                1L,
                1L
        );

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(tropelRepository, never()).save(any(Tropel.class));
    }
}
