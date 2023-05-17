package myTest.api.test.repository;

import myTest.api.test.domain.Overall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OverallRepository extends JpaRepository<Overall, Long> {

    List<Overall> findAll();




}
