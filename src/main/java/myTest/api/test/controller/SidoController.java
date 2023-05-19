package myTest.api.test.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jdk.jfr.Frequency;
import myTest.api.test.repository.SidoRepository;
import myTest.api.test.domain.Sido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;



@RestController
public class SidoController {

    private final SidoRepository sidoRepository;
    @Autowired
    public SidoController(SidoRepository sidoRepository) {
        this.sidoRepository = sidoRepository;
    }



//    @GetMapping("search")
//    public String Ser
//



    @PostConstruct
    @Transactional
    public void init() throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=MRi2mirgR5H4KnX%2FSaac2Hh6O76YRJtsHTZ60S%2F5zu%2FNoV5kDjup632dozD9jmKy%2F1inJix1TfB%2F1ns%2FDkY76Q%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml 또는 json*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("700", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        urlBuilder.append("&" + URLEncoder.encode("sidoName","UTF-8") + "=" + URLEncoder.encode("전국", "UTF-8")); /*시도 이름(전국, 서울, 부산, 대구, 인천, 광주, 대전, 울산, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주, 세종)*/
        urlBuilder.append("&" + URLEncoder.encode("ver","UTF-8") + "=" + URLEncoder.encode("1.0", "UTF-8")); /*버전별 상세 결과 참고*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(),"UTF-8"));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String jsonString = sb.toString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);
        JsonNode dataNode = rootNode.path("response").path("body").path("items"); // "response.body.items"에 해당하는 노드를 가져옴

        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        for (JsonNode itemNode : dataNode) {
            String sidoName = itemNode.path("sidoName").asText();
            String stationName = itemNode.path("stationName").asText();
            String dateTime = itemNode.path("dataTime").asText();
            String pm10Value = itemNode.path("pm10Value").asText();

            String pm25Value = itemNode.path("pm25Value").asText();
            String pm10Grade = itemNode.path("pm10Grade").asText();
            String pm25Grade = itemNode.path("pm25Grade").asText();

            Sido domain = new Sido();

            domain.setSidoName(sidoName);
            domain.setStationName(stationName);
            domain.setDataTime(dateTime);

            domain.setPm10Value(pm10Value);
            domain.setPm25Value(pm25Value);
            domain.setPm10Grade(pm10Grade);
            domain.setPm25Grade(pm25Grade);

            sidoRepository.save(domain);
        }
    }


}
