package com.kaitusoft.ratel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author frog.w
 * @version 1.0.0, 2020/2/18
 *          <p>
 *          write description here
 */
public class Address {

    /**
     * 解析地址
     * @author cjw
     * @param address
     * @return
     */
    public static List<Map<String,String>> parse(String address){
        String regex="(?<province>[^省]+省|.+自治区)(?<city>[^自治州]+自治州|[^市]+市|[^盟]+盟|[^地区]+地区|.+区划)(?<county>[^市]+市|[^县]+县|[^旗]+旗|.+区)?(?<town>[^区]+区|.+镇)?(?<village>.*)";
        Matcher m= Pattern.compile(regex).matcher(address);
        String province=null,city=null,county=null,town=null,village=null;
        List<Map<String,String>> list=new ArrayList<Map<String,String>>();
        Map<String,String> row=null;
        while(m.find()){
            row=new LinkedHashMap<String,String>();
            province=m.group("province");
            row.put("province", province==null?"":province.trim());
            city=m.group("city");
            row.put("city", city==null?"":city.trim());
            county=m.group("county");
            row.put("county", county==null?"":county.trim());
            town=m.group("town");
            row.put("town", town==null?"":town.trim());
            village=m.group("village");
            row.put("village", village==null?"":village.trim());
            list.add(row);
        }
        return list;
    }
    public static void main(String[] args) {
        System.out.println("地址是：" + parse("宁夏回族自治区银川市灵武市回水滩桥头1户"));
    }
}
