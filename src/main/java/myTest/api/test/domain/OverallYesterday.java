package myTest.api.test.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class OverallYesterday {

    @Entity
    @Data
    @RequiredArgsConstructor
    public class Overall{

        @jakarta.persistence.Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long Id;
        private String dataTime;
        private String informOverall;
        private String informCause;
        private String informGrade;
        private String informData;
        private String informCode;


    }


}
