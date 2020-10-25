package org.java.model;

import java.io.Serializable;

public class PersonDetail  implements Serializable, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;	
	private Integer x;
	private Integer y;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}
	
	public PersonDetail clone() {		
		try {
			return (PersonDetail)super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}		
		return null;		
	}
}
