package myTest.api.test.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import myTest.api.test.domain.Sido;
import myTest.api.test.repository.SidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Service
public class SidoService {

    private final SidoRepository sidoRepository;
    @Autowired
    public SidoService(SidoRepository sidoRepository) {
        this.sidoRepository = sidoRepository;
    }






}
