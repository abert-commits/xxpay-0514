package org.xxpay.core.common.util;

import org.xxpay.core.entity.Nginx;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WeightUtil<T> {
    private static final MyLog _log = MyLog.getLog(WeightUtil.class);

    private int getServerByWeight(int[] weightArr) {
        int[][] randArr = new int[weightArr.length][2];
        int totalRank = 0;
        int index = 0;
        for (int i = 0; i < weightArr.length; i++) {
            if (weightArr[i] <= 0) {
                continue;
            }

            totalRank += weightArr[i];
            randArr[i][0] = i;
            randArr[i][1] = totalRank;
        }

        int hitRank = new Random().nextInt(totalRank) + 1;//[1, totalRand]
        for (int i = 0; i < randArr.length; i++) {
            if (hitRank <= randArr[i][1]) {
                return randArr[i][0];
            }
        }

        return randArr[0][0];
    }

    public T choose(List<T> serverList) {
        if (null == serverList) {
            return null;
        }

        int[] weightArr = new int[serverList.size()];

        for (int i = 0; i < serverList.size(); i++) {
            Integer weights = getWeights(serverList.get(i));
            if (getWeights(serverList.get(i)) > 0) {
                weightArr[i] = weights;
            }
        }

        if (weightArr.length == 0) {
            return null;
        }

        int chosenIndex = getServerByWeight(weightArr);
        return serverList.get(chosenIndex);
    }

    public Integer getWeights(T t) {
        try {
            Class<? extends Object> tClass = t.getClass();

            //整合出 getId() 属性这个方法
            Method m = tClass.getMethod("getWeights");

            //调用这个整合出来的get方法，强转成自己需要的类型
            Integer Weights = (Integer) m.invoke(t);

            //成功通过 T 泛型对象取到具体对象的 id ！
            return Weights;
        } catch (Exception e) {
            _log.info("没有这个属性");
            return null;
        }
    }

    public static void main(String[] args) {
        int i20 = 0;
        int i30 = 0;
        int i60 = 0;
        for (int i = 0; i < 110000; i++) {
            List<Nginx> nginxes = new ArrayList<Nginx>();
            Nginx nginx1 = new Nginx();
            nginx1.setId(1);
            nginx1.setWeights(20);
            nginxes.add(nginx1);

            Nginx nginx2 = new Nginx();
            nginx2.setId(2);
            nginx2.setWeights(30);
            nginxes.add(nginx2);

            Nginx nginx3 = new Nginx();
            nginx3.setId(3);
            nginx3.setWeights(60);
            nginxes.add(nginx3);
            WeightUtil<Nginx> wt = new WeightUtil<Nginx>();
            Nginx server = wt.choose(nginxes);
            if (server.getId() == 1) {
                i20++;
            }
            if (server.getId() == 2) {
                i30++;
            }
            if (server.getId() == 3) {
                i60++;
            }
        }
        System.out.println(i20);
        System.out.println(i30);
        System.out.println(i60);
    }
}