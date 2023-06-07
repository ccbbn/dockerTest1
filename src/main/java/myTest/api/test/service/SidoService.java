package myTest.api.test.service;
import lombok.RequiredArgsConstructor;
import myTest.api.test.domain.Sido;
import myTest.api.test.repository.SidoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SidoService {





    private final SidoRepository sidoRepository;

    public List<Sido> findAll() {
        return sidoRepository.findAll();
    }

    public Sido findLastByOrderByGPm10Value() { return sidoRepository.findLastByOrderByGPm10Value();}


    public Sido findLastByOrderByGPm25Value() { return sidoRepository.findLastByOrderByGPm25Value();}



    public Sido findLastByOrderByGwPm10Value() { return sidoRepository.findLastByOrderByGPm10Value();}


    public Sido findLastByOrderByGwPm25Value() { return sidoRepository.findLastByOrderByGPm25Value();}



}
