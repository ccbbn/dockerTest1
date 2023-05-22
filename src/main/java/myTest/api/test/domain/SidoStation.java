package myTest.api.test.domain;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import myTest.api.test.repository.SidoStationRepository;

import javax.annotation.PostConstruct;
import javax.persistence.*;

@Entity
@Data
@RequiredArgsConstructor
public class SidoStation {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String addr;
    private String x;
    private String y;
    private String stationName;

    @OneToOne(mappedBy = "sidoStation")
    private Sido sido;

    public SidoStation(String addr, String x, String y, String stationName) {
        this.addr = addr;
        this.x = x;
        this.y = y;
        this.stationName = stationName;

    }
}
