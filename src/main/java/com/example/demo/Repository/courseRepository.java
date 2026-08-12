package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.course;

@Repository
public interface courseRepository extends JpaRepository<course, Long> {

}
