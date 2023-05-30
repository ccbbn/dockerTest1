package myTest.api.test.service;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import myTest.api.test.domain.Overall;
import myTest.api.test.repository.OverallRepository;

import org.springframework.stereotype.Service;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverallService {

    private final OverallRepository overallRepository;

    public List<Overall> findAll(){
        return overallRepository.findAll();
    }

    public void save(Overall overall){
        overallRepository.save(overall);
    }

    public void deleteAll() {overallRepository.deleteAll();}

}
