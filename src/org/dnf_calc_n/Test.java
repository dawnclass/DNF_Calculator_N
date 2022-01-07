package org.dnf_calc_n;

import java.util.ArrayList;
import java.util.HashMap;

public class Test {

    public static void main(String[] args){

        new Thread(Test::testRun).start();

    }

    private static void testRun(){
        HashMap<Integer, Long> testInt = new HashMap<>();
        HashMap<Double, Long> testDouble = new HashMap<>();
        HashMap<String, Long> testString = new HashMap<>();
        HashMap<Long, Long> testLong = new HashMap<>();
        ArrayList<Integer> indexInt = new ArrayList<>();
        ArrayList<Double> indexDouble = new ArrayList<>();
        ArrayList<String> indexString = new ArrayList<>();
        ArrayList<Long> indexLong = new ArrayList<>();
        for (int i=0;i<10000000;i++){
            testInt.put(i, (long)i);
            testDouble.put((double) i, (long)i);
            testString.put(String.valueOf(i), (long)i);
            testLong.put((long) i, (long)i);
            indexInt.add(i);
            indexDouble.add((double) i);
            indexString.add(String.valueOf(i));
            indexLong.add((long) i);
        }
        long count;

        long beforeTime = System.currentTimeMillis();
        count = 0;

        for (int in : indexInt){
            count += testInt.get(in);
            for(long j=0;j<1000;j++){
                count -= j;
            }
        }

        long time1 = System.currentTimeMillis();
        System.out.println("Int 시간차이(m) : "+(time1 - beforeTime));
        count = 0;

        for (double in : indexDouble){
            count += testDouble.get(in);
            for(long j=0;j<1000;j++){
                count -= j;
            }
        }

        long time2 = System.currentTimeMillis();
        System.out.println("Double 시간차이(m) : "+(time2 - time1));
        count = 0;

        for (String in : indexString){
            count += testString.get(in);
            for(long j=0;j<1000;j++){
                count -= j;
            }
        }

        long time3 = System.currentTimeMillis();
        System.out.println("String 시간차이(m) : "+(time3 - time2));
        count = 0;

        for (Long in : indexLong){
            count += testLong.get(in);
            for(long j=0;j<1000;j++){
                count -= j;
            }
        }

        long time4 = System.currentTimeMillis();
        System.out.println("Long 시간차이(m) : "+(time4 - time3));
    }

}
