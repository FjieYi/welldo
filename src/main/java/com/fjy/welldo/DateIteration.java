package com.fjy.welldo;

import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * @author  fjy
 *
 * 时间边界计算表达式，由一次次迭代连接而成，如：(xxx)(xxx)...，最后可带有一个[n1-n2]，对最终日期yyyy-MM-dd字符串进行部分截取，前闭后开。
 * 单次日期迭代可能场景：
 * (D+23): 23天后
 * (D=y23): 设置成当年第23天
 * (D=s23): 设置成当季第23天
 * (D=m23): 设置成当月第23天
 *
 * (W=6): W只支持=，数字范围1-7；含义：把日期变换到指定日期（不包括）前最后一个周六
 *
 * (M+11): 11个月后
 * (M=11): 设置成当年第11个月（年/dayOfMonth保持不变）
 *
 * (Y+11): 11年后
 * (Y=2011): 年份设置成2011
 */
public class DateIteration {

    public static String bound(String equation) {
        return bound(equation, LocalDate.now().toString());
    }

    public static String bound(String equation, String baseDate) {
        LocalDate base = LocalDate.parse(baseDate);
        int startIdx = 0;
        int endIdx = -1;
        String part = null;
        while ((startIdx = equation.indexOf("(", endIdx + 1)) > -1) {
            endIdx = equation.indexOf(")", startIdx + 1);
            part = equation.substring(startIdx + 1, endIdx);
            base = transformOnce(part, base);
        }
        String r = base.toString();
        if((startIdx = equation.indexOf("[", endIdx + 1)) > -1){
            endIdx = equation.indexOf("]", startIdx + 1);
            part = equation.substring(startIdx + 1, endIdx);
            String[] startEnd = part.split("-");
            r = r.substring(Integer.parseInt(startEnd[0]), Integer.parseInt(startEnd[1]));
        }
        return r;
    }

    private static LocalDate transformOnce(String equation, LocalDate baseDate) {
        if(equation.startsWith("D")){
            if(equation.charAt(1) == '=') {
                if(equation.charAt(2) == 'm'){
                    return baseDate.withDayOfMonth(Integer.parseInt(equation.substring(3)));
                }else if(equation.charAt(2) == 's'){
                    return firstDayOfQuarter(baseDate).plusDays(Integer.parseInt(equation.substring(3)) - 1);
                }else if(equation.charAt(2) == 'y'){
                    return baseDate.withDayOfYear(Integer.parseInt(equation.substring(3)));
                }else {
                    throw new InvalidParameterException("set day only support 3 types: m(dayOfMonth)/s(dayOfQuarter)/y(dayOfYear)");
                }
            }else if(equation.charAt(1) == '+' || equation.charAt(1) == '-'){
                return baseDate.plusDays(Integer.parseInt(equation.substring(1)));
            }else{
                throw new InvalidParameterException("modify day only support 3 types: +/-/=");
            }
        }else if(equation.startsWith("M")){
            if(equation.charAt(1) == '=') {
                return baseDate.withMonth(Integer.parseInt(equation.substring(2)));
            }else if(equation.charAt(1) == '+' || equation.charAt(1) == '-'){
                return baseDate.plusMonths(Integer.parseInt(equation.substring(1)));
            }else{
                throw new InvalidParameterException("modify month only support 3 types: +/-/=");
            }
        }else if(equation.startsWith("Y")){
            if(equation.charAt(1) == '=') {
                return baseDate.withYear(Integer.parseInt(equation.substring(2)));
            }else if(equation.charAt(1) == '+' || equation.charAt(1) == '-'){
                return baseDate.plusYears(Integer.parseInt(equation.substring(1)));
            }else{
                throw new InvalidParameterException("modify year only support 3 types: +/-/=");
            }
        }else if(equation.startsWith("W")){
            if(equation.charAt(1) == '=') {
                DayOfWeek dow = baseDate.getDayOfWeek();
                int c = Integer.parseInt(equation.substring(2));
                int k = 0;
                switch (dow) {
                    case MONDAY: k = 7; break;
                    case TUESDAY: k = 1; break;
                    case WEDNESDAY: k = 2; break;
                    case THURSDAY: k = 3; break;
                    case FRIDAY: k = 4; break;
                    case SATURDAY: k = 5; break;
                    case SUNDAY: k = 6; break;
                }
                switch (c) {
                    case 1: return baseDate.minusDays(k);
                    case 2: return baseDate.minusDays(minusOne(k));
                    case 3: return baseDate.minusDays(minus(k, 2));
                    case 4: return baseDate.minusDays(minus(k, 3));
                    case 5: return baseDate.minusDays(minus(k, 4));
                    case 6: return baseDate.minusDays(minus(k, 5));
                    case 7: return baseDate.minusDays(minus(k, 6));
                    default: throw new InvalidParameterException("set week parameter support only int 1-7");
                }
            }else {
                throw new InvalidParameterException("modify week only support 1 types: =");
            }
        }else {
            throw new InvalidParameterException("Date type flags support only: Y/M/W/D");
        }
    }

    private static int minus(int base, int i){
        for(int k = 0; k < i; k++){
            base = minusOne(base);
        }
        return base;
    }

    private static int minusOne(int i){
        if(i == 1) return 7;
        else if(i > 1 && i < 8) return i - 1;
        else throw new InvalidParameterException("support only int 1-7");
    }

    private static LocalDate firstDayOfQuarter(LocalDate base){
        int m = base.getMonthValue();
        return base.withMonth((m - 1)/3*3 + 1).withDayOfMonth(1);
    }
}

