package myTest.api.test.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;


@Entity
@Data
@RequiredArgsConstructor
public class Sido {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String sidoName;

    private String stationName;


    @OneToOne
    @JoinColumn(name = "sido_station_id")
    private SidoStation sidoStation;

    private String dataTime;
    private String pm10Value;
    private String pm25Value;
    private String pm10Grade;
    private String pm25Grade;





}
