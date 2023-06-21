package com.mzyl.test;

public class MyClass {
    public static void main(String[] args) {
//        Map map=new HashMap();
//        int[] arr=new int[2];
//        arr[0]=1;
//        arr[1]=2;
//        map.put(0,arr);
//
//        boolean a=true;
//        boolean b=false;
//        boolean c=false;
//        c=a|b;
//        System.out.println(c);

        float right = 300;
        float left = 150;
        float x0 = (right - left)   / 3;
        float x = (right - left) * 2 / 3;
        float x1 = (right - left) * 2f / 3;
        float x2 = (right - left) * 2f / 3f;
        System.out.println(x0);
        System.out.println(x);
        System.out.println(x1);
        System.out.println(x2);

    }
}
