package myTest.api.test.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import myTest.api.test.domain.MyLocation;
import myTest.api.test.domain.SidoStation;
//import myTest.api.test.service.SidoService;
import myTest.api.test.service.MyLocationService;
import myTest.api.test.service.SidoService;
import myTest.api.test.service.SidoStationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
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

            model.addAttribute("area", sidoStations);  // xx시 xx구 까지 일치한 거 보여줌









            model.addAttribute("km", km);
            System.out.println(km);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "/main/ttt";
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



//    @PostConstruct
//    public void initStation() {
//        sidoStationService.save(new SidoStation("서울 강남구 학동로 426 강남구청 별관 1동","127.0476845","37.517554","강남구"));
//        sidoStationService.save(new SidoStation("서울 강동구 구천면로 42길 59 천호1동 주민센터","127.1368025","37.5450092","강동구"));
//        sidoStationService.save(new SidoStation("서울 강북구 삼양로 139길 49 우이동 주민센터","127.0119581","37.6479649","강북구"));
//        sidoStationService.save(new SidoStation("서울 강서구 강서로 45 다길 71 화곡3동 푸른들청소년도서관","126.835184","37.5446716","강서구"));
//        sidoStationService.save(new SidoStation("서울 관악구 신림동길 14 신림동 주민센터","126.9271328","37.4874078","관악구"));
//        sidoStationService.save(new SidoStation("서울 광진구 광나루로 571 구의 아리수정수센터","127.093546","37.5454509","광진구"));
//        sidoStationService.save(new SidoStation("서울 구로구 가마산로 27길 45 구로고등학교","126.8901912","37.4988702","구로구"));
//        sidoStationService.save(new SidoStation("서울 금천구 금하로21길 20 시흥5동 주민센터","126.9083062","37.4524009","금천구"));
//        sidoStationService.save(new SidoStation("서울 노원구 상계로 118 상계2동 주민센터 (23길 17 노원구 원터행복발전소)","127.0678533","37.6574481","노원구"));
//        sidoStationService.save(new SidoStation("서울 도봉구 시루봉로2길 34 쌍문동청소년문화의집","127.029113","37.6541802","도봉구"));
//        sidoStationService.save(new SidoStation("서울 동대문구 천호대로13길 43 용두초등학교","127.0286537","37.5762732","동대문구"));
//        sidoStationService.save(new SidoStation("서울 동작구 사당로16아길 6 사당4동 주민센터","126.9716482","37.4809827","동작구"));
//        sidoStationService.save(new SidoStation("서울 마포구 포은로 6길 10 망원1동주민센터 옥상","126.9057046","37.5556","마포구"));
//        sidoStationService.save(new SidoStation("서울 서대문구 세검정로4길 32(홍제3동 주민센터)","126.9497269","37.5937711","서대문구"));
//        sidoStationService.save(new SidoStation("서울 서초구 신반포로15길 16 반포 2동 주민센터","126.9944877","37.5045885","서초구"));
//        sidoStationService.save(new SidoStation("서울 성동구 뚝섬로3길 18 성수1가1동주민센터","127.0496929","37.5420426","성동구"));
//        sidoStationService.save(new SidoStation("서울 성북구 삼양로2길 70 길음2동 주민센터","127.0273068","37.6066991","성북구"));
//        sidoStationService.save(new SidoStation("서울 송파구 백제고분로 236 삼전동 주민센터 (삼전동)","127.092525","37.5026972","송파구"));
//        sidoStationService.save(new SidoStation("서울 양천구 중앙로52길 56 신정4동 문화센터","126.8566192","37.5259775","양천구"));
//        sidoStationService.save(new SidoStation("서울 영등포구 당산로 123 영등포구청 (당산동3가)","126.8958164","37.5262063","영등포구"));
//        sidoStationService.save(new SidoStation("서울 용산구 한남대로 136 서울특별시중부기술교육원","127.0048417","37.5399702","용산구"));
//        sidoStationService.save(new SidoStation("서울 은평구 진흥로 215 (한국환경산업기술원 온실동2층 )","126.9328513","37.6098653","은평구"));
//        sidoStationService.save(new SidoStation("서울 종로구 종로35가길 19 종로5,6가 동 주민센터","127.0050604","37.57204","종로구"));
//        sidoStationService.save(new SidoStation("서울 중구 덕수궁길 15 시청서소문별관 3동","126.9750209","37.5642594","중구"));
//        sidoStationService.save(new SidoStation("서울 중랑구 용마산로 369 건강가정지원센터","127.0940302","37.5849024","중랑구"));
//        sidoStationService.save(new SidoStation("경기 여주시 가남읍 태평중앙1길 20 가남읍행정복지센터 옥상","127.5452341","37.2016372","가남읍"));
//        sidoStationService.save(new SidoStation("경기 가평군 가평읍 석봉로 181 가평군청 의회동","127.509623","37.8312368","가평"));
//        sidoStationService.save(new SidoStation("경기 광주시 중앙로 128 농협중앙회","127.257946","37.4111321","경안동"));
//        sidoStationService.save(new SidoStation("경기 평택시 고덕면 고덕국제2로 111 종덕초등학교 2층 옥상","127.0463337","37.0532329","고덕면"));
//        sidoStationService.save(new SidoStation("경기 수원시 권선구 서부로 1600 수원시도로교통관리사업소","126.9765127","37.2522353","고색동"));
//        sidoStationService.save(new SidoStation("경기 양주시 고읍남로 205 청소년문화의 집","127.0847443","37.7916882","고읍"));
//        sidoStationService.save(new SidoStation("경기 안산시 단원구 화랑로 387 안산시청","126.831598","37.3221683","고잔동"));
//        sidoStationService.save(new SidoStation("경기 의왕시 시청로 11 의왕시청 민원실","126.9690955","37.3447925","고천동"));
//        sidoStationService.save(new SidoStation("경기 김포시 고촌읍 신곡로 152 김포시상하수도사업소","126.7628428","37.6067998","고촌읍"));
//        sidoStationService.save(new SidoStation("경기 광주시 곤지암읍 광여로 59 곤지암읍 행정복지센터 옥상","127.3524042","37.3509449","곤지암"));
//        sidoStationService.save(new SidoStation("경기 안성시 공도읍 공도4로 8 공도읍행정복지센터 옥상","127.1726168","37.0011764","공도읍"));
//        sidoStationService.save(new SidoStation("경기 과천시 상하벌로 17 과천시 환경사업소","127.0036165","37.4491251","과천동"));
//        sidoStationService.save(new SidoStation("경기 수원시 영통구 광교중앙로 216 광교중앙공원 관리동 옥상 (하동)","127.0517784","37.2989615","광교동"));
//        sidoStationService.save(new SidoStation("경기 구리시 아차산로 439 구리시청 구리시의회","127.1297893","37.5943801","교문동"));
//        sidoStationService.save(new SidoStation("경기 남양주시 경춘로 1037 남양주시청 신관","127.216278","37.6357043","금곡동"));
//        sidoStationService.save(new SidoStation("경기 파주시 후곡로 13 교육문화회관 별관","126.7738978","37.7561664","금촌동"));
//        sidoStationService.save(new SidoStation("경기 용인시 기흥구 관곡로 95 기흥구청","127.1149242","37.2803475","기흥"));
//        sidoStationService.save(new SidoStation("경기 용인시 처인구 금령로 50 처인구청","127.2017321","37.234387","김량장동"));
//        sidoStationService.save(new SidoStation("경기 화성시 남양읍 남양성지로 192-5 남양읍행정복지센터","126.8238556","37.2114313","남양읍"));
//        sidoStationService.save(new SidoStation("경기 부천시 삼작로 109 신흥동주민센터 앞 도로변","126.7734902","37.520413","내동"));
//        sidoStationService.save(new SidoStation("경기 성남시 수정구 희망로 506번길 21 단대동행정복지센터","127.1554779","37.4493166","단대동"));
//        sidoStationService.save(new SidoStation("경기 군포시 산본로197번길 36 당동도서관","126.9451682","37.3537184","당동"));
//        sidoStationService.save(new SidoStation("경기 안산시 단원구 대부중앙로 97-9 대부동 행정복지센터","126.585139","37.2435288","대부동"));
//        sidoStationService.save(new SidoStation("경기 여주시 대신면 율촌1길 12-10 대신도서관 옥상","127.5844617","37.3714694","대신면"));
//        sidoStationService.save(new SidoStation("경기 시흥시 복지로 37 다다커뮤니티센터","126.7885296","37.4430613","대야동"));
//        sidoStationService.save(new SidoStation("경기 구리시 동구릉로 217-14 시립동구어린이집","127.1384921","37.6184026","동구동"));
//        sidoStationService.save(new SidoStation("경기 화성시 동탄반석로 87 동탄2동 행정복지센터","127.0723177","37.1969406","동탄"));
//        sidoStationService.save(new SidoStation("경기 용인시 처인구 모현읍 독점로 31-6 모현읍주민자치센터","127.242365","37.3295123","모현읍"));
//        sidoStationService.save(new SidoStation("경기 시흥시 솔고개길 33 목감공원 지상 (목감동)","126.8612839","37.3855949","목감동"));
//        sidoStationService.save(new SidoStation("경기 하남시 아리수로 531 미사2동 행정복지센터 (망월동)","127.1860595","37.5671734","미사"));
//        sidoStationService.save(new SidoStation("경기 시흥시 배곧4로 102 배곧도서관 옥상 (배곧동)","126.7337975","37.3734409","배곧동"));
//        sidoStationService.save(new SidoStation("경기 양주시 백석읍 꿈나무로 199 꿈나무도서관","126.9872623","37.7923682","백석읍"));
//        sidoStationService.save(new SidoStation("경기 용인시 처인구 백암면 백암로185번길 8-11 백암면주민자치센터","127.3746665","37.1636525","백암면"));
//        sidoStationService.save(new SidoStation("경기 남양주시 별내중앙로 148 화접초등학교 (별내동)","127.1158476","37.6528979","별내동"));
//        sidoStationService.save(new SidoStation("경기 과천시 코오롱로 53 문원초등학교","126.9943707","37.4238622","별양동"));
//        sidoStationService.save(new SidoStation("경기 동두천시 싸리말로 28 보산동행정복지센터","127.0612456","37.9177333","보산동"));
//        sidoStationService.save(new SidoStation("경기 성남시 수정구 성남대로 1416번길 22 복정정수장","127.1309817","37.4565728","복정동"));
//        sidoStationService.save(new SidoStation("경기 안산시 상록구 오목로7길 15 본오1동 작은도서관 (본오동)","126.8645192","37.2892665","본오동"));
//        sidoStationService.save(new SidoStation("경기 화성시 봉담읍 샘마을1길 8-4 화성시립봉담도서관 옥상","126.9490353","37.2194384","봉담읍"));
//        sidoStationService.save(new SidoStation("경기 안성시 시청길 25 안성시청 식당동","127.2799067","37.0079404","봉산동"));
//        sidoStationService.save(new SidoStation("경기 의왕시 덕영대로 166 부곡청소년문화의 집 (삼동)","126.9573035","37.3177111","부곡3동"));
//        sidoStationService.save(new SidoStation("경기 안산시 상록구 성호로 326 부곡동 행정복지센터","126.8609908","37.3317809","부곡동1"));
//        sidoStationService.save(new SidoStation("경기 안양시 동안구 시민대로 235 안양시청 민원실","126.9577103","37.3943954","부림동"));
//        sidoStationService.save(new SidoStation("경기 이천시 부발읍 무촌로 117 부발보건지소 옥상","127.4866877","37.2832822","부발읍"));
//        sidoStationService.save(new SidoStation("경기 평택시 중앙로 275 자원봉사센터","127.1126257","36.9910559","비전동"));
//        sidoStationService.save(new SidoStation("경기 김포시 돌문로 51 사우동행정복지센터","126.7168497","37.6193643","사우동"));
//        sidoStationService.save(new SidoStation("경기 군포시 청백리길 6 군포시청 별관","126.9350184","37.3618812","산본동"));
//        sidoStationService.save(new SidoStation("경기 성남시 중원구 둔촌대로 425 상대원1동행정복지센터","127.1643488","37.4331605","상대원동"));
//        sidoStationService.save(new SidoStation("경기 화성시 수노을중앙로 178 새솔동 행정복지센터 (새솔동)","126.8187217","37.2812493","새솔동"));
//        sidoStationService.save(new SidoStation("경기 화성시 서신면 궁평항로 1702 서신면사무소 옥상","126.7089997","37.1663801","서신면"));
//        sidoStationService.save(new SidoStation("경기 포천시 삼육사로 2186번길 11-15 선단보건지소","127.1593088","37.8534765","선단동"));
//        sidoStationService.save(new SidoStation("경기 가평군 설악면 한서로 8 설악면 문화센터 옥상","127.4946627","37.6759071","설악면"));
//        sidoStationService.save(new SidoStation("경기 부천시 경인옛로 73 소사어울마당 소향관","126.7999512","37.4800483","소사본동"));
//        sidoStationService.save(new SidoStation("경기 광명시 소하일로 7 소하도서관","126.8879971","37.4454627","소하동"));
//        sidoStationService.save(new SidoStation("경기 평택시 지산2로 113 송북동행정복지센터 (지산동)","127.0601605","37.0816383","송북동"));
//        sidoStationService.save(new SidoStation("경기 의정부시 민락로243번길 94 푸른마당 근린공원 인근 연결녹지(송양유치원과 민락천 사이) (민락동)","127.1065601","37.7478771","송산3동"));
//        sidoStationService.save(new SidoStation("경기 성남시 분당구 분당로 50 분당구청","127.1191436","37.3826246","수내동"));
//        sidoStationService.save(new SidoStation("경기 용인시 수지구 수지로 342번길3 풍덕천1동주민자치센터","127.0950036","37.3279183","수지"));
//        sidoStationService.save(new SidoStation("경기 시흥시 공단1대로 204 시화유통상가 27동","126.7246787","37.3372019","시화산단"));
//        sidoStationService.save(new SidoStation("경기 고양시 일산동구 위시티로 151 양일초등학교","126.8133239","37.6856252","식사동"));
//        sidoStationService.save(new SidoStation("경기 고양시 덕양구 신원2로 24 신원도서관","126.8859996","37.6665028","신원동"));
//        sidoStationService.save(new SidoStation("경기 하남시 대청로 10 하남시청 종합민원실","127.2149791","37.5393555","신장동"));
//        sidoStationService.save(new SidoStation("경기 수원시 팔달구 신풍로 23번길 68 선경도서관","127.0104231","37.2838632","신풍동"));
//        sidoStationService.save(new SidoStation("경기 안양시 만안구 안양로 384번길 50 안양2동 행정복지센터","126.9178145","37.405093","안양2동"));
//        sidoStationService.save(new SidoStation("경기 안양시 만안구 문예로36번길 16 안양아트센터 옥상 (안양동)","126.931645","37.3848278","안양8동"));
//        sidoStationService.save(new SidoStation("경기 평택시 안중읍 안현로 400 안중읍행정복지센터","126.9315681","36.9857436","안중"));
//        sidoStationService.save(new SidoStation("경기 양평군 양평읍 마유산로 17 양평군보건소 옥상","127.4866536","37.4969758","양평읍"));
//        sidoStationService.save(new SidoStation("경기 연천군 연천읍 차현로 58 연천군청 지역경제과","127.0761453","38.0964338","연천"));
//        sidoStationService.save(new SidoStation("경기 수원시 영통구 영통로 217번길 12 영통2동 행정복지센터","127.0562974","37.2468968","영통동"));
//        sidoStationService.save(new SidoStation("경기 남양주시 오남읍 진건오남로 806-34 오남읍사무소","127.2047623","37.6987718","오남읍"));
//        sidoStationService.save(new SidoStation("경기 오산시 경기동로 51 오산고용복지플러스센터","127.0773092","37.1588539","오산동"));
//        sidoStationService.save(new SidoStation("경기 부천시 성오로 172 오정어울마당 아트홀","126.7962865","37.528036","오정동"));
//        sidoStationService.save(new SidoStation("경기 광주시 오포읍 오포로859번길 29 오포1동 행정복지센터 옥상","127.2286077","37.3661156","오포1동"));
//        sidoStationService.save(new SidoStation("경기 남양주시 와부읍 도곡길 7 덕소중학교 후관동 옥상","127.2257451","37.5804515","와부읍"));
//        sidoStationService.save(new SidoStation("경기 양평군 용문면 용문로 395 용문도서관","127.5959562","37.4869657","용문면"));
//        sidoStationService.save(new SidoStation("경기 화성시 우정읍 쌍봉로 109-14 우정읍 행정복지센터","126.8154713","37.0896202","우정읍"));
//        sidoStationService.save(new SidoStation("경기 파주시 와석순환로 470 한국토지주택공사 파주사업본부","126.7563233","37.7233933","운정"));
//        sidoStationService.save(new SidoStation("경기 성남시 분당구 운중로 138번길 10 운중동행정복지센터","127.0778655","37.3905007","운중동"));
//        sidoStationService.save(new SidoStation("경기 안산시 단원구 부부로6길 17 근로자운동장 (원곡동)","126.793316","37.3369939","원곡동"));
//        sidoStationService.save(new SidoStation("경기 안산시 단원구 산단로 112 근로복지관 민주노총","126.7885211","37.3052997","원시동"));
//        sidoStationService.save(new SidoStation("경기 김포시 월곶면 군하로 263 월곶면 주민자치센터 옥상","126.5528427","37.7151334","월곶면"));
//        sidoStationService.save(new SidoStation("경기 의정부시 가능로152번길 14 의정부1동 주민센터","127.0475853","37.7463791","의정부1동"));
//        sidoStationService.save(new SidoStation("경기 의정부시 범골로 138 경기도로사업소","127.0404282","37.735617","의정부동"));
//        sidoStationService.save(new SidoStation("경기 용인시 처인구 이동읍 경기동로 673 이동읍주민자치센터","127.1961183","37.1410213","이동읍"));
//        sidoStationService.save(new SidoStation("경기 수원시 팔달구 효원로 241 수원시청","127.0287264","37.2636323","인계동"));
//        sidoStationService.save(new SidoStation("경기 포천시 일동면 화동로1099번길 30 일동면행정복지센터","127.3173559","37.9613669","일동면"));
//        sidoStationService.save(new SidoStation("경기 시흥시 시청로 20 미래키움어린이집 옥상 (장현동)","126.8033416","37.3800669","장현동"));
//        sidoStationService.save(new SidoStation("경기 이천시 장호원읍 오남리 27 재래시장 공영주차장","127.6296762","37.1142783","장호원읍"));
//        sidoStationService.save(new SidoStation("경기 연천군 전곡읍 은전로 45 전곡읍행정복지센터","127.0638252","38.028133","전곡"));
//        sidoStationService.save(new SidoStation("경기 시흥시 정왕대로 233번길 19 정왕보건지소","126.7401276","37.3466573","정왕동"));
//        sidoStationService.save(new SidoStation("경기 성남시 분당구 돌마로 242 정자3동행정복지센터","127.1195457","37.3607856","정자동"));
//        sidoStationService.save(new SidoStation("경기 고양시 일산서구 주엽로 104 주엽어린이도서관 (주엽동)","126.7564698","37.6684609","주엽동"));
//        sidoStationService.save(new SidoStation("경기 안성시 죽산면 남부길 60 동안성시민복지센터 옥상","127.4244542","37.0746749","죽산면"));
//        sidoStationService.save(new SidoStation("경기 부천시 심중로 121 책마루도서관","126.7701006","37.4939686","중2동"));
//        sidoStationService.save(new SidoStation("경기 여주시 여흥로11번길 26 중앙동행정복지센터","127.628921","37.2986368","중앙동(경기)"));
//        sidoStationService.save(new SidoStation("경기 남양주시 진접읍 금강로 1509-26 진접오남행정복지센터 옥상","127.1898234","37.7262686","진접읍"));
//        sidoStationService.save(new SidoStation("경기 이천시 영창로 163번길 28 평생학습관 어르신쉼터","127.4410925","37.2834272","창전동"));
//        sidoStationService.save(new SidoStation("경기 수원시 장안구 서부로 2066 성균관대학교 제2공학관","126.9764225","37.2947114","천천동"));
//        sidoStationService.save(new SidoStation("경기 광명시 시청로 20 광명시청 제1별관","126.864719","37.4782105","철산동"));
//        sidoStationService.save(new SidoStation("경기 화성시 동탄순환대로22길 13 예솔초등학교 (청계동)","127.1192026","37.1966496","청계동"));
//        sidoStationService.save(new SidoStation("경기 평택시 청북읍 안청로2길 60 청북문화센터","126.918133","37.0206753","청북읍"));
//        sidoStationService.save(new SidoStation("경기 파주시 파주읍 교육길 13 민방위교육장","126.8330053","37.830846","파주읍"));
//        sidoStationService.save(new SidoStation("경기 평택시 포승읍 평택항로184번길 38 한국산업단지공단 평택지사","126.8455125","36.9746832","평택항"));
//        sidoStationService.save(new SidoStation("경기 김포시 양촌읍 양곡2로30번길 46 김포독립운동기념관 옥상","126.6294169","37.6531775","한강신도시"));
//        sidoStationService.save(new SidoStation("경기 고양시 덕양구 화신로 148 행신배수지 가라산공원","126.8423162","37.6248878","행신동"));
//        sidoStationService.save(new SidoStation("경기 화성시 향남읍 발안로 89 향남읍 행정복지센터","126.9205897","37.1320562","향남읍"));
//        sidoStationService.save(new SidoStation("경기 안양시 동안구 경수대로 504 호계복합청사 옥상 (호계동)","126.9588026","37.3676705","호계동"));
//        sidoStationService.save(new SidoStation("경기 수원시 권선구 칠보로1번길 62 경기보건환경연구원 (금곡동)","126.9347435","37.2686269","호매실"));
//        sidoStationService.save(new SidoStation("경기 안산시 단원구 안산천남로 119 양지중학교","126.8334886","37.3049676","호수동"));
//        sidoStationService.save(new SidoStation("경기 남양주시 화도읍 비룡로 59 화도수동행정복지센터","127.3004583","37.6578044","화도읍"));
//        sidoStationService.save(new SidoStation("인천 서구 검단로502번길 15(마전동) 검단출장소 옥상","126.6614411","37.6020711","검단"));
//        sidoStationService.save(new SidoStation("인천 계양구 계양산로134번길 18(계산동) 계양도서관 옥상","126.7301247","37.5460843","계산"));
//        sidoStationService.save(new SidoStation("인천 남동구 은봉로 82 인천지방중소벤처기업청옥상","126.6997595","37.4092933","고잔"));
//        sidoStationService.save(new SidoStation("인천 남동구 구월말로 7(구월동) 구월4동 행정복지센터 옥상","126.7239597","37.44959","구월동"));
//        sidoStationService.save(new SidoStation("인천 강화군 길상면 강화동로 15-1 길상면보건지소 2층","126.4912395","37.640481","길상"));
//        sidoStationService.save(new SidoStation("인천 남동구 청능대로611번길 54(논현동) 근린공원","126.7269968","37.403733","논현"));
//        sidoStationService.save(new SidoStation("인천 연수구 원인재로 115(동춘동) 연수구의회 옥상","126.6782956","37.4100735","동춘"));
//        sidoStationService.save(new SidoStation("인천 부평구 부평대로88번길 19(부평동) 인천부평동초등학교 옥상","126.7237723","37.4998384","부평"));
//        sidoStationService.save(new SidoStation("인천 부평구 충선로 262 시냇물공원 (삼산동)","126.7357588","37.5126537","삼산"));
//        sidoStationService.save(new SidoStation("인천 남동구 서창남로 101 서창어울근린공원 지장 (서창동)","126.7461073","37.4274544","서창"));
//        sidoStationService.save(new SidoStation("인천 서구 거북로 116(석남동) 석남2동 행정복지센터 옥상","126.6745875","37.5024683","석남"));
//        sidoStationService.save(new SidoStation("인천 연수구 갯벌로 12 테크노파크 3층 옥상","126.6548945","37.3820847","송도"));
//        sidoStationService.save(new SidoStation("인천 동구 금곡로 67(송림동) 동구의회 옥상","126.6431167","37.4735217","송림"));
//        sidoStationService.save(new SidoStation("인천 강화군 송해면 전망대로 29(솔정리) 송해면사무소 옥상","126.4631384","37.7646954","송해"));
//        sidoStationService.save(new SidoStation("인천 미추홀구 독정안길 26 용정초등학교","126.6574746","37.4595937","숭의"));
//        sidoStationService.save(new SidoStation("인천 중구 서해대로 471(신흥동 2가) 인천보건환경연구원 옥상","126.6351175","37.4682847","신흥"));
//        sidoStationService.save(new SidoStation("인천 연수구 센트럴로 350 송도달빛축제공원 지장 (송도동)","126.6327881","37.4055914","아암"));
//        sidoStationService.save(new SidoStation("인천 서구 심곡로 98(심곡동) 인천 인재개발원 옥상","126.6807353","37.5406322","연희"));
//        sidoStationService.save(new SidoStation("인천 중구 하늘중앙로 132 영종하늘도서관 옥상 (중산동)","126.5637787","37.4947307","영종"));
//        sidoStationService.save(new SidoStation("인천 옹진군 영흥면 영흥로251번길 90 영흥 면사무소 2층 옥상 (영흥면사무소)","126.4835669","37.2560123","영흥"));
//        sidoStationService.save(new SidoStation("인천 중구 영종대로 85(운서동) 영종도서관 옥상","126.4886634","37.4956135","운서"));
//        sidoStationService.save(new SidoStation("인천 서구 고산후로121번길 7(원당동) 검단선사박물관 옥상","126.698696","37.5944418","원당"));
//        sidoStationService.save(new SidoStation("인천 미추홀구 구월남로 27 주안도서관 옥상 (주안동)","126.69289","37.4555306","주안"));
//        sidoStationService.save(new SidoStation("인천 서구 크리스탈로 131 수질정화시설관리동 2층 옥상","126.6357059","37.5347505","청라"));
//        sidoStationService.save(new SidoStation("인천 계양구 봉오대로600번길 14 효성도서관 (효성동)","126.7150579","37.5292344","효성"));
//        sidoStationService.save(new SidoStation("강원 철원군 갈말읍 삼부연로 51 철원군청","127.3130314","38.1466739","갈말읍"));
//        sidoStationService.save(new SidoStation("강원 속초시 중앙시장로 43 금호동주민센터 (중앙동)","128.588219","38.2068721","금호동"));
//        sidoStationService.save(new SidoStation("강원 삼척시 남양길 11(남양동 331-9) 남양동주민센터 3층 옥상","129.1684413","37.4425214","남양동1"));
//        sidoStationService.save(new SidoStation("강원 원주시 문막읍 건등로 11 문막읍행정복지센터 부지내 지상","127.8172807","37.3125311","문막읍"));
//        sidoStationService.save(new SidoStation("강원 원주시 배울로 215 (반곡동 1816-2, 배울체육공원 지상) (반곡동)","127.9770914","37.3358915","반곡동(명륜동)"));
//        sidoStationService.save(new SidoStation("강원 고성군 간성읍 수성로 160 상리 측정소","128.4636679","38.381143","상리"));
//        sidoStationService.save(new SidoStation("강원 춘천시 외솔길 17(석사동 322-1) 강원 개발공사 2층 옥상","127.7497028","37.8570493","석사동"));
//        sidoStationService.save(new SidoStation("강원 춘천시 사우4길 26 신사우도서관 2층 옥상 (우두동)","127.7282053","37.9055397","신사우동"));
//        sidoStationService.save(new SidoStation("강원 양구군 양구읍 관공서로 33 양구읍 측정소","127.9897376","38.109192","양구읍"));
//        sidoStationService.save(new SidoStation("강원 양양군 양양읍 남문로 39 양양군 군의회 옥상","128.6182088","38.0750048","양양읍"));
//        sidoStationService.save(new SidoStation("강원 영월군 영월읍 팔괴로 7-3 능동배수펌프장 부지내 지상","128.4553135","37.1725829","영월읍"));
//        sidoStationService.save(new SidoStation("강원 강릉시 경강로 2179(옥천동 327-2) 옥천동주민센터 2층 옥상","128.9029964","37.7601032","옥천동"));
//        sidoStationService.save(new SidoStation("강원 횡성군 우천면 우항1길 5-34 우천보건지소 옥상","128.0629177","37.4595259","우천면"));
//        sidoStationService.save(new SidoStation("강원 인제군 인제읍 비봉로44번길 93 인제군기상관측소","128.1669233","38.06015","인제읍"));
//        sidoStationService.save(new SidoStation("강원 정선군 정선읍 정선로 1331 정선읍행정복지센터 옥상","128.6617003","37.3798549","정선읍"));
//        sidoStationService.save(new SidoStation("강원 강릉시 주문진읍 항구로 19 주문진보건출장소","128.8231975","37.8937578","주문진읍"));
//        sidoStationService.save(new SidoStation("강원 원주시 충정길 12(학성동 206-6) 중앙동주민센터 3층 옥상","127.9474558","37.3527688","중앙동(강원)"));
//        sidoStationService.save(new SidoStation("강원 춘천시 중앙로길 135(중앙로 3가 67-1) 춘천시보건소 3층 옥상","127.7301589","37.8812707","중앙로"));
//        sidoStationService.save(new SidoStation("강원 원주시 지정면 기업도시로 200 원주의료기기테크노벨리 부지 내 주차장","127.873421","37.3717441","지정면"));
//        sidoStationService.save(new SidoStation("강원 동해시 천곡로 77(천곡동 806) 동해시의회 2층 옥상","129.1140747","37.5248249","천곡동"));
//        sidoStationService.save(new SidoStation("강원 평창군 평창읍 종부로 61(종부리 504) 평창군보건의료원 2층 옥상","128.3890288","37.360467","평창읍"));
//        sidoStationService.save(new SidoStation("강원 홍천군 홍천읍 연봉동로 27 홍천기상관측소","127.8802509","37.6837588","홍천읍"));
//        sidoStationService.save(new SidoStation("강원 화천군 화천읍 화천새싹길 45 건설방재과 옥상","127.7081592","38.1061001","화천읍"));
//        sidoStationService.save(new SidoStation("강원 태백시 태붐로 21 태백시청 부지 내 지상 (황지동)","128.9860986","37.1639662","황지동"));
//        sidoStationService.save(new SidoStation("강원 횡성군 횡성읍 중앙로 30 친환경급식지원센터","127.9829588","37.4897819","횡성읍"));
//        sidoStationService.save(new SidoStation("충남 공주시 봉황로 1 (공주시의회 옥상)","127.1190543","36.4467414","공주"));
//        sidoStationService.save(new SidoStation("충남 금산군 금산읍 비호로 69 금산읍사무소 별관 1층 옥상","127.4904927","36.1072461","금산읍"));
//        sidoStationService.save(new SidoStation("충남 홍성군 홍북읍 홍예공원로 8 충남보건환경연구원 부지내 1층","126.6676167","36.6531388","내포"));
//        sidoStationService.save(new SidoStation("충남 논산시 시민로 389 (취암/부창동 행정복지센터 2층 옥상)","127.0870759","36.1992309","논산"));
//        sidoStationService.save(new SidoStation("충남 당진시 시청1로 1 당진시청사","126.6455722","36.889408","당진시청사"));
//        sidoStationService.save(new SidoStation("충남 서산시 대산읍 충의로 1942 대산종합시장 옥상","126.4339459","36.9380064","대산리"));
//        sidoStationService.save(new SidoStation("충남 보령시 중앙로 142-16 (대천2동 주민센터 옥상)","126.5897997","36.3533925","대천2동"));
//        sidoStationService.save(new SidoStation("충남 아산시 도고면 기곡리 296-4 기곡1리 마을회관 지상","126.8887297","36.7625275","도고면"));
//        sidoStationService.save(new SidoStation("충남 서산시 대산읍 평신1로(독곶리) 한국수자원공사 대산산업용수센터 저수동 옥상","126.3783547","36.9879722","독곶리"));
//        sidoStationService.save(new SidoStation("충남 서산시 중앙로 38-1 서산초등학교 (동문동)","126.4548825","36.7803107","동문동"));
//        sidoStationService.save(new SidoStation("충남 아산시 둔포면 중앙공원로 43 둔포면 측정소","127.0588549","36.9194613","둔포면"));
//        sidoStationService.save(new SidoStation("충남 아산시 모종동 573-2 보건소옥상","127.0144179","36.7826765","모종동"));
//        sidoStationService.save(new SidoStation("충남 아산시 배방읍 배방로 38 배방읍사무소 옥상","127.0536385","36.773704","배방읍"));
//        sidoStationService.save(new SidoStation("충남 천안시 백석동 555-57번지(백성농공단지) 백성농공단지","127.1109811","36.825107","백석동"));
//        sidoStationService.save(new SidoStation("충남 부여군 부여읍 사비로 36 (부여읍행정복지센터 옥상)","126.9111654","36.2755268","부여읍"));
//        sidoStationService.save(new SidoStation("충남 예산군 삽교읍 두리3길 33 삽교읍 행정복지센터 옥상","126.7393169","36.6881449","삽교읍"));
//        sidoStationService.save(new SidoStation("충남 서천군 서면 서인로 761 서면보건지소옥상","126.5493932","36.1529175","서면"));
//        sidoStationService.save(new SidoStation("충남 서천군 서천읍 서천로14번길 20 서천읍 문예의전당 주차장","126.698364","36.0763402","서천읍"));
//        sidoStationService.save(new SidoStation("충남 천안시 서북구 성거읍 천흥8길 7 천흥산업단지 내 족구장 지상","127.2073744","36.8834717","성거읍"));
//        sidoStationService.save(new SidoStation("충남 논산시 성동면 산업단지로5길 73-28 논산지방산업단지 기숙사 옥상","127.0438841","36.21508","성동면"));
//        sidoStationService.save(new SidoStation("충남 서산시 성연면 마루들길 15 보건지소 옥상","126.460854","36.8390495","성연면"));
//        sidoStationService.save(new SidoStation("충남 천안시 동남구 복자1길 24 문성어린이집 문성시립보육시설 옥상","127.1521278","36.8142241","성황동"));
//        sidoStationService.save(new SidoStation("충남 당진시 송산면 유곡로 342-27 공공하폐수처리시설","126.7163035","36.9764562","송산면"));
//        sidoStationService.save(new SidoStation("충남 아산시 송악면 송악로 790 송악면행정복지센터","127.0089909","36.7316709","송악면"));
//        sidoStationService.save(new SidoStation("충남 천안시 동남구 천안천변길 127 천안시 맑은물사업소 하수처리장 (신방동)","127.1197879","36.7833316","신방동"));
//        sidoStationService.save(new SidoStation("충남 계룡시 엄사면 번영7길 17 엄사도서관 옥상","127.2379177","36.2870047","엄사면"));
//        sidoStationService.save(new SidoStation("충남 논산시 연무읍 안심로 50 연무읍사무소 옥상","127.0989339","36.1250214","연무읍"));
//        sidoStationService.save(new SidoStation("충남 예산군 예산읍 신흥길 63 (다목적 노인회관 옥상)","126.8488847","36.6774314","예산군"));
//        sidoStationService.save(new SidoStation("충남 태안군 원북면 상리길 17-4 원북면 보건지소 옥상","126.2571799","36.8242536","원북면"));
//        sidoStationService.save(new SidoStation("충남 태안군 이원면 분지길 14 (이원면사무소 1층 나동 옥상)","126.2806874","36.8696403","이원면"));
//        sidoStationService.save(new SidoStation("충남 아산시 인주면 인주산단로 23-28 인주면 측정소","126.8791992","36.873161","인주면"));
//        sidoStationService.save(new SidoStation("충남 서천군 장항읍 장산로 297 한국철도시설공단 부지내, 공원로 지상 1층","126.6930489","36.0083654","장항읍"));
//        sidoStationService.save(new SidoStation("충남 청양군 정산면 칠갑산로 1861 정산커뮤니티센터","126.9455821","36.4150007","정산면"));
//        sidoStationService.save(new SidoStation("충남 보령시 주교면 울계큰길 396 주교면사무소 옥상","126.5693382","36.3908904","주교면"));
//        sidoStationService.save(new SidoStation("충남 청양군 청양읍 칠갑산로7길 54 (청양군 보건의료원 옥상)","126.8048347","36.4550845","청양읍"));
//        sidoStationService.save(new SidoStation("충남 공주시 탄천면 안터새말길 34 안영1리 경로당 옥상","127.0672063","36.3086682","탄천면"));
//        sidoStationService.save(new SidoStation("충남 태안군 태안읍 군청6길 태안군장애인복지관","126.3015066","36.7466994","태안읍"));
//        sidoStationService.save(new SidoStation("충남 당진시 합덕읍 합덕리 344 합덕리","126.7858865","36.790494","합덕읍"));
//        sidoStationService.save(new SidoStation("충남 홍성군 홍성읍 내포로 136번길 29 (느티나무 어린이집 옥상)","126.6550705","36.5976765","홍성읍"));
//        sidoStationService.save(new SidoStation("대전 유성구 테크노중앙로 88 동화울수변공원 내 (관평동)","127.3944622","36.4245465","관평동"));
//        sidoStationService.save(new SidoStation("대전 유성구 대학로 407 보건환경연구원 보건환경연구원","127.3738902","36.3727364","구성동"));
//        sidoStationService.save(new SidoStation("대전 유성구 노은동로 87번길 89(노은1동 주민센터) 노은1동 주민센터 3층 옥상","127.3184197","36.368197","노은동"));
//        sidoStationService.save(new SidoStation("대전 동구 동구청로 36 남부여성가족원 (대성동)","127.4602234","36.3020671","대성동"));
//        sidoStationService.save(new SidoStation("대전 서구 둔산서로 84(근로자 종합복지회관) 근로자 종합복지회관(3층 옥상)","127.383475","36.3542928","둔산동"));
//        sidoStationService.save(new SidoStation("대전 중구 보문로 20번길 38(문창동 주민센터) 문창동 주민센터","127.4379199","36.3162222","문창동"));
//        sidoStationService.save(new SidoStation("대전 대덕구 문평동로 18번길 34(문평동 119안전센터) 문평동 119안전센터","127.3967192","36.4456016","문평동"));
//        sidoStationService.save(new SidoStation("대전 유성구 도안대로 398 대전역사박물관 (상대동)","127.3347185","36.3359884","상대동(대전)"));
//        sidoStationService.save(new SidoStation("대전 동구 계족로 368번길 70(성남동 주민센터) 성남동 주민센터","127.4374825","36.3445464","성남동1"));
//        sidoStationService.save(new SidoStation("대전 대덕구 대전로 1331번길 75(태아산업(주)) 태아산업(주)","127.4175908","36.3722905","읍내동"));
//        sidoStationService.save(new SidoStation("대전 서구 정림동로 10(정림동 주민센터) 정림동 주민센터","127.3667199","36.3044924","정림동"));
//        sidoStationService.save(new SidoStation("충북 청주시 상당구 가덕면 보청대로 4650 가덕면사무소 앞쪽 부지","127.5483015","36.5534185","가덕면"));
//        sidoStationService.save(new SidoStation("충북 괴산군 감물면 충민로신대길 13 감물면사무소 지상","127.8749584","36.8367276","감물면"));
//        sidoStationService.save(new SidoStation("충북 괴산군 괴산읍 서부리 377-1 반석아파트 앞 지상","127.7893038","36.8075704","괴산읍"));
//        sidoStationService.save(new SidoStation("충북 단양군 단성면 충혼로 52-1 단성보건지소 주차장(지면)","128.3207864","36.9391124","단성면"));
//        sidoStationService.save(new SidoStation("충북 단양군 단양읍 별곡6길 26 공설운동장 공중화장실 옥상","128.3661249","36.9873016","단양읍"));
//        sidoStationService.save(new SidoStation("충북 진천군 덕산읍 대월로 90 혁신도시공원 관리사무소 옥상","127.5390637","36.9018387","덕산읍"));
//        sidoStationService.save(new SidoStation("충북 증평군 도안면 문화마을길 8 도안면사무소 옥상","127.6128398","36.813246","도안면"));
//        sidoStationService.save(new SidoStation("충북 단양군 매포읍 평동리 1274(평동33길 3),(매포 보건지소 마당) 매포보건지소 지상","128.29579","37.0332313","매포읍"));
//        sidoStationService.save(new SidoStation("충북 보은군 보은읍 이평리 244 보은군 스포츠파크","127.7313002","36.4841258","보은읍"));
//        sidoStationService.save(new SidoStation("충북 청주시 청원구 사천동 233-223번지(사뜸로 61번길 88-14), 청주청원도서관 옥상(2층) 청주청원도서관 옥상","127.4749408","36.6662918","사천동"));
//        sidoStationService.save(new SidoStation("충북 청주시 서원구 원흥로 81 청주지방법원 옆 주차장 지상 (산남동)","127.4685923","36.61339","산남동"));
//        sidoStationService.save(new SidoStation("충북 충주시 살미면 세성양지말길 41 살미면 행정복지센터 주차장 옆","127.9645853","36.9053048","살미면"));
//        sidoStationService.save(new SidoStation("충북 음성군 소이면 소이로 409 소이면전천후게이트볼장 공터","127.7576921","36.923537","소이면"));
//        sidoStationService.save(new SidoStation("충북 청주시 흥덕구 직지대로 393(송정동) 충북문화재연구원 옥상","127.4368063","36.6447051","송정동(봉명동)"));
//        sidoStationService.save(new SidoStation("충북 영동군 영동읍 계산로2길 25 레인보우도서관 옥상","127.7777878","36.1727547","영동읍"));
//        sidoStationService.save(new SidoStation("충북 제천시 청풍호로8길 7 비점오염원 관리시설 뒤편 공터 (영천동)","128.2008578","37.1241124","영천동"));
//        sidoStationService.save(new SidoStation("충북 청주시 흥덕구 오송읍 오송생명로 150 오송읍 사무소 별관 지상","127.3251263","36.6321208","오송읍"));
//        sidoStationService.save(new SidoStation("충북 청주시 청원구 오창읍 오창대로 197 오창중앙공원내","127.4174052","36.7141391","오창읍"));
//        sidoStationService.save(new SidoStation("충북 옥천군 옥천읍 중앙로 99(옥천군청 통합관제 센터 옥상) 옥천군청 통합관제 센터 옥상","127.5713985","36.3064919","옥천읍"));
//        sidoStationService.save(new SidoStation("충북 청주시 상당구 교동로139번길 20 라일락소공원 지상 (용담동)","127.5040301","36.6360189","용담동"));
//        sidoStationService.save(new SidoStation("충북 청주시 상당구 용암동 1590(중흥로 29), 용암1동 주민센터 3층 옥상 용암1동 주민센터 옥상","127.5082931","36.6127223","용암동"));
//        sidoStationService.save(new SidoStation("충북 음성군 음성읍 중앙로 49 읍성보건소 옥상","127.6890142","36.9283425","음성읍"));
//        sidoStationService.save(new SidoStation("충북 제천시 장락동 672-8(내제로 318),(시립도서관 주차장 옆, 화단) 의병도서관 지상","128.2207481","37.1451907","장락동"));
//        sidoStationService.save(new SidoStation("충북 충주시 중앙탑면 기업도시로 237 서충주 국공립어린이집 측면부지","127.8226101","37.0167149","중앙탑면"));
//        sidoStationService.save(new SidoStation("충북 증평군 증평읍 남하용강로 16 증평자전거공원 내 부지","127.6043562","36.7673116","증평읍"));
//        sidoStationService.save(new SidoStation("충북 진천군 진천읍 상산로 65 읍내리 370-1 진천읍사무소 옥상","127.4412334","36.8549637","진천읍"));
//        sidoStationService.save(new SidoStation("충북 제천시 청풍면 청풍호로 2115 청풍면행정복지센터 옆쪽 공터","128.1686128","37.0008403","청풍면"));
//        sidoStationService.save(new SidoStation("충북 충주시 칠금동 874번지(칠금동 주민센터 옥상)(칠금 중랑로 37) 칠금금릉동 주민센터 옥상","127.9192365","36.9820983","칠금동"));
//        sidoStationService.save(new SidoStation("충북 충주시 호암동 562(충주시 청소년수련원 옥상)(중원대로 3324) 청소년수련원 옥상","127.9271952","36.9641522","호암동"));
//        sidoStationService.save(new SidoStation("충북 영동군 황간면 남성리 185 황간근린공원","127.913176","36.2294759","황간면"));
//        sidoStationService.save(new SidoStation("세종특별자치시 부강면 부강외천로 20 문화복지회관 옥상","127.3703943","36.5269272","부강면"));
//        sidoStationService.save(new SidoStation("세종특별자치시 조치원읍 군청로 87-16(신흥동) 세종특별자치시 조치원청사 옥상","127.2918651","36.5925544","신흥동"));
//        sidoStationService.save(new SidoStation("세종특별자치시 보듬3로 114 아름동커뮤니티센터 옥상 (아름동)","127.2468785","36.5123597","아름동"));
//        sidoStationService.save(new SidoStation("세종특별자치시 전의면 운주산로 1270 행정복지센터 지상","127.1957588","36.6812977","전의면"));
//        sidoStationService.save(new SidoStation("세종특별자치시 누리로 27 첫마을 6단지 관리사무소 옥상 (한솔동, 첫마을6단지)","127.2522974","36.4736055","한솔동"));
//        sidoStationService.save(new SidoStation("부산 부산진구 개금온정로17번길 51 개금3동 어린이놀이터 지상 (개금동)","129.0226649","35.1551975","개금동"));
//        sidoStationService.save(new SidoStation("부산 중구 광복로 55번길 10 광복동주민센터 옥상","129.030398","35.0999576","광복동"));
//        sidoStationService.save(new SidoStation("부산 수영구 광안로21번가길 57 한바다중학교 옥상","129.117914","35.1571324","광안동"));
//        sidoStationService.save(new SidoStation("부산 기장군 기장읍 읍내로 69 기장초등학교 옥상","129.2120212","35.2460214","기장읍"));
//        sidoStationService.save(new SidoStation("부산 강서구 녹산산단 382로 49번길 39 부산환경공단 녹산사업소","128.8628791","35.0882582","녹산동"));
//        sidoStationService.save(new SidoStation("부산 사하구 제석로 41 낙동초등학교 운동장 동편 화단 (당리동)","128.9731566","35.1055776","당리동"));
//        sidoStationService.save(new SidoStation("부산 서구 대신로 150 부산국민체육센터 옥상","129.0156968","35.1173164","대신동"));
//        sidoStationService.save(new SidoStation("부산 남구 수영로 196번길 80 부산공업고등학교 공동실습관 옥상","129.0875481","35.1301869","대연동"));
//        sidoStationService.save(new SidoStation("부산 강서구 체육공원로 43 강서체육공원 (대저1동)","128.9715289","35.2095306","대저동"));
//        sidoStationService.save(new SidoStation("부산 북구 덕천2길 10 덕천초등학교 운동장 남쪽 화단","129.0067693","35.2083593","덕천동"));
//        sidoStationService.save(new SidoStation("부산 사상구 삼덕로 83 덕포초등학교 운동장 북서쪽 화단 (덕포동)","128.9857605","35.1739008","덕포동"));
//        sidoStationService.save(new SidoStation("부산 동래구 명장로 32 명장1동 주민센터 옥상","129.1042446","35.2046703","명장동"));
//        sidoStationService.save(new SidoStation("부산 강서구 명지동 3513-3 봄뜰공원 (명지동)","128.9258587","35.1055695","명지동"));
//        sidoStationService.save(new SidoStation("부산 금정구 부곡로156번길 7 부곡2동 주민센터 옥상 (부곡동)","129.0926992","35.2298332","부곡동"));
//        sidoStationService.save(new SidoStation("부산 동구 구청로 1 동구청 지상","129.0453821","35.129296","수정동"));
//        sidoStationService.save(new SidoStation("부산 연제구 중앙대로 1001 부산청 녹음광장 창고","129.0753359","35.1802056","연산동"));
//        sidoStationService.save(new SidoStation("부산 기장군 정관읍 용수로 4 정관읍사무소 옥상","129.1800598","35.3256476","용수리"));
//        sidoStationService.save(new SidoStation("부산 남구 이기대공원로 11 부산환경공단 남부사업소 북쪽 (용호동)","129.1159964","35.1260072","용호동"));
//        sidoStationService.save(new SidoStation("부산 사하구 장림로 161번길 2 장림1동주민센터 옥상","128.9668795","35.0830296","장림동"));
//        sidoStationService.save(new SidoStation("부산 해운대구 센텀동로 191 동부하수처리장 (재송동)","129.1201122","35.1829726","재송동"));
//        sidoStationService.save(new SidoStation("부산 부산진구 전포대로209번길 26 놀이마루 운동장 남편 (전포동)","129.0633827","35.1559557","전포동"));
//        sidoStationService.save(new SidoStation("부산 해운대구 양운로 91 좌1동주민센터 옥상","129.1742075","35.1708686","좌동"));
//        sidoStationService.save(new SidoStation("부산 금정구 청룡로 25 청룡노포동주민센터 옥상","129.0898684","35.2752428","청룡동"));
//        sidoStationService.save(new SidoStation("부산 영도구 청학남로13번길 18 청학동 어울림마당 (청학동)","129.0594867","35.0907471","청학동"));
//        sidoStationService.save(new SidoStation("부산 영도구 전망로 24 태종대유원지관리사무소 3층","129.0798267","35.0596948","태종대"));
//        sidoStationService.save(new SidoStation("부산 사상구 대동로 205 학장초등학교 옥상","128.9837802","35.1461991","학장동"));
//        sidoStationService.save(new SidoStation("부산 북구 용당로16번길 22 장미원 (화명동)","129.0089291","35.2329041","화명동"));
//        sidoStationService.save(new SidoStation("부산 금정구 금사로 217 회동마루 (회동동)","129.1206275","35.2300827","회동동"));
//        sidoStationService.save(new SidoStation("울산 북구 호수중앙로 14 농소운동장 주차장 (창평동)","129.3587711","35.6217799","농소동"));
//        sidoStationService.save(new SidoStation("울산 동구 대송5길 10 대송동주민센터 옥상","129.4183398","35.5031687","대송동"));
//        sidoStationService.save(new SidoStation("울산 울주군 온산읍 덕신로 229 고려아연(주) 사택 주차장","129.3067209","35.4338654","덕신리"));
//        sidoStationService.save(new SidoStation("울산 남구 대학로 147번길 38 무거동주민센터","129.260698","35.5509491","무거동"));
//        sidoStationService.save(new SidoStation("울산 울주군 범서읍 당앞로 14-50 천상정수사업소","129.2235362","35.5701784","범서읍"));
//        sidoStationService.save(new SidoStation("울산 남구 처용로 260-37 대경기계기술","129.3398586","35.4963146","부곡동(울산)"));
//        sidoStationService.save(new SidoStation("울산 울주군 삼남읍 서향교1길 67-12 울주군보건소 옥상","129.1137405","35.5583629","삼남읍"));
//        sidoStationService.save(new SidoStation("울산 남구 삼산중로 131번길 36 삼산동 주민센터 옥상","129.3319472","35.5444688","삼산동"));
//        sidoStationService.save(new SidoStation("울산 울주군 청량면 신덕하3길 5 청량면사무소 옥상","129.30595","35.4931533","상남리"));
//        sidoStationService.save(new SidoStation("울산 중구 새즈믄해거리 28 농협 성남동 지점","129.3206632","35.556427","성남동"));
//        sidoStationService.save(new SidoStation("울산 북구 송내14길 41 송정동 행정복지센터 (화봉동)","129.36518","35.5922894","송정동 대기오염측정소"));
//        sidoStationService.save(new SidoStation("울산 남구 봉월로 20번길 9 신정2동주민센터 옥상","129.3078132","35.5346564","신정동"));
//        sidoStationService.save(new SidoStation("울산 남구 대암로 90번길 27 울산세관","129.3261827","35.5285793","야음동"));
//        sidoStationService.save(new SidoStation("울산 중구 종가로 560 약사고등학교","129.3359582","35.5735274","약사동"));
//        sidoStationService.save(new SidoStation("울산 남구 부두로 9 현대자동차 엔진공장","129.3661642","35.5150642","여천동(울산)"));
//        sidoStationService.save(new SidoStation("울산 울주군 웅촌면 새초천길 12 웅촌운동장 주차장","129.2083361","35.4613982","웅촌면"));
//        sidoStationService.save(new SidoStation("울산 동구 진성4길 45 주민센터 3층 옥상 (전하동)","129.4266298","35.5088572","전하동"));
//        sidoStationService.save(new SidoStation("울산 울주군 온산읍 산암로 94 풍산금속 야외 주차장","129.3414978","35.4364313","화산리"));
//        sidoStationService.save(new SidoStation("울산 북구 염포로 290 효문배수펌프장 옥상","129.3705223","35.5600075","효문동"));
//        sidoStationService.save(new SidoStation("대구 중구 남산로2길 125 대구명덕초등학교 (남산동)","128.5894892","35.8583277","남산1동"));
//        sidoStationService.save(new SidoStation("대구 서구 서대구로3길 46 내당4동 행정복지센터 3층 옥상 (내당동)","128.5517939","35.8590204","내당동"));
//        sidoStationService.save(new SidoStation("대구 달성군 다사읍 매곡로12길 37 다사읍주민자치센터","128.4573047","35.8648162","다사읍"));
//        sidoStationService.save(new SidoStation("대구 남구 성당로30길55 성명초등학교별관3층옥상 성명초등학교 별관 3층 옥상","128.5711635","35.8457616","대명동"));
//        sidoStationService.save(new SidoStation("대구 수성구 만촌2동 934 (동원초등학교)(국채보상로 1000) 동원초등학교","128.6396481","35.8655971","만촌동"));
//        sidoStationService.save(new SidoStation("대구 달서구 구마로26길 62 본동행정복지센터 (본동)","128.5410421","35.8344452","본동"));
//        sidoStationService.save(new SidoStation("대구 북구 연암로 40 시청별관 105동 3층 옥상 (산격동)","128.6009859","35.8925838","산격동"));
//        sidoStationService.save(new SidoStation("대구 동구 서호동 25-1(안심로49길 70) 반야월초등학교 옥상","128.7110217","35.8690445","서호동"));
//        sidoStationService.save(new SidoStation("대구 중구 수창동 73 (수창초등학교)(달성로 22길 30) 수창초등학교","128.5842249","35.8742256","수창동"));
//        sidoStationService.save(new SidoStation("대구 수성구 노변공원로 52 노변초등학교 (시지동)","128.6975463","35.8375505","시지동"));
//        sidoStationService.save(new SidoStation("대구 동구 신암동 72-1 (신암5동사무소)(아양로 37길 92) 주민센터","128.632942","35.8897788","신암동"));
//        sidoStationService.save(new SidoStation("대구 달성군 유가읍 테크노북로6길 20 비슬공원","128.4606996","35.6953158","유가읍"));
//        sidoStationService.save(new SidoStation("대구 서구 이현동 48-60 (중리초등학교)(국채보상로 135) 중리초등학교","128.545412","35.8696942","이현동"));
//        sidoStationService.save(new SidoStation("대구 수성구 지산동 761-11(한국환경공단) (무학로 209) 한국환경공단","128.6316177","35.830371","지산동"));
//        sidoStationService.save(new SidoStation("대구 달서구 월배로 131 월배초등학교 (진천동)","128.5289015","35.8163645","진천동"));
//        sidoStationService.save(new SidoStation("대구 북구 옥산로17길 21 대구일중학교 (침산동)","128.5844716","35.8867076","침산동"));
//        sidoStationService.save(new SidoStation("대구 북구 태전동 1076-5 (태암초등학교)(칠곡중앙대로 52길 56) 태암초등학교 남관 4층 옥상","128.5500759","35.9244188","태전동"));
//        sidoStationService.save(new SidoStation("대구 달서구 성서공단로 11길 32 대구기계부품연구원 1동 옥상 대구기계부품연구원","128.488822","35.8383824","호림동"));
//        sidoStationService.save(new SidoStation("대구 달성군 화원읍 인흥1길 12 화원명곡체육공원","128.5037757","35.7979591","화원읍"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 대송면 철강산단로130번길 29 3공단 배수지","129.3767553","35.9630725","3공단"));
//        sidoStationService.save(new SidoStation("경북 구미시 산동읍 첨단기업1로 17 구미전자정보기술원 혁신관 옥상","128.4498598","36.1511791","4공단"));
//        sidoStationService.save(new SidoStation("경북 영주시 가흥로 263 시립도서관 내 (가흥동)","128.6128055","36.8209126","가흥동"));
//        sidoStationService.save(new SidoStation("경북 구미시 공단동 256-18 (근로자종합복지회) 근로자종합복지회","128.3840348","36.1051002","공단동"));
//        sidoStationService.save(new SidoStation("경북 군위군 군위읍 군청로 158 군위종합테니스장 옥상","128.5740455","36.2377019","군위읍"));
//        sidoStationService.save(new SidoStation("경북 고령군 대가야읍 성산로 46 고령배수지","128.2862346","35.7280444","대가야읍"));
//        sidoStationService.save(new SidoStation("경북 김천시 공단2길 30-22 평생교육원 별관 2층 옥상","128.1371903","36.1435995","대광동"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 대도동 111-6(상대동평생학습관) 상대동평생학습관","129.3658879","36.0188389","대도동"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 대송면 장동홍계길19 (대송면사무소 내) 대송면사무소","129.3598975","35.9684559","대송면"));
//        sidoStationService.save(new SidoStation("경북 안동시 퇴계로 115 (명륜동) 안동시청 경민관 옥상","128.7282153","36.5683713","명륜동"));
//        sidoStationService.save(new SidoStation("경북 문경시 시청2길 45 모전도서관 옥상 (모전동)","128.1860797","36.586672","문경시"));
//        sidoStationService.save(new SidoStation("경북 경주시 경감로 587-18 경주스마트미디어센터 지상","129.2872808","35.835781","보덕동"));
//        sidoStationService.save(new SidoStation("경북 봉화군 봉화읍 봉화로 1111 봉화군청 입구","128.7325543","36.8929138","봉화군청"));
//        sidoStationService.save(new SidoStation("경북 상주시 북천로 63 북문동주민센터","128.1577764","36.4288976","상주시"));
//        sidoStationService.save(new SidoStation("경북 봉화군 석포면 석포로1길 55 석포면사무소 앞","129.0651659","37.0486618","석포면"));
//        sidoStationService.save(new SidoStation("경북 경주시 성건동 667-3(성건동주민센터) 성건동주민센터","129.2083203","35.8525534","성건동"));
//        sidoStationService.save(new SidoStation("경북 성주군 성주읍 성주로 3258 성주군 별고을교육원 옥상","128.2901285","35.9173967","성주군"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 축항로 55 송림초등학교 (송도동)","129.3750595","36.0301707","송도동"));
//        sidoStationService.save(new SidoStation("경북 경주시 안강읍 비화동길 9-40 대한노인회 경주시지회 안강읍분회 옥상","129.227026","35.9907612","안강읍"));
//        sidoStationService.save(new SidoStation("경북 의성군 안계면 안계길 114 안계면사무소 옥상","128.4401614","36.3838134","안계면"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 연일읍 동문로 67 연일복지회관 옥상","129.352089","35.9961941","연일읍"));
//        sidoStationService.save(new SidoStation("경북 영덕군 영덕읍 삼근길 6-7 영덕군민운동장 옆","129.3637317","36.4203307","영덕읍"));
//        sidoStationService.save(new SidoStation("경북 영양군 영양읍 군청길 37 영양군청 옥상","129.1123022","36.6670585","영양군"));
//        sidoStationService.save(new SidoStation("경북 영주시 광복로 65 수도사업소 옥상 (영주동)","128.6261845","36.828611","영주동"));
//        sidoStationService.save(new SidoStation("경북 영천시 시청로 23 영천 별빛직장어린이집","128.9375753","35.9727964","영천시"));
//        sidoStationService.save(new SidoStation("경북 영덕군 영해면 예주3길 7 영해면행정복지센터 옥상","129.4071391","36.5375185","영해면"));
//        sidoStationService.save(new SidoStation("경북 예천군 호명면 행복7길 25-4 예천군 통합관제센터 옥상","128.4732445","36.5767846","예천군"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 오천읍 냉천로320번길 4-11 오천읍민복지회관","129.408582","35.9667947","오천읍"));
//        sidoStationService.save(new SidoStation("경북 경주시 외동읍 입실로3길 31 외동보건지소 옥상","129.3235399","35.7156624","외동읍"));
//        sidoStationService.save(new SidoStation("경북 울릉군 울릉읍 도동2길 65 울릉군민회관","130.9054456","37.4842172","울릉읍"));
//        sidoStationService.save(new SidoStation("경북 울진군 울진읍 읍내10길 19 울진읍사무소","129.4026586","36.997304","울진군"));
//        sidoStationService.save(new SidoStation("경북 구미시 원평동 56-4(구미중앙로 24) 동사무소","128.325865","36.1308781","원평동"));
//        sidoStationService.save(new SidoStation("경북 김천시 혁신4로 21 율곡동 주민센터 옥상 (율곡동)","128.1836872","36.1214376","율곡동"));
//        sidoStationService.save(new SidoStation("경북 의성군 의성읍 군청길 31 의성군청 별관 옥상","128.696922","36.3527244","의성읍"));
//        sidoStationService.save(new SidoStation("경북 포항시 북구 삼흥로 98 북구보건소 북구보건소","129.3799825","36.0706692","장량동"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 철강로 331 공단정수장 공단정수장","129.374551","35.9804376","장흥동"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 인덕로 52 인덕어울림복지회관 옥상 (인덕동)","129.3970229","35.989659","제철동"));
//        sidoStationService.save(new SidoStation("경북 경산시 중방동 708-5 남매로 158 경산시보건소 정신보건센터 2층","128.7431775","35.8250191","중방동"));
//        sidoStationService.save(new SidoStation("경북 경산시 진량읍 낙산길 7 진량복지회관 옥상","128.8124992","35.8757107","진량읍"));
//        sidoStationService.save(new SidoStation("경북 구미시 이계북로 149 진미동 행정복지센터 (진평동)","128.4187479","36.1060474","진미동"));
//        sidoStationService.save(new SidoStation("경북 포항시 남구 신항로 10 청림동주민센터 (청림동)","129.405554","35.9973459","청림동"));
//        sidoStationService.save(new SidoStation("경북 청송군 청송읍 금월로 230 청송군 영농일자리지원센터 옥상","129.0560955","36.429297","청송읍"));
//        sidoStationService.save(new SidoStation("경북 칠곡군 왜관읍 관문로1길 30 칠곡군 보건소 건강증진센터 옥상","128.4179149","35.990549","칠곡군"));
//        sidoStationService.save(new SidoStation("경북 김천시 평화순환길 291 평화남산동주민센터 옥상 (평화동)","128.1161324","36.1205938","평화남산동"));
//        sidoStationService.save(new SidoStation("경북 경산시 하양읍 하양로 119-1 기상청부지 지상","128.8200546","35.9146495","하양읍"));
//        sidoStationService.save(new SidoStation("경북 구미시 형곡동 142(구미시립도서관) 구미시립도서관","128.3359906","36.1136085","형곡동"));
//        sidoStationService.save(new SidoStation("경북 청도군 화양읍 도주관로 159 화양읍사무소 옥상","128.7061245","35.6497264","화양읍"));
//        sidoStationService.save(new SidoStation("경남 함안군 가야읍 함안대로 505 가야읍행정복지센터 옥상","128.408444","35.272179","가야읍"));
//        sidoStationService.save(new SidoStation("경남 거창군 거창읍 거함대로 3252 자전거교통안전교육장 옥상","127.9179277","35.6812158","거창읍"));
//        sidoStationService.save(new SidoStation("경남 창원시 진해구 경화로16번길 31 (병암동주민센터)","128.6895634","35.1549551","경화동"));
//        sidoStationService.save(new SidoStation("경남 고성군 고성읍 중앙로 35 고성읍보건지소 2층 옥상","128.3242629","34.9743277","고성읍"));
//        sidoStationService.save(new SidoStation("경남 거제시 계룡로 125 거제시청 옥상 (고현동)","128.6210728","34.8808094","고현동"));
//        sidoStationService.save(new SidoStation("경남 하동군 금성면 금성중앙길 14 금성꿈나무어린이집 옥상","127.7941922","34.9659322","금성면"));
//        sidoStationService.save(new SidoStation("경남 남해군 남해읍 남해대로 2745 남해유배문학관 옥상","127.9000398","34.8323222","남해읍"));
//        sidoStationService.save(new SidoStation("경남 창원시 마산회원구 내서읍 광려로 8 삼계근린공원 지상","128.5031055","35.2260936","내서읍"));
//        sidoStationService.save(new SidoStation("경남 밀양시 중앙로 346 내일동주민센터 (내일동)","128.7541993","35.4936634","내일동"));
//        sidoStationService.save(new SidoStation("경남 진주시 진주대로 1052 (중소기업은행)","128.0845009","35.1935132","대안동"));
//        sidoStationService.save(new SidoStation("경남 김해시 호계로 517번길 8 (동상동 주민센터)","128.8834455","35.2368235","동상동"));
//        sidoStationService.save(new SidoStation("경남 창원시 의창구 우곡로101번길 28 명서2동 민원센터 (명서동)","128.6417456","35.2433761","명서동"));
//        sidoStationService.save(new SidoStation("경남 통영시 안개4길 53 무전동주민센터 (무전동)","128.4324438","34.8572868","무전동"));
//        sidoStationService.save(new SidoStation("경남 양산시 물금읍 황산로 384 물금읍행정복지센터 옥상","128.9872922","35.3103485","물금읍"));
//        sidoStationService.save(new SidoStation("경남 창원시 마산회원구 봉양로 148 봉암동 주민센터 (봉암동)","128.6024604","35.2175543","봉암동"));
//        sidoStationService.save(new SidoStation("경남 양산시 북안남5길 21 중앙동주민센터 (북부동)","129.0411472","35.3459604","북부동"));
//        sidoStationService.save(new SidoStation("경남 사천시 사천읍 읍내로 52 (사천읍사무소)","128.0911857","35.0827572","사천읍"));
//        sidoStationService.save(new SidoStation("경남 창원시 성산구 창이대로 706번길 16-23 (사파민원센터)","128.698235","35.2217903","사파동"));
//        sidoStationService.save(new SidoStation("경남 산청군 산청읍 산엔청로 6 산청군 여성회관 옥상","127.8740989","35.4149824","산청읍"));
//        sidoStationService.save(new SidoStation("경남 김해시 활천로 303 (신어초등학교)","128.9117842","35.2440858","삼방동"));
//        sidoStationService.save(new SidoStation("경남 양산시 삼호9길 11 웅상노인복지회관 (삼호동)","129.1720264","35.414382","삼호동"));
//        sidoStationService.save(new SidoStation("경남 진주시 동진로 279 (한국전력공사 진주지점)","128.1220265","35.1807529","상대동(진주)"));
//        sidoStationService.save(new SidoStation("경남 진주시 북장대로64번길 14 중앙119안전센터 옥상 (봉곡동)","128.0745431","35.1958905","상봉동"));
//        sidoStationService.save(new SidoStation("경남 창원시 성산구 외리로14번길 18 성주민원센터","128.7110457","35.1993968","성주동"));
//        sidoStationService.save(new SidoStation("경남 거제시 아주동 산164-1 거제시 옥포 유해대기측정소","128.6905545","34.8633069","아주동"));
//        sidoStationService.save(new SidoStation("경남 창원시 의창구 용지로 239번길 19-4 (용지동 주민센터)","128.6841074","35.236041","용지동"));
//        sidoStationService.save(new SidoStation("경남 창원시 성산구 공단로 303 (효성굿스프링스)","128.6581363","35.2144521","웅남동"));
//        sidoStationService.save(new SidoStation("경남 창원시 마산합포구 월영동16길 22 마산합포구도서관 옥상 (해운동)","128.5628437","35.1830403","월영동"));
//        sidoStationService.save(new SidoStation("경남 의령군 의령읍 의병로8길 44 서동생활공원 지상","128.256499","35.315549","의령읍"));
//        sidoStationService.save(new SidoStation("경남 김해시 장유동 능동로 149 (장유건강지원센터)","128.8072627","35.2022911","장유동"));
//        sidoStationService.save(new SidoStation("경남 진주시 정촌면 예하리 1340 예하초등학교 앞 공원 지상","128.1007457","35.1251988","정촌면"));
//        sidoStationService.save(new SidoStation("경남 김해시 진영읍 김해대로365번길 6-24 진영건강증진센터 옥상","128.7310709","35.3077341","진영읍"));
//        sidoStationService.save(new SidoStation("경남 창녕군 창녕읍 우포2로 1189-35 정신건강복지센터 옥상","128.4904015","35.5435899","창녕읍"));
//        sidoStationService.save(new SidoStation("경남 하동군 하동읍 군청로 23 (하동군청)","127.7517474","35.067393","하동읍"));
//        sidoStationService.save(new SidoStation("경남 함양군 함양읍 고운로 35 함양군청 민원봉사과 옥상","127.7245034","35.5210527","함양읍"));
//        sidoStationService.save(new SidoStation("경남 합천군 합천읍 대야로 888-20 보훈회관 옥상","128.1651598","35.5671796","합천읍"));
//        sidoStationService.save(new SidoStation("경남 사천시 향촌5길 28 향촌동행정복지센터 옥상 (향촌동)","128.0935055","34.933879","향촌동"));
//        sidoStationService.save(new SidoStation("경남 창원시 마산회원구 회원동 11번길 7 (회원1동 주민센터)","128.5741917","35.2182325","회원동"));
//        sidoStationService.save(new SidoStation("전남 강진군 강진읍 동성로 72 강진읍 측정소","126.7711346","34.6445099","강진읍"));
//        sidoStationService.save(new SidoStation("전남 고흥군 고흥읍 터미널길 11 고흥읍사무소","127.2807805","34.6062466","고흥읍"));
//        sidoStationService.save(new SidoStation("전남 곡성군 곡성읍 학정3길 6 곡성읍 측정소","127.2896294","35.2833485","곡성읍"));
//        sidoStationService.save(new SidoStation("전남 광양시 광양읍 인덕로 1100(칠성리) 광양읍 2청사 옥상","127.5836335","34.9808168","광양읍"));
//        sidoStationService.save(new SidoStation("전남 구례군 구례읍 동편제길 30 구례읍 측정소","127.4611173","35.2159159","구례읍"));
//        sidoStationService.save(new SidoStation("전남 담양군 담양읍 면앙정로 730 담양군 농업기술센터 생명농업연구동 뒤편","126.9738837","35.3118461","담양읍"));
//        sidoStationService.save(new SidoStation("전남 영암군 삼호읍 대불주거9로 27 대불종합체육관","126.4512021","34.7571185","대불"));
//        sidoStationService.save(new SidoStation("전남 여수시 덕충안길 95 만덕동주민자치센터","127.7465406","34.7536334","덕충동"));
//        sidoStationService.save(new SidoStation("전남 무안군 무안읍 성내1길 2 무안읍사무소","126.4763023","34.9905297","무안읍"));
//        sidoStationService.save(new SidoStation("전남 여수시 여문1로 71(문수동) 문수동주민센터 옥상","127.7027047","34.7545891","문수동"));
//        sidoStationService.save(new SidoStation("전남 보성군 벌교읍 체육공원길 35 벌교읍 측정소","127.3476872","34.8418558","벌교읍"));
//        sidoStationService.save(new SidoStation("전남 보성군 보성읍 현충로 42-36 원봉3구회관(노인당) 옥상","127.0782134","34.7649312","보성읍"));
//        sidoStationService.save(new SidoStation("전남 광양시 봉강면 조양길 46 봉강면사무소 옥상","127.5812102","35.011467","봉강면"));
//        sidoStationService.save(new SidoStation("전남 목포시 삼향천로 28(옥암동) 부흥동주민센터","126.4345289","34.8043087","부흥동"));
//        sidoStationService.save(new SidoStation("전남 나주시 빛가람로 719 빛가람동 주민센터 옥상","126.7903456","35.021765","빛가람동"));
//        sidoStationService.save(new SidoStation("전남 여수시 상암로 601-1 상암보건지소 (상암동)","127.7315646","34.8247831","삼일동"));
//        sidoStationService.save(new SidoStation("전남 여수시 서교1길 28-1 서강동주민자치센터 (서교동)","127.7259377","34.7412653","서강동"));
//        sidoStationService.save(new SidoStation("전남 순천시 순천만길 513-25(대대동) 순천만 자연생태관 옥상","127.5091088","34.8857858","순천만"));
//        sidoStationService.save(new SidoStation("전남 순천시 해룡면 매안로 162 순천시립신대도서관","127.5481925","34.9366178","신대"));
//        sidoStationService.save(new SidoStation("전남 신안군 안좌면 중부로 860 신안군 측정소","126.1259872","34.7555665","신안군"));
//        sidoStationService.save(new SidoStation("전남 완도군 신지면 신지로 567 신지면 문화센터","126.8289675","34.3344141","신지면"));
//        sidoStationService.save(new SidoStation("전남 여수시 무선로 190(선원동) 여천동 주민센터 뒷편","127.6524298","34.7774801","여천동(여수)"));
//        sidoStationService.save(new SidoStation("전남 순천시 연향번영길 54(연향동) 연향도서관 옥상","127.5185749","34.9471232","연향동"));
//        sidoStationService.save(new SidoStation("전남 영광군 영광읍 물무로2길 61 영광읍사무소","126.511826","35.2778938","영광읍"));
//        sidoStationService.save(new SidoStation("전남 영암군 영암읍 낭주로 202-2 영암 종합스포츠타운 야구장","126.7012932","34.80725","영암읍"));
//        sidoStationService.save(new SidoStation("전남 목포시 동부로 31번길 20(용당1동 주민센터 옥상) 용당1동 주민센터 옥상","126.3916813","34.8059673","용당동"));
//        sidoStationService.save(new SidoStation("전남 여수시 여수산단로 1201(월내동) 여수산단 월내 폐수종말처리장","127.7289773","34.853715","월내동"));
//        sidoStationService.save(new SidoStation("전남 여수시 율촌면 동산개길 2 율촌면 보건지소","127.5789031","34.882054","율촌면"));
//        sidoStationService.save(new SidoStation("전남 장성군 장성읍 영천로 211 장성읍사무소 옥상","126.7853568","35.3032491","장성읍"));
//        sidoStationService.save(new SidoStation("전남 순천시 장명로 30(장천동) 순천시청 별관 옥상","127.4871779","34.9503031","장천동"));
//        sidoStationService.save(new SidoStation("전남 장흥군 장흥읍 흥성로 23 정남진도서관","126.9065956","34.6743759","장흥읍"));
//        sidoStationService.save(new SidoStation("전남 광양시 중마중앙로 109 광양소방서 옥상","127.6976261","34.939831","중동(유해+중금속)"));
//        sidoStationService.save(new SidoStation("전남 진도군 진도읍 진도대로 7195 진도여성플라자","126.2689182","34.4795461","진도읍"));
//        sidoStationService.save(new SidoStation("전남 광양시 진월면 선소중앙길 31 진월면사무소 옥상","127.7583026","34.9785136","진월면"));
//        sidoStationService.save(new SidoStation("전남 광양시 태인길 376(태인동) 태인폐정수장 사무실","127.755762","34.9414582","태인동"));
//        sidoStationService.save(new SidoStation("전남 함평군 함평읍 중앙길 39 함평군노인복지회관","126.522628","35.062021","함평읍"));
//        sidoStationService.save(new SidoStation("전남 해남군 해남읍 남부순환로 114 해남읍사무소","126.5990826","34.5653304","해남읍"));
//        sidoStationService.save(new SidoStation("전남 순천시 해룡면 호두리 276-9 호두정수장","127.5729798","34.8936083","호두리"));
//        sidoStationService.save(new SidoStation("전남 화순군 화순읍 동헌길 9-24 화순군 CCTV 관제센터","126.9858919","35.0652918","화순읍"));
//        sidoStationService.save(new SidoStation("전남 여수시 화양면 평촌길 2-6 화동경로당","127.5973832","34.6857366","화양면"));
//        sidoStationService.save(new SidoStation("광주 북구 첨단과기로 333 (광주테크노파크 벤처지원센터 옥상) 광주테크노파크 벤처지원센터 옥상","126.8617075","35.2294041","건국동"));
//        sidoStationService.save(new SidoStation("광주 남구 덕남길 7 문화관 근처 (노대동)","126.8970659","35.1003831","노대동"));
//        sidoStationService.save(new SidoStation("광주 서구 상무대로 1165 (광주시립미술관 상록전시관 단독건물) 광주시립미술관","126.888757","35.1549493","농성동"));
//        sidoStationService.save(new SidoStation("광주 북구 군왕로 141번길 6 (두암보건지소) 두암보건지소 3층 옥상","126.9325911","35.1743807","두암동"));
//        sidoStationService.save(new SidoStation("광주 동구 서남로 1 동구청보건소","126.9232737","35.1458415","서석동"));
//        sidoStationService.save(new SidoStation("광주 광산구 하남산단 6번로 107 삼성전자 환경안전센터","126.8077903","35.2047194","오선동"));
//        sidoStationService.save(new SidoStation("광주 광산구 우산동 1026-2 월곡119안전센터 인근 사유지 (우산동)","126.8100579","35.1576541","우산동(광주)"));
//        sidoStationService.save(new SidoStation("광주 서구 천변우하로 203 광주보건환경연구원 신청사 옥상 (유촌동)","126.8450667","35.1617697","유촌동"));
//        sidoStationService.save(new SidoStation("광주 북구 모룡대길 68 교통문화연수원 옥상 (일곡동)","126.8926504","35.2182887","일곡동"));
//        sidoStationService.save(new SidoStation("광주 남구 회서로 21번가길 13 주월1동 주민센터","126.893327","35.1319337","주월동"));
//        sidoStationService.save(new SidoStation("광주 광산구 평동산단로 184-1 평동종합비즈니스센터 후문 주차장 입구 (월전동)","126.7692582","35.1236254","평동"));
//        sidoStationService.save(new SidoStation("전북 부안군 계화면 간재로 405 계화면사무소 옥상","126.6977396","35.7615533","계화면"));
//        sidoStationService.save(new SidoStation("전북 완주군 고산면 고산로 69-13 고산면주민자치센터 옥상","127.2053318","35.9764507","고산면"));
//        sidoStationService.save(new SidoStation("전북 고창군 고창읍 월곡공원1길 36 고창군여성회관","126.7127605","35.4380945","고창읍"));
//        sidoStationService.save(new SidoStation("전북 임실군 관촌면 사선1길 13 관촌면 행정복지센터 옥상","127.2706778","35.6737261","관촌면"));
//        sidoStationService.save(new SidoStation("전북 김제시 광활면 지평선로 638 광활면 화합관 옥상","126.7405131","35.835733","광활면"));
//        sidoStationService.save(new SidoStation("전북 완주군 구이면 덕천전원길 232-58 술테마박물관 부지","127.1361049","35.7266719","구이면"));
//        sidoStationService.save(new SidoStation("전북 전주시 완산구 물왕멀3길 29 전주도시혁신센터 옥상 (서노송동)","127.148061","35.8286327","노송동"));
//        sidoStationService.save(new SidoStation("전북 익산시 배산로 189-10 익산시 청소년문화의 집 옥상","126.9411143","35.9546992","모현동"));
//        sidoStationService.save(new SidoStation("전북 무주군 무주읍 향학로 49 무주읍사무소 옥상","127.662049","36.0087009","무주읍"));
//        sidoStationService.save(new SidoStation("전북 완주군 봉동읍 삼봉로 933 3층 옥외광장","127.1659009","35.9417538","봉동읍"));
//        sidoStationService.save(new SidoStation("전북 부안군 행안면 변산로 16 농업인 회관","126.7194361","35.7297368","부안읍"));
//        sidoStationService.save(new SidoStation("전북 군산시 새만금북로 43 비응 119 안전센터 옥상 (비응도동)","126.5410784","35.9437977","비응도동"));
//        sidoStationService.save(new SidoStation("전북 군산시 번영로 329 개정작은도서관 옥상 (사정동)","126.7532123","35.9640156","사정동"));
//        sidoStationService.save(new SidoStation("전북 익산시 삼기면 황금로 513 삼기면 행정복지센터 옥상","126.9840707","36.0202774","삼기면"));
//        sidoStationService.save(new SidoStation("전북 전주시 완산구 용리로 107 삼천도서관 옥상","127.1216934","35.7988981","삼천동"));
//        sidoStationService.save(new SidoStation("전북 군산시 외항1길 222. (롯데주류) 3층옥상 롯데주류","126.6514742","35.9758662","소룡동"));
//        sidoStationService.save(new SidoStation("전북 군산시 동장산2길 6 자동차융합기술원 연구실험동 옥상 (소룡동)","126.5968112","35.9590352","소룡동2"));
//        sidoStationService.save(new SidoStation("전북 전주시 덕진구 동부대로 1183 전주시 농수산물도매시장 관리동 (송천동2가)","127.1168894","35.868047","송천동"));
//        sidoStationService.save(new SidoStation("전북 순창군 순창읍 경천로 33 순창군청 3층 옥상","127.137354","35.3745246","순창읍"));
//        sidoStationService.save(new SidoStation("전북 정읍시 신태인읍 신태인중앙로 40 신태인여성문화관 옥상","126.8894362","35.6885601","신태인"));
//        sidoStationService.save(new SidoStation("전북 군산시 대학로 215 신풍동주민센터 옥상 (문화동)","126.7007977","35.9728055","신풍동(군산)"));
//        sidoStationService.save(new SidoStation("전북 고창군 심원면 심원로 211 심원면사무소","126.5509947","35.5242009","심원면"));
//        sidoStationService.save(new SidoStation("전북 익산시 여산면 가람로 393 여산보건지소 옥상","127.0835051","36.0598421","여산면"));
//        sidoStationService.save(new SidoStation("전북 전주시 덕진구 여암2길 9 전주시립쪽구름도서관 옥상 (반월동) (반월동)","127.0755933","35.8713055","여의동"));
//        sidoStationService.save(new SidoStation("전북 정읍시 조곡천1길 7. 여성문화관 옥상","126.8459002","35.5684657","연지동"));
//        sidoStationService.save(new SidoStation("전북 정읍시 영파동 232-1 정읍 공공하수처리시설 옥상 (영파동)","126.8542507","35.6103033","영파동"));
//        sidoStationService.save(new SidoStation("전북 군산시 옥산면 산성로 200 옥산면사무소 옥상","126.7482448","35.9394859","옥산면"));
//        sidoStationService.save(new SidoStation("전북 김제시 요촌중길 50 요촌동 주민센터 옥상","126.8913929","35.8048814","요촌동"));
//        sidoStationService.save(new SidoStation("전북 익산시 용동면 용동1길 80-4 용동복지회관 옥상","126.9917064","36.1096566","용동면"));
//        sidoStationService.save(new SidoStation("전북 남원시 운봉읍 황산로 1083 운봉읍 행정복지센터 옥상","127.5288783","35.4395935","운봉읍"));
//        sidoStationService.save(new SidoStation("전북 임실군 임실읍 운수로 33-50 청소년문화의집 옥상","127.2864726","35.6142689","임실읍"));
//        sidoStationService.save(new SidoStation("전북 장수군 장수읍 호비로 10 장수군청 옥상","127.5211124","35.6475093","장수읍"));
//        sidoStationService.save(new SidoStation("전북 남원시 비석길 72 죽항동주민센터 옥상 (죽항동)","127.384515","35.4075127","죽항동"));
//        sidoStationService.save(new SidoStation("전북 진안군 진안읍 진무로 1189 진안군 보건소 옥상","127.4348933","35.797308","진안읍"));
//        sidoStationService.save(new SidoStation("전북 익산시 춘포면 춘포2길 11 춘포보건지소 옥상","127.0052594","35.9010359","춘포면"));
//        sidoStationService.save(new SidoStation("전북 전주시 덕진구 서귀로 107 청소년자유센터 옥상 (팔복동3가)","127.0875162","35.8513067","팔복동"));
//        sidoStationService.save(new SidoStation("전북 익산시 무왕로 1338. 익산소방서 옥상","127.0051158","35.9620334","팔봉동"));
//        sidoStationService.save(new SidoStation("전북 익산시 함열읍 함열중앙로 83 익산시청 북부청사 옥상","126.9664791","36.0785647","함열읍"));
//        sidoStationService.save(new SidoStation("전북 전주시 덕진구 중동로 150 공원 내 (장동)","127.0631293","35.8464413","혁신동"));
//        sidoStationService.save(new SidoStation("전북 전주시 완산구 쑥고개로 259 전주역사박물관 옥상 (효자동2가)","127.0933017","35.8011028","효자동"));
//        sidoStationService.save(new SidoStation("제주특별자치도 서귀포시 일주서로 166 제주 유나이티드 연습구장 옆(지상) (강정동)","126.4909508","33.2533328","강정동"));
//        sidoStationService.save(new SidoStation("제주특별자치도 서귀포시 남원읍 남한로 67 서귀포시 동부노인복지회관","126.7148829","33.284268","남원읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 서귀포시 대정읍 동일하모로149번길 21-8 대정청소년수련관","126.2474172","33.2277441","대정읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 서귀포시 동홍동 453-1(서귀포소방서) 서귀포소방서","126.5671115","33.2511322","동홍동"));
//        sidoStationService.save(new SidoStation("제주특별자치도 서귀포시 성산읍 일주동로 4120번길 7 (동부소방서)","126.9096902","33.460245","성산읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 애월읍 고내리 1319 애월근린공원 옆","126.3314345","33.4647103","애월읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 연동 322-1 번지 제주특별자치도청 제2청사 제주특별자치도청 제2청사 옥상","126.5004669","33.4889049","연동"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 이도2동 1176-1(제주시청) 제주시청","126.5315853","33.4996817","이도동"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 조천읍 조천18길 11-1 조천읍 체육관","126.6439526","33.5391265","조천읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 한림읍 한림중앙로 71-9 한림읍 체육관","126.2684929","33.4092593","한림읍"));
//        sidoStationService.save(new SidoStation("제주특별자치도 제주시 화북일동 1098 지상 (화북일동)","126.5702562","33.5169934","화북동"));
//
//
//    }

}
