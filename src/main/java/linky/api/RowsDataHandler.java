package linky.api;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RowsDataHandler {
    
    private transient List<XmlNode> rows;

    public RowsDataHandler() {
        super();
        this.rows = new CopyOnWriteArrayList<XmlNode>();
    }
    
    public void setRows(List<XmlNode> rows) {
        this.rows = rows;
    }
    public List<XmlNode> getRows() {
        return rows;
    }
    public XmlNode get(int index) {
        return rows.get(index);
    }
    public void clearRows(){
        if (rows!=null)
        synchronized (this) {            
            rows.clear();
        }
    }
    public void removeRow(XmlNode node){
        
        XmlNode[] newRows=null;
        if (!rows.isEmpty())
        synchronized (this) {
            if (rows.isEmpty())
                return;
            newRows = new XmlNode[rows.size()-1];
            int m = 0;
            for (int i=0;i<newRows.length;i++){
                XmlNode row = rows.get(i);
                if (row!=node)
                    newRows[i-m] = row;
                else
                    m=1;
            }           
            //rows = new CopyOnWriteArrayList<XmlNode>(newRows);
            rows.clear();
            rows.addAll(Arrays.asList( newRows ));
        }    
    }
    
    public void add(XmlNode node){
        synchronized (this) {
            rows.add(node);    
        }        
    }
    
    public int size(){
        return rows.size();
    }    
}