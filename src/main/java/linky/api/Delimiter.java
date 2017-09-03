package linky.api;

import java.io.Serializable;

public class Delimiter  implements Serializable{
    private String tag;
    private String tagStart;
    private String tagEnd;
   // private int id;
    public Delimiter(){
        
    }
    public Delimiter(String tag, String startTag, String endTag) {
        super();
        this.tag = tag;
        this.tagStart = startTag;
        this.tagEnd = endTag;        
    }
    
    public Delimiter(String tag) {
        super();
        this.tag = tag;
        this.tagStart = "<" + tag + ">";
        this.tagEnd = "</" + tag + ">";
      //  this.id = tag.hashCode();
    }

    public String getTag() {
        return tag;
    }
    public String getTagStart() {
        return tagStart;
    }
    public String getTagEnd() {
        return tagEnd;
    }
    
    @Override
    public String toString() {
        return "Delimiter [tag=" + tag + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Delimiter other = (Delimiter) obj;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }
     
    
}
