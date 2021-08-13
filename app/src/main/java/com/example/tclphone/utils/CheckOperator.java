package com.example.tclphone.utils;


import java.util.regex.Pattern;

public class CheckOperator {
    /**
     * Regex of exact mobile.
     * <p>china mobile: 134(0-8), 135, 136, 137, 138, 139, 147, 150, 151, 152, 157, 158, 159, 178, 182, 183, 184, 187, 188, 198</p>
     * <p>china unicom: 130, 131, 132, 145, 155, 156, 166, 171, 175, 176, 185, 186</p>
     * <p>china telecom: 133, 153, 173, 177, 180, 181, 189, 199, 191</p>
     * <p>global star: 1349</p>
     * <p>virtual operator: 170</p>
     * ^((13[0-9])|(14[57])|(15[0-35-9])|(16[6])|(
     *
     * 17[0135-8])|(18[0-9])|(19[189]))\\d{8}$";
     */
    public String checkOperator(String number){
        final String chinaMobile = "^(134[0-8]\\d{7})|(((13[5-9])|(147)|(15[012789])|(178)|(18[23478])|198)\\d{8})$";
        final String chinaUnicom = "^((13[012])|(145)|(15[56])|166|(17[156])|(18[56]))\\d{8}$";
        final String chinaTelecom = "^(133|153|173|177|180|181|189|199|191)\\d{8}$";
        final String globalStar = "^(1349)\\d{7}$";
        final String virtualOperator = "^(170)\\d{8}$";

        if (Pattern.matches(chinaMobile,number)){
            return "中国移动";
        }
        if (Pattern.matches(chinaUnicom,number)){
            return "中国联通";
        }
        if (Pattern.matches(chinaTelecom,number)){
            return "中国电信";
        }
        if (Pattern.matches(globalStar,number)){
            return "全球通";
        }
        if (Pattern.matches(virtualOperator,number)){
            return "虚拟运营商";
        }
        else return "副卡";
    }
}
