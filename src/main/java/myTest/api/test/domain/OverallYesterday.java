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
public class OverallYesterday {


        @javax.persistence.Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long Id;
        private String dataTime;
        private String informOverall;
        private String informCause;
        private String informGrade;
        private String informData;
        private String informCode;





}
