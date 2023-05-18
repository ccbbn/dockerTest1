package myTest.api.test.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
@Data
@RequiredArgsConstructor
public class Sido {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String sidoName;
    private String stationName;
    private String dataTime;
    private String pm10Value;
    private String pm25Value;
    private String pm10Grade;
    private String pm25Grade;





}
