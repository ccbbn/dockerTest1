package myTest.api.test.service;

import lombok.AllArgsConstructor;
import myTest.api.test.domain.MyLocation;
import myTest.api.test.domain.SidoStation;
import myTest.api.test.repository.MyLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MyLocationService {

    private final MyLocationRepository myLocationRepository;

    public MyLocation save(MyLocation myLocation) {
        return myLocationRepository.save(myLocation);
    }

}
