package com.flores.projects.bind;

public 	class Athlete {
	String name;
	int age;
	Sport sport;

	public Athlete(){}
	public Athlete(String name, int age, Sport sport) {
		this.name = name;
		this.age = age;
		this.sport = sport;
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public int getAge() { return age; }
	public void setAge(int age) { this.age = age; }
	public Sport getSport() { return sport; }
	public void setSport(Sport sport) { this.sport = sport; }
}