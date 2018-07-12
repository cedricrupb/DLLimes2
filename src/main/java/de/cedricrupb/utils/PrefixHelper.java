package de.cedricrupb.utils;

import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Class to help resolvePrefixes and revert them if needed. Operations related to prefix are handled here.
 *
 * @author Cedric Richter
 */


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

    private static List<Map.Entry<String, String>> sort(Map<String, String> prefix){
        List<Map.Entry<String, String>> list = new ArrayList<>(prefix.entrySet());

        Comparator<Map.Entry<String, String>> comp = Collections.reverseOrder(
                new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                        return o1.getValue().length() - o2.getValue().length();
                    }
                }
        );
        Collections.sort(list, comp);
        return list;
    }


    public static String revertPrefix(String str, Map<String, String> prefix){

        final Matcher matcher = Pattern.compile("<\\S+>").matcher(str);

        Map<String, String> replace = new HashMap<>();

        while(matcher.find()){
            String entity = matcher.group();
            String replaceStr = entity.substring(1, entity.length()-1);

            for(Map.Entry<String, String> p: sort(prefix)){
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
        return PrefixHelper.revertSinglePrefix(str, prefix, false);
    }

    public static String revertSinglePrefix(String str, Map<String, String> prefix, boolean quote){
        boolean prefixed = false;
        for(Map.Entry<String, String> p: sort(prefix)){
            prefixed |= str.contains(p.getValue());
            str = str.replaceAll(Pattern.quote(p.getValue()), p.getKey()+":");
        }

        if(!prefixed && quote){
            return "<"+str+">";
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
