package myTest.api.test.repository;

import myTest.api.test.domain.SidoStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SidoStationRepository extends JpaRepository<SidoStation, Long> {


    List<SidoStation> findByAddrStartingWith(String addr);

}
