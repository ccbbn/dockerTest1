package myTest.api.test.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import myTest.api.test.domain.Area;
import myTest.api.test.domain.SidoStation;
import myTest.api.test.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    public Area save(Area area) {
        return areaRepository.save(area);
    }






}