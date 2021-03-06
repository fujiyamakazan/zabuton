package net.nanisl.zabuton.example;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Component
public class ExampleComponent implements Serializable {
	private static final long serialVersionUID = 1L;

	private String field1;

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

}
