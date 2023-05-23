package myTest.api.test.service;
import lombok.RequiredArgsConstructor;
import myTest.api.test.domain.Sido;
import myTest.api.test.repository.SidoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SidoService {


    private final SidoRepository sidoRepository;

    public List<Sido> findAll() {
        return sidoRepository.findAll();
    }


}
