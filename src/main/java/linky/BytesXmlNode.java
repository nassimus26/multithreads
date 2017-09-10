package linky;

import linky.api.XmlNode;

import java.util.Arrays;

public class BytesXmlNode {
    byte[] chunk;
    XmlNode xmlNode;

    public BytesXmlNode(byte[] chunk, XmlNode xmlNode) {
        this.chunk = chunk;
        this.xmlNode = xmlNode;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public XmlNode getXmlNode() {
        return xmlNode;
    }

    @Override
    public String toString() {
        return "BytesXmlNode{" +
                "chunk=" + new String(chunk) +
                ", xmlNode=" + xmlNode +
                '}';
    }
}