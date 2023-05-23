package myTest.api.test.repository;

import myTest.api.test.domain.Overall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverallRepository extends JpaRepository<Overall, Long> {

    List<Overall> findAll();

}
