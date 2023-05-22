package myTest.api.test.repository;


import myTest.api.test.domain.Sido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SidoRepository extends JpaRepository<Sido, Long> {

    Sido findByStationName(String stationName);

    Sido findBySidoName(String sidoName);




}
