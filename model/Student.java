package model;

public class Student {

	private Integer studentId;
	private Integer userId;
	private String name;
	private String email;
	private Integer courseId;

	public Student(Integer studentId, Integer userId, String name, String email, Integer courseId) {
		this.studentId = studentId;
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.courseId = courseId;
	}

	public Integer getUserId() {
		return userId;
	}

	public Integer getStudentId() {
		return studentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getCourseId() {
		return courseId;
	}

	public void setCourseId(Integer courseId) {
		this.courseId = courseId;
	}

	@Override
	public String toString() {
		return studentId + "," + userId + "," + name + "," + email + "," + courseId;
	}
}
