package hw9;


import java.util.List;

/**
 * Message that are passed around the actors are usually immutable classes.
 * This class is used to pass the information between actors.
 */

public class Message {

    private double value;
    private String string;
    private int index;
    private List<?> list;

    public Message(int index, double value,String string,  List<?> list){
        this.value = value;
        this.string = string;
        this.index = index;
        this.list = list;

    }

    public double getValue() {
        return value;
    }

    public String getString() {
        return string;
    }

    public int getIndex() {
        return index;
    }

    public List<?> getList() {
        return list;
    }
}
