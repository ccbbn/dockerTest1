package myTest.api.test.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import myTest.api.test.domain.MyLocation;
import myTest.api.test.domain.SidoStation;
//import myTest.api.test.service.SidoService;
import myTest.api.test.service.MyLocationService;
import myTest.api.test.service.SidoStationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Controller
public class SidoStationController {

    private final SidoStationService sidoStationService;
    private final MyLocationService myLocationService;

    @GetMapping("findSidoStationNearBy")
    public String findArea(@RequestParam("area") String area, Model model) {

        int firstSpaceIndex = area.indexOf(" ");
        int secondSpaceIndex = area.indexOf(" ", firstSpaceIndex + 1);  // 두 번째 공백 문자의 인덱스
        String areaState = area.substring(0, secondSpaceIndex);

        List<SidoStation> sidoStations = sidoStationService.findByAddrStartingWith(areaState);



        //주소로 x, y좌표를 검색하고 인근 측정소까지의 거리를 구함.
        String apiURL = "http://api.vworld.kr/req/address";
        try {
            int responseCode = 0;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
           //area 주소
            String text_content = URLEncoder.encode(area.toString(), "utf-8");

            // post request
            String postParams = "service=address";
            postParams += "&request=getcoord";
            postParams += "&version=2.0";
            postParams += "&crs=EPSG:4326";
            postParams += "&address=" + text_content;
            postParams += "&arefine=true";
            postParams += "&simple=false";
            postParams += "&format=json";
            postParams += "&type=road";
            postParams += "&errorFormat=json";
            postParams += "&key=B4BEBCB2-2B94-38D5-88C3-72D2921ECAD7";

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            responseCode = con.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            con.disconnect();


            String jsonString = response.toString();


            // Jackson ObjectMapper 객체 생성
            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(jsonString);


            String x = rootNode.get("response").get("result").get("point").get("x").asText();
            String y = rootNode.get("response").get("result").get("point").get("y").asText();

            model.addAttribute("x",Double.parseDouble(x));
            model.addAttribute("y",Double.parseDouble(y));


            myLocationService.save(new MyLocation(x, y));

            List<Double> km = new ArrayList<>();
            for (SidoStation station : sidoStations) {
                double distanceInKm = distance(Double.parseDouble(station.getY()), Double.parseDouble(station.getX()), Double.parseDouble(y), Double.parseDouble(x), "kilo");
                distanceInKm = Math.round(distanceInKm * 100.0) / 100.0;
                km.add(distanceInKm);
                System.out.println(distanceInKm);
            }


            for (SidoStation station : sidoStations) {
                double distanceInKm = distance(Double.parseDouble(station.getY()), Double.parseDouble(station.getX()), Double.parseDouble(y), Double.parseDouble(x), "kilo");
                distanceInKm = Math.round(distanceInKm * 100.0) / 100.0;
                station.setDistanceDifference(distanceInKm);
                System.out.println(distanceInKm);
            }

            model.addAttribute("sidoStations", sidoStations);  // xx시 xx구 까지 일치한 거 보여줌
            model.addAttribute("area",area);








            model.addAttribute("km", km);
            System.out.println(km);


        } catch (Exception e) {
            e.printStackTrace();
        }

//        return "/main/areaForecast";
        return "/main/test";
    }





    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (Objects.equals(unit, "kilo")) {
            dist = dist * 1.609344;
        } else if(Objects.equals(unit, "meter")){
            dist = dist * 1609.344;
        }
        return (dist);
    }


    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


}
