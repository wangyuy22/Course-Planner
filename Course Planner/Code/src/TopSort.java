import java.util.*;

public class TopSort {
    private Set<String> vis;
    private TreeMap<String, ArrayList<String>> adj;
    private LinkedList<String> top; //this linkedlist will act as a stack for the topological sort
    public TopSort(TreeMap<String, ArrayList<String>> adj){
        vis = new TreeSet<String>();
        top = new LinkedList<String>();
        this.adj = adj;
        for (String temp: adj.keySet()) {
            if(temp == "" || temp == "Prereqs" || temp == "Coreqs") continue;
            if(!vis.contains(temp)) dfs(temp);
        }
    }
    
    private void dfs(String cc){
        vis.add(cc);
        if(adj.get(cc) == null) return;
        for (String i: adj.get(cc)) {
            if(i == "" || i == "Prereqs" || i == "Coreqs") continue;
            if (!vis.contains(i)) {
                dfs(i);
            }
        }
        top.addFirst(cc);
    }
    
    public LinkedList<String> getTopSort() {
        return top;
    }
}
