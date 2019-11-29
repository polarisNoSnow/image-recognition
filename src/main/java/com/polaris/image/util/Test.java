package com.polaris.image.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        System.out.println(123);
        BufferedImage bufferedImage = new BufferedImage(1,1,1);
        List ss = new ArrayList<>();
        int i = 0;
        while (true) {
            ss.add(bufferedImage);
            System.out.println(i++);
        }

    }
}
