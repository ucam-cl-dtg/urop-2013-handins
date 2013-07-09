package uk.ac.cam.sup.models;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "DemoModel")
public class DemoModel {
	private String name;
	private int id;
	
	public DemoModel() {}
	public DemoModel(String n){name = n;}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	public int getId() {return id;}
	public void setId(int i) {id = i;}
	
	public String getName() {return name;}
	public void setName(String n) {name = n;}
	
}
