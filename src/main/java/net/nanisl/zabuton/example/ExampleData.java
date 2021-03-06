package net.nanisl.zabuton.example;
import java.io.Serializable;

public class ExampleData implements Serializable  {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private String note;

	public ExampleData(int id, String name, String note) {
		this.id = id;
		this.name = name;
		this.note = note;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Override
	public String toString() {
		return "ExampleData [id=" + id + ", name=" + name + ", note=" + note + "]";
	}
}
