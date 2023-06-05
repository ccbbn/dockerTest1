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


    private String gPm25Value;
    private String gPm10Value;

    private String gwPm25Value;
    private String gwPm10Value;



    @OneToOne(mappedBy = "sido")
    private SidoStation sidoStation;



}
