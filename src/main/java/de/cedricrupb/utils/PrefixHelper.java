package de.cedricrupb.utils;

import org.semanticweb.owlapi.model.IRI;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixHelper {

    public static String resolvePrefix(String str, Map<String, String> prefix){

        final Matcher matcher = Pattern.compile("\\w+\\:").matcher(str);

        Map<String, String> replace = new HashMap<>();

        while(matcher.find()){
            String entity = matcher.group();
            String replaceStr = entity.substring(0, entity.length()-1);

            for(Map.Entry<String, String> p: prefix.entrySet()){
                if(replaceStr.contains(p.getValue())){
                    replaceStr = replaceStr.replace(p.getKey(), p.getValue());
                    replaceStr = "<"+replaceStr+">";
                    replace.put(entity, replaceStr);
                    break;
                }
            }
        }

        for(Map.Entry<String, String> e: replace.entrySet())
            str = str.replaceAll(Pattern.quote(e.getKey()), e.getValue());

        return str;
    }

    public static String revertPrefix(String str, Map<String, String> prefix){

        final Matcher matcher = Pattern.compile("<\\S+>").matcher(str);

        Map<String, String> replace = new HashMap<>();

        while(matcher.find()){
            String entity = matcher.group();
            String replaceStr = entity.substring(1, entity.length()-1);

            for(Map.Entry<String, String> p: prefix.entrySet()){
                if(replaceStr.contains(p.getValue())){
                    replaceStr = replaceStr.replace(p.getValue(), p.getKey()+":");
                    replace.put(entity, replaceStr);
                    break;
                }
            }
        }

        for(Map.Entry<String, String> e: replace.entrySet())
            str = str.replaceAll(Pattern.quote(e.getKey()), e.getValue());

        return str;
    }

    public static String revertSinglePrefix(String str, Map<String, String> prefix){
        for(Map.Entry<String, String> p: prefix.entrySet()){
            str = str.replaceAll(Pattern.quote(p.getValue()), p.getKey()+":");
        }
        return str;
    }

    public static String resolveSinglePrefix(String s, Map<String, String> prefixes){
        String[] split = s.split(":");
        if(split.length == 2){
            String prefix = split[0];

            if(prefixes.containsKey(prefix)){
                prefix = prefixes.get(prefix);
                return prefix + split[1];
            }
        }
        return s;
    }

}
