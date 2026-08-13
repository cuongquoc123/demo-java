package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.enrollment;
import com.example.demo.Entity.enrollment_id;

public interface enrollmentRepository extends JpaRepository<enrollment, enrollment_id> {

}
