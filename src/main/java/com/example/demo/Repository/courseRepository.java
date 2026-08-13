package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.course;

public interface courseRepository extends JpaRepository<course, Long> {

}
