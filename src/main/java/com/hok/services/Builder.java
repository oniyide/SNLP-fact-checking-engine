package com.hok.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Builder {
    // activate if using training data
//    private static final String result_data = "SNLP2019_training_result.ttl";
//    private static final String train_data = "SNLP2019_training.tsv";

    private static final String result_test_data = "SNLP2019_test_result.ttl";
    private static final String test_data = "SNLP2019_test.tsv";

    private static final String storage_dir = "storage/";

    private static final Map<String, String> fact_map = new HashMap<>();
    private static final Map<String, String> correction_map = new HashMap<>();

    private static final Map<String, String> result_map = new HashMap<>();
    int cc = 0;



    public void launch()  {
        System.out.println("launch phase");
        new File(storage_dir).mkdirs();
        File file = new File(test_data);
        LineIterator lineItr = null;
        try {
            lineItr = FileUtils.lineIterator(file, "UTF-8");

            while (lineItr.hasNext()){
                String line = lineItr.next();
                if (!StringUtils.isBlank(line)) {
                    if (line.split("\t")[0].matches("\\d+")){
                        List<String> fact_content = Arrays.asList(line.split("\t"));

                        fact_map.put(fact_content.get(0), fact_content.get(1));
                    }

                }
            }
            generateResult(fact_map);

            exportToFile(result_map);
            System.out.println("All facts have been generated in SNLP2019_test_result.ttl");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void exportToFile(Map<String, String> res_export){
        try {
            String data = "";

            String p1 = "<http://swc2017.aksw.org/task2/dataset/";
            String p2 = "<http://swc2017.aksw.org/hasTruthValue>";
            String p3 = "^^<http://www.w3.org/2001/XMLSchema#double>";

            for (Map.Entry<String, String> e : res_export.entrySet())
                data += (p1 + e.getKey() + "> " + p2 + " \"" + e.getValue() + "\"" + p3 + " .\n");

            FileUtils.writeStringToFile(new File(result_test_data), data, "UTF-8");
        } catch (Exception e) {
            System.out.println("Couldn't Write: " + result_test_data);
        }
    }



    public void generateResult(Map<String, String> facts){

        Iterator itr = facts.entrySet().iterator();

        while (itr.hasNext()) {
            cc++;
            if (cc % 250 == 0)
                System.out.println(cc + " facts processed");
            Map.Entry pair = (Map.Entry)itr.next();

            String text = (String)pair.getValue();

            Map<String, String> tokens = tokenSanitizer(text);
            Watson watson = new Watson();
            String info = watson.search(tokens);
            boolean match = match(info, tokens);
            String match_val;
            if (match == true)
                match_val = "1.0";
            else
                match_val = "0.0";

            String fact = (String) pair.getKey();
            result_map.put(fact, match_val);


        }
    }

    public Map<String, String> tokenSanitizer(String text){
        Map<String, String> sanitized_token = new HashMap<>();
        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

        text = text.replaceAll("[,.:\\-\\&]|'s", " ");
        String dummies[] = new String[] {"\\band\\b", "\\bor\\b", "\\bin\\b", "\\binto\\b", "\\bvan\\b", "\\bder\\b",
                "\\bof\\b", "\\bfor\\b", "\\bdas\\b", "\\bthe\\b", "\\bde\\b", "\\ba\\b",
                "\\bal\\b", "\\bas\\b" ,"\\bdel\\b", "\\bdu\\b", "\\bvon\\b", "\\ble\\b",
                "\\blas\\b", "\\bby\\b", "\\bon\\b", "\\bat\\b", "\\bden\\b", "\\bfrom\\b",
                "\\bat\\b","\\bshek\\b","\\bcom\\b", "\\bwith\\b"};

        for (String s: dummies){
            String new_s = s.substring(2,s.length()-2);
            new_s = new_s.substring(0, 1).toUpperCase() + new_s.substring(1);
            text = text.replaceAll(s, new_s);
        }

        String subject = "";
        String action ="";
        String object = "";

        Pattern subj_pattern = Pattern.compile("(\\b([A-Z][A-Za-z']*\\b|(\\d+)[a-z]*) *)+(\\([A-Za-z0-9 ]+\\))*(\\b[A-Z][A-Za-z0-9]*\\b)*");
        Matcher subj_matcher = subj_pattern.matcher(text);
        if (subj_matcher.find()){
            subject= subj_matcher.group();
            subject = StringUtils.trim(subject);
            text = text.replace(subject,"");
            text = text.replaceAll("\\s+", " ");
        }

        text = text.replaceAll("(\\bis\\b)", "");

        Pattern act_pattern = Pattern.compile("(\\b([a-z]+\\b) )+");
        Matcher act_matcher = act_pattern.matcher(text);
        if (act_matcher.find()){
            action= act_matcher.group();
            action = StringUtils.trim(action);
            text = text.replace(action,"");
        }

        if (!StringUtils.isBlank(text)){
            text = text.replaceAll("[',.\\-\\&\\:]","");
            text= text.replaceAll("\\([A-Za-z ]*\\)","");
            text = text.replaceAll("\\?","");
            text = text.replaceAll("\\s+", " ");
            object = StringUtils.trim(text);
        }




        sanitized_token.put("subject", subject);
        sanitized_token.put("action", action);
        sanitized_token.put("object", object);

        return sanitized_token;
    }


    public boolean match(String text, Map<String, String> token_fact){
        String action = token_fact.get("action");
        String subject = token_fact.get("subject");
        String object = token_fact.get("object");

        if (token_fact.get("subject").matches("Nobel .*") || token_fact.get("action").matches("innovation place")){
            subject = token_fact.get("object");
            object = token_fact.get("subject");
        }
        boolean match = false;

        String strPattern = stringPattern(action);

        String subtext;

        int search_range = StringUtils.ordinalIndexOf(text, ".", 2);
        if (search_range == -1)
        {
            Watson watson = new Watson();
            text = watson.searchUpdate(token_fact);
            search_range = StringUtils.ordinalIndexOf(text, ".", 2);
        }
        if (search_range == -1){
            subtext = text;
        }else {
            subtext = text.substring(0, search_range);
        }




        subtext = Normalizer.normalize(subtext, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        subtext = subtext.replaceAll("\\.","");  //removing fullstops since fullstops were removed as well in object
        subtext = subtext.replaceAll("-"," ");

        Pattern pattern = Pattern.compile(strPattern + object);
        Matcher matcher = pattern.matcher(subtext);



        if (matcher.find()){
            match =true;
        }else {
            String[] obj = object.split(" ");
            if (obj.length>= 2)
            {
                String new_obj =obj[0]+" "+obj[1];   // Use this format for author its working
                pattern = Pattern.compile(strPattern + new_obj);
                matcher = pattern.matcher(subtext);
                if (matcher.find()){
                    match =true;
                }
                else {

                    matcher = pattern.matcher(subtext);

                    if (matcher.find()){
                        match =true;
                    }
                    else {
                        text = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""); // just added
                        matcher = pattern.matcher(text);
                        if (matcher.find()){
                            match =true;
                        }
                        else{
                            match =false;
                        }
                    }

                }
            }

        }


        return match;
    }


    public String stringPattern(String action){
        String strPattern ="";
        if (action.equals("author")){
            strPattern = ".*(novel by|written by|by author|by the|by|novel|written|book|author|Author).{0,200}";
        }else if (action.equals("award")||action.equals("honour")){
            strPattern = ".*(award|awarded|received|awards|Awards|honoured).{0,200}";
        }else if (action.equals("better half")|| action.equals("spouse")){
            strPattern = ".*(married|marry|wife|husband|spouse|spouse\\(s\\)).{0,200}";
        }else if (action.equals("birth place")||action.equals("nascence place")){
            strPattern = ".*(born in|born|origin|originated|Born).{0,200}";
        }else if (action.equals("death place") || action.equals("last place")){
            strPattern = ".*(died|dead|death|killed|Died).{0,200}";
        }else if (action.equals("foundation place")){
            strPattern = "[A-Za-z0-9 \\-]*(founded).{0,200}";
        }else if (action.equals("team")){
            strPattern = ".*(player|present).{0,200}";
        }else if (action.equals("subordinate")||action.equals("subsidiary")){
            strPattern = ".*(fate|bought|subsidiary|subsidiaries|Subsidiaries|Subsidiary|Owner|owned|owner).{0,200}";
        }else if (action.equals("stars")||action.equals("role")||action.equals("generator")){
            strPattern = ".*";
        }else if (action.equals("office")){
            strPattern = ".{0,200}";
        }else if (action.equals("squad")){
            strPattern = ".{0,200}";
        }else {
            strPattern = ".*";
        }

        return strPattern;
    }


}
