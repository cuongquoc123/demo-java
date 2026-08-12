package com.example.demo.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class enrollment_id implements java.io.Serializable {

    @jakarta.persistence.Column(name = "student_id")
    private Long studentId;
    @jakarta.persistence.Column(name = "course_id")
    private Long courseId;
   
}
