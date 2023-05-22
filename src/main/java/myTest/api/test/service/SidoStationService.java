package myTest.api.test.service;

import lombok.RequiredArgsConstructor;
import myTest.api.test.domain.Sido;
import myTest.api.test.domain.SidoStation;
import myTest.api.test.repository.SidoRepository;
import myTest.api.test.repository.SidoStationRepository;
import org.hibernate.usertype.LoggableUserType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SidoStationService {

    private final SidoStationRepository sidoStationRepository;
    private final SidoRepository sidoRepository;


    public SidoStation save(SidoStation sidoStation) {
        return sidoStationRepository.save(sidoStation);
    }

    public List<SidoStation> findByAddrStartingWith(String addr) {
        return sidoStationRepository.findByAddrStartingWith(addr);

    }

    public List<SidoStation> findStation(String stationName) {
        Sido sido = sidoRepository.findByStationName(stationName);
        List<SidoStation> sidoStationList = sidoStationRepository.findBySido(sido);

        return sidoStationList;

    }

}
