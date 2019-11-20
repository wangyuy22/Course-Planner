import java.util.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;

/**
 * Inner class to represent nodes
 * 
 *
 */
class Node {
    int x, y;
    String name;
    int height = 30, width = 30; // default widths and heights
    
    /**
     * 
     * @param course the course this node represents
     * @param posX the x position of the node
     * @param posY the y position of the node
     */
    public Node(String course, int posX, int posY) {
        x = posX;
        y = posY;
        name = course;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public String getName() {
        return name;
    }
    
    
    /**
     * Draws the nodes as ovals labelled with the course they represent 
     * @param g
     */
    public void draw(Graphics g) { 
        FontMetrics f = g.getFontMetrics();
        int nodeHeight = Math.max(height, f.getHeight());
        g.setColor(Color.black);
        int nodeWidth = Math.max(width, f.stringWidth(name) + width/2);
        g.setColor(Color.white);
        g.fillOval(x-nodeWidth/2, y-nodeHeight/2, 
               nodeWidth, nodeHeight);
        g.setColor(Color.black);
        g.drawOval(x-nodeWidth/2, y-nodeHeight/2, 
               nodeWidth, nodeHeight);
        
        g.drawString(name, x-f.stringWidth(name)/2,
             y+f.getHeight()/2);
    }
}

/**
 * Inner class to represent edges
 * 
 *
 */
class Edge {
    int x1, y1, x2, y2;
    
    /**
     * 
     * @param x1 beginning x position
     * @param y1 beginning y position
     * @param x2 end x position
     * @param y2 end y position
     */
    public Edge(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;    
    }
            
            
    /**
     * Draws an arrow to represent a directed edge using the x and y positions from the constructor
     * @param g1
     */
    public void draw(Graphics g1) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[] {len, len-4, len-4, len},
                      new int[] {0, -4, 4, 0}, 4);
    }

}

@SuppressWarnings("serial")
public class Visualizer extends JPanel{
    
    int width = 30;
    int height = 30;
    String course, deg;
    ArrayList<Node> nodes;
    ArrayList<Edge> edges;

    /**
     * 
     * @param map of prereqs to required courses, outputted by the ReqSearch
     */
    public Visualizer(TreeMap<String, ArrayList<String>> map) {

        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        
        drawCourses(map);
    }
    
    /**
     * Creates nodes and edges 
     * @param map
     */
    public void drawCourses(TreeMap<String, ArrayList<String>> map) {
        Set<String> courses = map.keySet();
        int xVal = width;
        int yVal = height;
        for (String c: courses) {
            Node n = new Node(c, xVal, yVal);
            nodes.add(n);
            xVal += 3 * width;
            ArrayList<String> reqs = map.get(c);
            for (String req: reqs) {
                if (!req.equals("Prereqs") && !req.equals("Coreqs")) {
                    Node m = new Node(req, xVal, yVal);
                    nodes.add(m);
                    yVal += 3 * width;
                    Edge e = new Edge(m.getX() - (2*width/3), m.getY(), n.getX() + (2*width/3), n.getY());
                    edges.add(e);
                }
            }
            xVal = width;
        }

    }
    
    /**
     * Draws the nodes and edges and the background
     */
    public void paintComponent(Graphics g) {
        g.setColor(java.awt.Color.white);
        g.fillRect(0,0,getWidth(),getHeight());
        for (Node n: nodes) {
            n.draw(g);
            repaint();
        }
        
        for (Edge e: edges) {
            e.draw(g);
            repaint();
        }
    } 
}
    

