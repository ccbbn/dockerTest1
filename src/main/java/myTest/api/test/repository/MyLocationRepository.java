package myTest.api.test.repository;

import myTest.api.test.domain.MyLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyLocationRepository extends JpaRepository<MyLocation, Long> {

}
