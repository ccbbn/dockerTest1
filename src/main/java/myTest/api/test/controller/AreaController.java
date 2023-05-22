package myTest.api.test.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import myTest.api.test.domain.SidoStation;
import myTest.api.test.repository.AreaRepository;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import myTest.api.test.domain.Area;
@AllArgsConstructor
@Controller
public class AreaController {

    private final AreaRepository areaRepository;








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

//            Area area = new Area();


            //String sidoName, String stationName, String addr, String x, String y, String dataTime, String pm10Value, String pm25Value, String pm10Grade, String pm25Grade
//            areaRepository.save(new Area(sidoName,stationName,"서울 강남구 학동로 426 강남구청 별관 1동","127.0476845","37.517554",dateTime,pm10Value,pm25Value,pm10Grade,pm25Grade));
//
//
//
//











//            area.setSidoName(sidoName);
//            area.setStationName(stationName);
//            area.setDataTime(dateTime);
//            area.setPm10Value(pm10Value);
//            area.setPm25Value(pm25Value);
//            area.setPm10Grade(pm10Grade);
//            area.setPm25Grade(pm25Grade);
//
//            area.save(sido);
        }
    }

}
