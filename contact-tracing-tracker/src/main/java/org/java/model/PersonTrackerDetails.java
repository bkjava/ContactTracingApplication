package org.java.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PersonTrackerDetails  implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	private List<String> nameList;	
		
	public List<String> getNameList() {
		return nameList;
	}
	public void setNameList(List<String> nameList) {
		this.nameList = nameList;
	}
	public void addName(String name) {
		if(nameList == null)
			nameList = new ArrayList<String>();
		nameList.add(name);
	}

	public PersonTrackerDetails clone() {		
		try {
			return (PersonTrackerDetails)super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}		
		return null;		
	}
}
