package geekgfs.test;

import geekgfs.util.MyIDUtil;

public class Test {
    public static void main(String[] args) {
        System.out.println("f");
        System.out.println("sdg");
    }

    private static class IDUtilDemo extends Thread{
        MyIDUtil instance = MyIDUtil.getInstance();
        @Override
        public void run() {
            System.out.println(instance.generate());
        }
    }
}
