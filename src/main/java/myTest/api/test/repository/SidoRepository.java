package myTest.api.test.repository;


import myTest.api.test.domain.Sido;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface SidoRepository extends JpaRepository<Sido, Long> {
}
