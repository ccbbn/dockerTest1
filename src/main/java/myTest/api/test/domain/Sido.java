package myTest.api.test.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Entity
@Data
@RequiredArgsConstructor
public class Sido {

    @jakarta.persistence.Id
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
