package myTest.api.test.domain;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import myTest.api.test.repository.SidoStationRepository;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SIDO_ID")
    private Sido sido;


    private Double distanceDifference;

    public SidoStation(String addr, String x, String y, String stationName, List<Sido> sidoList) {
        this.addr = addr;
        this.x = x;
        this.y = y;
        this.stationName = stationName;

        // sidoList에서 stationName과 this.stationName이 일치하는 Sido 객체 찾기
        for (Sido sido : sidoList) {
            if (sido.getStationName().equals(this.stationName)) {
                this.sido = sido;
                sido.setSidoStation(this);
                break;
            }
        }

    }





}
