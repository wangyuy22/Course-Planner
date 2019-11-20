import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JFrame;

public class Main {
    
    public static void main(String args[]) {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter Engineering Major:");
        String major = keyboard.nextLine();
        System.out.println("Enter Degree Type:");
        String deg = keyboard.nextLine();
        keyboard.close();
        ReqSearch Penn = new ReqSearch();
        System.out.println("Calculating...");

        TreeMap<String, ArrayList<String>> map = Penn.getCourses(major, deg);
        System.out.println("Schedule:");
        TopSort a = new TopSort(map);
        LinkedList<String> ans = a.getTopSort();
        for (String i: ans) {
            System.out.println(i);
        }       
        JFrame t = new JFrame("Major Prerequisite Visualizer");
        Visualizer v = new Visualizer(map);
        t.add(v, BorderLayout.CENTER);

        t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        t.setSize(1300, 1300);
        t.setVisible(true);
    }
}