package myTest.api.test.repository;


import myTest.api.test.domain.Sido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SidoRepository extends JpaRepository<Sido, Long> {
    List<Sido> findAll();


    @Query("SELECT s FROM Sido s WHERE s.Id = 642")
    Sido findLastByOrderByGPm10Value();


    @Query("SELECT s FROM Sido s WHERE s.Id = 642")
    Sido findLastByOrderByGPm25Value();
}
