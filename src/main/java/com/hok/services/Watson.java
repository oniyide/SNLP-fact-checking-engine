package com.hok.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public class Watson {

    private static final String storage_dir = "storage/";
    private static final String url = "https://en.wikipedia.org/w/index.php?search=";


//    public String search_result;
    //tokens is a hashmap that contains subject, action and object as keys.
    //public Watson(Map<String, String> tokens){
//        search_result = search(tokens);
    //}


    public String search(Map<String, String> tokens){

        String subject = tokens.get("subject");
        String object = tokens.get("object");
        String action = tokens.get("action");
        File file_subj = new File(storage_dir + subject);

        String info = subject_check(subject, file_subj);

        return info;
    }

    public void saveInfo(String token, String info){

        info = info.replaceAll("\\[[\\p{Alnum}]*\\]", "");
        try {
            FileUtils.writeStringToFile(new File(storage_dir + token), info, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getInfo(String token){
        String info = "";
        try {
            info =  FileUtils.readFileToString(new File(storage_dir + token), "UTF-8");
        } catch (Exception e) {
            System.out.println("Couldn't Read: " + storage_dir + token);
        }
        return info;
    }


    public String searchUpdate(Map<String, String> tokens){

        String subject = tokens.get("subject")+ " "+ tokens.get("action");
        String object = tokens.get("object");
        String action = tokens.get("action");
        File file_subj = new File(storage_dir + subject);

        String info = subject_check(subject, file_subj);

        return info;
    }



    public String subject_check(String subject, File file_subj){
        String info = "";
        if (!file_subj.exists()){
            try {
                Document doc = Jsoup.connect(url + URLEncoder.encode(subject, "UTF8")).get();

                if (doc.selectFirst("h1").text().equals("Search results"))
                {
                    //System.out.println("Incorrect subject: "+ subject);
                    Element correct_heading = doc.selectFirst("div.mw-search-result-heading");
                    String link = correct_heading.selectFirst("a").attr("href");
                    link = link.replaceAll("/wiki/", "");
                    doc = Jsoup.connect(url + link).get();

                }
                Element heading = doc.selectFirst("h1");
                Elements paragraphs = doc.select("p:not(:has(#coordinates))");
//                System.out.println("Fact Title: "+ doc.title());
                info += heading.text()+ "\n";
                for (Element p : paragraphs){
                    info += p.text() + "\n";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (!StringUtils.isBlank(info)){
                    saveInfo(subject, info);
                }
            }
        }
        else {
            info = getInfo(subject); // May implement depth feature. Lets see
        }
        return info;
    }



}
