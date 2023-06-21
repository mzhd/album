package com.mzyl.test;

public class EnumTest {
    public static void main(String[] args) {
        EnumTest enumTest = new EnumTest();
        enumTest.setDrawType(DrawType.LINE);
        enumTest.printDrawType();

        enumTest.setDrawType(DrawType.MOSAIC);
        enumTest.printDrawType();

        enumTest.setDrawType(DrawType.LINE);
        enumTest.printDrawType();

        enumTest.setDrawType(DrawType.MOSAIC);
        enumTest.printDrawType();

        enumTest.setDrawType(DrawType.MOSAIC);
        enumTest.printDrawType();

        enumTest.setDrawType(DrawType.LINE);
        enumTest.printDrawType();
    }
    public void printDrawType() {
        switch (drawType) {
            case LINE:
                System.out.println("line");
                break;
            case MOSAIC:
                System.out.println("mosaic");
                break;
        }
    }
    DrawType drawType;

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    public enum DrawType {
        LINE(0),
        MOSAIC(1),
        OTHER(2);

        DrawType(int type) {
            this.type = type;
        }

        int type;

    }
}
