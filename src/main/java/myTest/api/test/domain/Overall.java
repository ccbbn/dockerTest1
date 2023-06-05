package myTest.api.test.domain;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@RequiredArgsConstructor
public class Overall{

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String dataTime;
    private String informOverall;
    private String informCause;
    private String informGrade;
    private String informData;
    private String informCode;

    private String seoul;
    private String jeju;
    private String jeonnam;
    private String jenbuk;
    private String gwangju;
    private String gyeongnam;
    private String gyeongbuk;
    private String ulsan;
    private String daegu;
    private String busan;
    private String chungbuk;
    private String chungnam;
    private String sejong;
    private String daejeon;
    private String yeongdong;
    private String yeongseo;
    private String gyeonggiSouth;
    private String gyeonggiNorth;
    private String incheon;





}



