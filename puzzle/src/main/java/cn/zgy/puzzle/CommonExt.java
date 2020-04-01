package cn.zgy.puzzle;

import java.util.Random;

public class CommonExt {
    private static Random random = new Random();

    /**
     * 随机获取一个数字，包含max和min
     * @param max 最大值
     * @param min 最小值
     * @return
     */
    public static int getRandom(int max, int min) {
        return random.nextInt(max + 1 - min) + min;
    }

}
