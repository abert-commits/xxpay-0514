package org.xxpay.manage.common.util;

import java.util.Random;

/**
 * @description
 * @date 2017/11/14 11:15 create
 */
public class SeqUtils
{

    /** 开始时间截 (2015-01-01) */
    private static final long twepoch = 1420041600000L;

    /** 机器id所占的位数 */
    private static final long workerIdBits = 5L;

    /** 数据标识id所占的位数 */
    private static final long datacenterIdBits = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 支持的最大数据标识id，结果是31 */
    private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** 序列在id中占的位数 */
    private static final long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    private static final long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    private static final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间截向左移22位(5+5+12) */
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** 工作机器ID(0~31) */
    public static long workerId = 0L;

    /** 数据中心ID(0~31) */
    public static long datacenterId = 0L;

    /** 毫秒内序列(0~4095) */
    private static long sequence = 0L;

    /** 上次生成ID的时间截 */
    private static long lastTimestamp = -1L;

    private static Random random = new Random();

    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public static synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
//        System.out.println(lastTimestamp);
//        System.out.println(timestamp);
//        System.out.println(twepoch);
//        System.out.println(timestampLeftShift);
//        System.out.println(datacenterId);
//        System.out.println(datacenterIdShift);
//        System.out.println("=="+((timestamp - twepoch) << timestampLeftShift));
//        System.out.println(""+(datacenterId << datacenterIdShift));
//        System.out.println(sequence);
        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    public static synchronized String nextIdStr(){
        return nextId()+"";
    }
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 生成随机范围长度的随机数 不以0开头
     * @param min 最小长度
     * @param max 最大长度
     * @param repeat 数字是否可重复 true-可以重复 false-不可重复
     * @return 随机数 (当长度大于10 设置不可重复时 直接返回null)
     */
    public static synchronized String randomRoomNo(int min, int max, boolean repeat) {
        StringBuffer sb = new StringBuffer();
        int randLength = random.nextInt(max - min + 1);
        int i = 0;
        if (!repeat && max > 10) {
            return null;
        }
        while (i < min + randLength) {
            int num = random.nextInt(10);
            if (i == 0 && num == 0) {
                continue;
            }
            if (!repeat) {
                if (sb.indexOf(num + "") > -1) {
                    continue;
                }
            }
            sb.append(num);
            i++;
        }
        return sb.toString();
    }
}
