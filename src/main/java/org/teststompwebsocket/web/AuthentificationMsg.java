package org.teststompwebsocket.web;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("serial")
public class AuthentificationMsg implements Serializable {

    private String type;

    private String sequence_id;

    private Map<String, String> data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSequence_id() {
        return sequence_id;
    }

    public void setSequence_id(String sequence_id) {
        this.sequence_id = sequence_id;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AuthentificationMsg [type=" + type + ", sequence_id=" + sequence_id
            + ", data=" + data + "]";
    }

}
