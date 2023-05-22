package myTest.api.test.domain;


import jdk.jfr.Enabled;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import myTest.api.test.service.MyLocationService;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@RequiredArgsConstructor
public class MyLocation {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;
    String x;
    String y;


    public MyLocation(String x, String y) {
        this.x = x;
        this.y = y;

    }

}
