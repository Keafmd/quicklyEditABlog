package com.keafmd;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Keafmd
 *
 * @ClassName: HttpClientTest
 * @Description:
 * @author: 牛哄哄的柯南
 * @date: 2021-08-20 14:53
 */
public class HttpClientTest {
    public static void main(String[] args) throws IOException {

        //这里修改每套题的组id
        String tid = "46702839";
        HashMap<String, String> firstQuestion = getTheFirstQuestion(tid);
        String qid = firstQuestion.get("qid");
        List<String> list = backToTheCollectionOfQuestions(tid, qid);
        HashMap<String, HashMap<String, String>> resmap = getTopicContent(tid, list);
        HashMap<Integer, HashMap<String, String>> problemMap = analyzeTheContentOfEachQuestion(resmap, list);


        //String nnn = "sdsf" + "\n" + "sdfsg";
        //System.out.println(nnn);


        File file = new File("C:\\Users\\章贺龙\\Desktop\\模板.md");
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt = null;
        HashMap<String, Integer> map = new HashMap<>();
        List<String> list1 = new ArrayList<>();

        StringBuilder readString = new StringBuilder();


        while ((lineTxt = bufferedReader.readLine()) != null) {
            //System.out.println(lineTxt);
            readString.append(lineTxt);
            readString.append("\n");
        }

        //System.out.println(readString.toString());
        String result = readString.toString();
        //System.out.println(result);



        for(int i=10;i>=1;i--){
            String timu = "题目";
            String daan = "正确答案: B   你的答案: B (正确)";
            String xuan = "选项";
            String tijie = "题解内容";
            timu+=i;
            daan+=i;
            xuan+=i;
            tijie+=i;

            /*System.out.println(problemMap.get(i-1).get("title"));
            System.out.println(problemMap.get(i-1).get("answer"));
            System.out.println(problemMap.get(i-1).get("roptions"));
            System.out.println(problemMap.get(i-1).get("rnote"));*/


            String title = result.replace(timu, problemMap.get(i - 1).get("title"));
            String answer = title.replace(daan, problemMap.get(i - 1).get("answer"));
            String roptions = answer.replace(xuan, problemMap.get(i - 1).get("roptions"));
            String rnote = roptions.replace(tijie, problemMap.get(i - 1).get("rnote"));
            result = rnote;
        }


        System.out.println(result);

        FileOutputStream fos = new FileOutputStream("C:\\Users\\章贺龙\\Desktop\\99.md",true);
       //true表示在文件末尾追加
        fos.write(result.getBytes());
        fos.close();


    }

    /**
     * 解析每题的内容
     * @param resmap
     * @param list
     * @return
     */
    public static HashMap<Integer, HashMap<String, String>> analyzeTheContentOfEachQuestion(HashMap<String, HashMap<String, String>> resmap, List<String> list) {

        HashMap<Integer, HashMap<String, String>> res = new HashMap<>();
        for (int i = 0; i <= 9; i++) {

            HashMap<String, String> map = new HashMap<>();


            String title = resmap.get(list.get(i)).get("title");
            String answer = resmap.get(list.get(i)).get("answer");

            String roptions = "";
            String rnote = "";

            String options = resmap.get(list.get(i)).get("options");
            options.replaceAll(" ", "");
            //System.out.println(options);
            String[] optionsSplit = options.split(",");
            Character order = 'A';
            for (int i1 = 0; i1 < optionsSplit.length; i1++) {
                String s = optionsSplit[i1];
                if (order == 'A') {
                    s = order + " " + s;
                } else {
                    s = order + s;
                }


                //System.out.println(s);
                if(i1<optionsSplit.length-1){
                    s += "\n";
                }

                roptions = roptions + s;
                order++;
            }

            String note = resmap.get(list.get(i)).get("note");
            String[] noteSplit = note.split(",");
            Character nnote = '1';
            for (int i1 = 0; i1 < noteSplit.length; i1++) {

                String s = noteSplit[i1];
                if (s.contains("查看全部")) {
                    continue;
                }
                s = nnote + "、" + s;
                nnote++;

                //System.out.println(s);
                if(i1<noteSplit.length-1){
                    s += "\n";
                }

                rnote = rnote + s;

            }

            map.put("title",title);
            map.put("answer",answer);
            map.put("roptions",roptions);
            map.put("rnote",rnote);
            res.put(i,map);


        }
        return res;
    }

    /**
     * 获取每题的内容
     *
     * @param list
     * @return
     */
    public static HashMap<String, HashMap<String, String>> getTopicContent(String tid, List<String> list) {

        HashMap<String, HashMap<String, String>> resmap = new HashMap<>();
        // 题目  答案 选项 题解
        // title  answer options note

        for (String qid : list) {
            HashMap<String, String> map = new HashMap<>();

            String url = "https://www.nowcoder.com/test/question/done?tid=" + tid + "&qid=" + qid;
            String html = requestPage(url);
//            System.out.println(html);

            Document document = Jsoup.parse(html);

            //===========  解析题目  ===========
            Elements title1 = document.getElementsByClass("question-main");
            String title = title1.eachText().toString();
            title = title.replaceAll("\\[", "");
            title = title.replaceAll("\\]", "");
            map.put("title", title);


            //===========  解析答案  ===========
            Elements content = document.getElementsByClass("result-subject-item result-subject-answer");
            Elements answerh = content.select("h1");
            String answer = answerh.eachText().toString();

            //不让别人看到我做错的答案
            //正确答案:C你的答案:A(错误)
            answer = answer.substring(0, answer.lastIndexOf('你'));


            answer = answer.replaceAll("\\[", "");
            answer = answer.replaceAll("\\]", "");
            answer = answer.replaceAll(" ", "");
            map.put("answer", answer);

            //===========  解析选项  ===========
            Elements optionss = document.getElementsByClass("result-answer-item");
            Elements optionsE = optionss.select("div");
            String options = optionsE.eachText().toString();
            options = options.replaceAll("\\[", "");
            options = options.replaceAll("\\]", "");
            map.put("options", options);

            //===========  解析题解  ===========
            Elements notes = document.getElementsByClass("answer-brief");
//            Elements noteE = notes.select("div");
            String note = notes.eachText().toString();
            note = note.replaceAll("\\[", "");
            note = note.replaceAll("\\]", "");
            map.put("note", note);

            //System.out.println("===============================");
            resmap.put(qid, map);


        }

        return resmap;
    }


