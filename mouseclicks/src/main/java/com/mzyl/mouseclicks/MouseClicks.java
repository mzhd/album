package com.mzyl.mouseclicks;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MouseClicks {
    Robot robot;
    private boolean start;

    public MouseClicks() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final MouseClicks mouseClicks = new MouseClicks();

        JFrame frame = new JFrame("鼠标连点");

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();

        JButton button=new JButton("置顶");
//        button.addMouseListener(new MouseListener() {
//            @Override
//            public void mouseClicked(MouseEvent mouseEvent) {
//                if (mouseEvent.getButton()==MouseEvent.BUTTON1){
//                    JFrame.getWindows()[0].setAlwaysOnTop(true);
//                }
//            }
//
//            @Override
//            public void mousePressed(MouseEvent mouseEvent) {
//
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent mouseEvent) {
//
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent mouseEvent) {
//
//            }
//
//            @Override
//            public void mouseExited(MouseEvent mouseEvent) {
//
//            }
//        });
       // panel.add(button);
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F10) {

                mouseClicks.startMouseClicks();
                }
                if (keyEvent.getKeyCode() == KeyEvent.VK_F12) {
                    mouseClicks.closeMouseClicks();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
        frame.setContentPane(panel);

        frame.setVisible(true);
    }

    void startMouseClicks() {
       start=true;
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (start) {
                    System.out.println("点击");

                    robot.setAutoDelay(500);

                    robot.mousePress(InputEvent.BUTTON1_MASK);

                    robot.delay(100);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                }
            }
        };
        thread.start();



    }
    void closeMouseClicks(){
        start=false;
    }
}
