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

    private String dataTime;
    private String pm10Value;
    private String pm25Value;
    private String pm10Grade;
    private String pm25Grade;
    private String stationName;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SIDOSTATION_ID")
    private SidoStation sidoStation;


    public Sido(String sidoName, String dataTime, String pm10Value, String pm25Value, String pm10Grade, String pm25Grade) {
        this.sidoName = sidoName;
        this.dataTime = dataTime;
        this.pm10Value = pm10Value;
        this.pm25Value = pm25Value;
        this.pm25Grade = pm25Grade;
        this.pm10Grade = pm10Grade;


    }
}
