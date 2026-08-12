package com.example.demo.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Student;
import com.example.demo.Service.StudentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/demos")
public class DemoControllers {

    private final StudentService studentService;

    public DemoControllers(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("students")
    public Student postMethodName(@RequestBody Student entity) {
        return studentService.SaveStudent(entity);
    }
    
    @GetMapping("students")
    public java.util.List<Student> getMethodName() {
        return studentService.getAllStudents();
    }
    
    @GetMapping("/test-error")
    public String Error() {
        throw new com.example.demo.exception.ResourceNotFoundException("Resource not found");
    }
    
}   
