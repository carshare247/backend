package com.carpool;

import com.carpool.service.CarbonService;
import com.carpool.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParcelAndCarbonIntegrationTest {

    @Autowired
    private ParcelService parcelService;

    @Autowired
    private CarbonService carbonService;

    @Test
    void parcelAndCarbonServicesAreAvailable() {
        assertThat(parcelService).isNotNull();
        assertThat(carbonService).isNotNull();
    }
}
