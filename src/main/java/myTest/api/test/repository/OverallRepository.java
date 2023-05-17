package myTest.api.test.repository;

import myTest.api.test.domain.Overall;
import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverallRepository extends JpaRepository<Overall, Long> {
}
