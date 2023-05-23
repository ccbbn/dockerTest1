package myTest.api.test.repository;

import myTest.api.test.domain.Sido;
import myTest.api.test.domain.SidoStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SidoStationRepository extends JpaRepository<SidoStation, Long> {

    List<SidoStation> findByAddrStartingWith(String addr);


}
