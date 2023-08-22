package myTest.api.test.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import myTest.api.test.domain.Overall;

import myTest.api.test.domain.Sido;
import myTest.api.test.repository.OverallRepository;

import myTest.api.test.service.OverallService;
import myTest.api.test.service.SidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Controller
public class
OverallController {

    private final OverallService overallService;
    private final SidoService sidoService;




    @Transactional
    @GetMapping("/overall")
    public String api1(Model model) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=MRi2mirgR5H4KnX%2FSaac2Hh6O76YRJtsHTZ60S%2F5zu%2FNoV5kDjup632dozD9jmKy%2F1inJix1TfB%2F1ns%2FDkY76Q%3D%3D"); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("returnType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*xml 또는 json*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수(조회 날짜로 검색 시 사용 안함)*/
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호(조회 날짜로 검색 시 사용 안함)*/
        urlBuilder.append("&" + URLEncoder.encode("searchDate","UTF-8") + "=" + URLEncoder.encode("2023-08-22", "UTF-8")); /*통보시간 검색(조회 날짜 입력이 없을 경우 한달동안 예보통보 발령 날짜의 리스트 정보를 확인)*/
//       urlBuilder.append("&" + URLEncoder.encode("searchDate", "UTF-8") + "=" + URLEncoder.encode(LocalDate.now().format(DateTimeFormatter.ISO_DATE), "UTF-8"));
//
        urlBuilder.append("&" + URLEncoder.encode("InformCode", "UTF-8") + "=" + URLEncoder.encode("PM10", "UTF-8")); /*통보코드검색(PM10, PM25, O3)*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();


        String jsonString = sb.toString();


// Jackson ObjectMapper 객체 생성
        ObjectMapper objectMapper = new ObjectMapper();

// JSON 파싱
        JsonNode rootNode = objectMapper.readTree(jsonString);

// 원하는 데이터 추출
        JsonNode dataNode = rootNode.path("response").path("body").path("items"); // "response.body.items"에 해당하는 노드를 가져옴

        System.setOut(new PrintStream(System.out, true, "UTF-8"));
//        Overall overall;

        overallService.deleteAll();




        for (JsonNode itemNode : dataNode) {
            String dataTime = itemNode.path("dataTime").asText();
            String informOverall = itemNode.path("informOverall").asText();
            String informCause = itemNode.path("informCause").asText();
            String informGrade = itemNode.path("informGrade").asText();
            String informData = itemNode.path("informData").asText();
            String informCode = itemNode.path("informCode").asText();


            LocalDateTime condition1 = LocalDateTime.parse(dataTime.substring(0, 14), DateTimeFormatter.ofPattern("yyyy-MM-dd HH시"));

            Overall overall = new Overall();

            if ((condition1.getHour() > LocalDateTime.now().getHour() - 6) && (Objects.equals(informData, LocalDate.now().format(DateTimeFormatter.ISO_DATE)))) {

                overall.setDataTime(dataTime);
                overall.setInformOverall(informOverall);
                overall.setInformCause(informCause);
                overall.setInformGrade(informGrade);
                overall.setInformData(informData);
                overall.setInformCode(informCode);
                overallService.save(overall);


                String[] informGradeList = informGrade.split(",");

                overall.setSeoul(informGradeList[0].substring(informGradeList[0].length() - 2));
                overall.setJeju(informGradeList[1].substring(informGradeList[1].length() - 2));
                overall.setJeonnam(informGradeList[2].substring(informGradeList[2].length() - 2));
                overall.setJenbuk(informGradeList[3].substring(informGradeList[3].length() - 2));
                overall.setGwangju(informGradeList[4].substring(informGradeList[4].length() - 2));
                overall.setGyeongnam(informGradeList[5].substring(informGradeList[5].length() - 2));
                overall.setGyeongbuk(informGradeList[6].substring(informGradeList[6].length() - 2));
                overall.setUlsan(informGradeList[7].substring(informGradeList[7].length() - 2));
                overall.setDaegu(informGradeList[8].substring(informGradeList[8].length() - 2));
                overall.setBusan(informGradeList[9].substring(informGradeList[9].length() - 2));
                overall.setChungnam(informGradeList[10].substring(informGradeList[10].length() - 2));
                overall.setChungbuk(informGradeList[11].substring(informGradeList[11].length() - 2));
                overall.setSejong(informGradeList[12].substring(informGradeList[12].length() - 2));
                overall.setDaejeon(informGradeList[13].substring(informGradeList[13].length() - 2));
//                overall.setYeongdong(informGradeList[14].substring(informGradeList[14].length() - 2));
//                overall.setYeongseo(informGradeList[15].substring(informGradeList[15].length() - 2));
//                overall.setGyeonggiSouth(informGradeList[16].substring(informGradeList[16].length() - 2));
//                overall.setGyeonggiNorth(informGradeList[17].substring(informGradeList[17].length() - 2));
                overall.setIncheon(informGradeList[18].substring(informGradeList[18].length() - 2));

                List<Overall> overallList = overallService.findAll();
                model.addAttribute("overallList", overallList);


                Sido gPm10 = sidoService.findLastByOrderByGPm10Value();
                Sido gPm25 = sidoService.findLastByOrderByGPm25Value();


                if (gPm10.getGPm10Value() < 31 && overall.getInformCode().equals("pm10")) {
                    overall.setGyeonggi("좋음");
                } else if (gPm10.getGPm10Value() < 81) {
                    overall.setGyeonggi("보통");
                } else if (gPm10.getGPm10Value() < 151) {
                    overall.setGyeonggi("나쁨");
                } else {
                    overall.setGyeonggi("매우 나쁨");
                }


                if (gPm25.getGPm25Value() < 31 && overall.getInformCode().equals("pm25")) {
                    overall.setGyeonggi("좋음");
                } else if (gPm25.getGPm25Value() < 81) {
                    overall.setGyeonggi("보통");
                } else if (gPm25.getGPm25Value() < 151) {
                    overall.setGyeonggi("나쁨");
                } else {
                    overall.setGyeonggi("매우 나쁨");
                }



                Sido gwPm10 = sidoService.findLastByOrderByGwPm10Value();
                Sido gwPm25 = sidoService.findLastByOrderByGwPm25Value();


                if (gwPm10.getGwPm10Value() < 31 && overall.getInformCode().equals("pm10")) {
                    overall.setGangwon("좋음");
                } else if (gwPm10.getGwPm10Value() < 81) {
                    overall.setGangwon("보통");
                } else if (gwPm10.getGwPm10Value() < 151) {
                    overall.setGangwon("나쁨");
                } else {
                    overall.setGangwon("매우 나쁨");
                }


                if (gwPm25.getGwPm25Value() < 31 && overall.getInformCode().equals("pm25")) {
                    overall.setGangwon("좋음");
                } else if (gwPm25.getGwPm25Value() < 81) {
                    overall.setGangwon("보통");
                } else if (gwPm25.getGwPm25Value() < 151) {
                    overall.setGangwon("나쁨");
                } else {
                    overall.setGangwon("매우 나쁨");
                }


                List<Overall> pm10 = overallService.findInformCode("PM10");
                List<Overall> pm25 = overallService.findInformCode("PM25");

                model.addAttribute("pm10List", pm10);
                model.addAttribute("pm25List", pm25);

            } else System.out.println("못불러옴");

        }

        return "/main/koreaForecast";

        }





}
