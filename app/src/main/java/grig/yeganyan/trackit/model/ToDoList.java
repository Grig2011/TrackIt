package grig.yeganyan.trackit.model;

public class ToDoList {
    public String id;
    public String Time;
    public String Title;
    public String Description;

    public ToDoList(){}

    public ToDoList(String id,String Time, String Title,String Description){
        this.id = id;
        this.Time = Time;
        this.Title = Title;
        this.Description = Description;
    }

}
