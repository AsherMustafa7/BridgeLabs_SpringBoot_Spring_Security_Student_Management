package com.asher.Student_management.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.asher.Student_management.Service.Student_Service;
import com.asher.Student_management.DTO.StudentRequestDTO;
import com.asher.Student_management.DTO.StudentResponseDTO;
import com.asher.Student_management.Entity.Student;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
// Import the HttpSession


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
@RestController 
@RequestMapping("/students")
public class Student_Controller
{
    private final Student_Service studentService;
    public Student_Controller(Student_Service studentService) {
        this.studentService = studentService;
    }
    @GetMapping("/security-info")
    public String getSecurityInfo(HttpServletRequest request) 
    {
        HttpSession session = request.getSession(false);
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return "Username: " + authentication.getName()
                + "\nAuthorities: " + authentication.getAuthorities()
                + "\nAuthenticated: " + authentication.isAuthenticated()+ "\nSession ID: " + (session != null ? session.getId() : "No session");
    }
    @GetMapping("/session-info")
    public String getSessionInfo(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return "No existing session";
        }

        return "Session ID: " + session.getId();
    }
    @GetMapping 
    public ResponseEntity<List<Student>> getAllStudents() 
    {
        
        System.out.println("Just Under the Get Controller Method");
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable int id) 
    {
       Optional<Student> studentOptional = studentService.getStudentById(id);
        if (studentOptional.isPresent()) {
            return ResponseEntity.ok(studentOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // @PostMapping
    // public ResponseEntity<Student> addStudent(@RequestBody Student student) {
    //     Student addedStudent = studentService.addStudent(student);
    //     return ResponseEntity.ok(addedStudent);
    // }
    // ---------------------------------------------------------------
    @PostMapping
    public ResponseEntity<StudentResponseDTO> addStudent(@Valid @RequestBody StudentRequestDTO request) 
    {

        StudentResponseDTO response =studentService.addStudent(request);

        return ResponseEntity.status(201).body(response);
    }
    // ---------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable int id, @RequestBody  Student updatedStudent) 
    {
        Optional<Student> updatedOptional = studentService.updateStudent(id, updatedStudent);
        if (updatedOptional.isPresent()) {
            return ResponseEntity.ok(updatedOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable int id) 
    {
        boolean deleted = studentService.deleteStudentById(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