    /**
     * 返回题目集合
     *
     * @param tid
     * @param qid 第一题的qid
     * @return
     */
    public static List<String> backToTheCollectionOfQuestions(String tid, String qid) {

        List<String> list = new ArrayList<>();
        String url = "https://www.nowcoder.com/test/question/done?tid=" + tid + "&qid=" + qid;
        String html = requestPage(url);
        //System.out.println(html);

        Document document = Jsoup.parse(html);

//        Elements elements = document.getElementsByTag("a");
        Elements listEl = document.getElementsByClass("subject-num-list");

        Elements a = listEl.select("a");
        //System.out.println(a.toString());
        for (Element element : a) {
            String attr = element.attr("data-qid");
            list.add(attr);
            //System.out.println(attr);
        }


        return list;
    }


    /**
     * 获取第一题
     *
     * @param tid 题组号
     * @return
     */
    public static HashMap<String, String> getTheFirstQuestion(String tid) {

        HashMap<String, String> map = new HashMap<>();

        String url = "https://www.nowcoder.com/test/question/analytic?tid=" + tid;
        String html = requestPage(url);
        Document document = Jsoup.parse(html);

        //获得文档下所有div标签，返回的是一个标签的集合
        Elements elements = document.getElementsByTag("a");
        Elements classEle = document.getElementsByClass("btn btn-primary btn-lg nc-req-auth");
        String href = classEle.attr("href");
        int wenhao = href.lastIndexOf('?');
        int yuhao = href.lastIndexOf('&');
        int denghao = href.lastIndexOf('=');
        String stid = href.substring(wenhao + 5, yuhao);
        String sqid = href.substring(denghao + 1);
        //System.out.println(href);
        //System.out.println(stid);
        //System.out.println(sqid);
        map.put("tid", stid);
        map.put("qid", sqid);


        return map;


    }

    //请求页面
    public static String requestPage(String url) {
        //1.生成httpclient，相当于该打开一个浏览器
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;


        //2.创建get请求，相当于在浏览器地址栏输入 网址
//        HttpGet request = new HttpGet("https://www.nowcoder.com/test/question/done?tid="+tid+"&qid="+qid);
//        HttpGet request = new HttpGet("https://www.nowcoder.com/test/question/analytic?tid=" + tid);
        HttpGet request = new HttpGet(url);

        //模拟登陆牛客的状态


        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        request.setHeader("Accept-Encoding", "gzip, deflate, br");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        request.setHeader("Cache-Control", "max-age=0");
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Host", "www.nowcoder.com");
        request.setHeader("Referer", "https://www.nowcoder.com/test/question/done?tid=46668987");
        request.setHeader("Upgrade-Insecure-Requests", "1");
        request.setHeader("Upgrade-Insecure-Requests", "1");
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
        request.setHeader("Cookie", "NOWCODERCLINETID=0C69020C0197FBAED87853FAF8ACCE67; gr_user_id=a505c1ef-8193-401d-afc6-9b178af4d653; grwng_uid=b6c3de4d-a7db-47da-ae27-1b6dcd59650c; c196c3667d214851b11233f5c17f99d5_gr_last_sent_cs1=127149377; from=nowcoderexam; t=75E235998BCBBA61D762EF7E6C1612F9; NOWCODERUID=2C46C185D92D11B5CD703E63A9ED176E; _ga=GA1.2.641413226.1629253451; _gid=GA1.2.211718254.1629360262; c196c3667d214851b11233f5c17f99d5_gr_session_id=230f8546-6d52-423f-a284-86b4f2b077a6; c196c3667d214851b11233f5c17f99d5_gr_last_sent_sid_with_cs1=230f8546-6d52-423f-a284-86b4f2b077a6; c196c3667d214851b11233f5c17f99d5_gr_session_id_230f8546-6d52-423f-a284-86b4f2b077a6=true; Hm_lvt_a808a1326b6c06c437de769d1b85b870=1629298191,1629334793,1629420827,1629444950; c196c3667d214851b11233f5c17f99d5_gr_cs1=127149377; Hm_lpvt_a808a1326b6c06c437de769d1b85b870=1629445039; _gat_gtag_UA_204868849_1=1; SERVERID=76c38881415ad3405ef5a3db2e9b59bb|1629445078|1629420824");

        String html = "";
        try {
            //3.执行get请求，相当于在输入地址栏后敲回车键
            response = httpClient.execute(request);

            //4.判断响应状态为200，进行处理
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //5.获取响应内容
                HttpEntity httpEntity = response.getEntity();
                html = EntityUtils.toString(httpEntity, "utf-8");
                //System.out.println(html);


            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                System.out.println("返回状态不是200");
                System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //6.关闭
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        return html;
    }


}
