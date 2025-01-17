package com.adminuniversity.adminuniversityrest.service;

import com.adminuniversity.adminuniversityrest.dto.entity.CourseDTO;
import com.adminuniversity.adminuniversityrest.dto.entity.GradeDTO;
import com.adminuniversity.adminuniversityrest.entity.CourseEntity;
import com.adminuniversity.adminuniversityrest.entity.GradeEntity;
import com.adminuniversity.adminuniversityrest.entity.user.StudentEntity;
import com.adminuniversity.adminuniversityrest.entity.user.TeacherEntity;
import com.adminuniversity.adminuniversityrest.repository.CourseRepository;
import com.adminuniversity.adminuniversityrest.repository.GradeEntityRepository;
import com.adminuniversity.adminuniversityrest.repository.StudentRepository;
import com.adminuniversity.adminuniversityrest.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final GradeService gradeService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GradeEntityRepository gradeEntityRepository;


    public CourseServiceImpl(CourseRepository courseRepository, GradeService gradeService, TeacherService teacherService, StudentService studentService, StudentRepository studentRepository, TeacherRepository teacherRepository, GradeEntityRepository gradeEntityRepository){
        this.courseRepository = courseRepository;
        this.gradeService = gradeService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.gradeEntityRepository = gradeEntityRepository;
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream().map(this::mapDTO).toList();
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        CourseEntity course = this.courseRepository.findById(id).orElseThrow();
        return mapDTO(course);
    }

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = mapEntity(courseDTO);
        return mapDTO(courseRepository.save(courseEntity));
    }

    @Override
    public CourseDTO updateCourse(CourseDTO courseDTO) {
        CourseEntity courseEntity = mapEntity(courseDTO);
        courseRepository.save(courseEntity);
        return mapDTO(courseEntity);
    }

    @Override
    public ResponseEntity<Object> deleteCourse(Long id) {
        Optional<CourseEntity> courseEntityOptional = this.courseRepository.findById(id);
        if (courseEntityOptional.isPresent()){
            CourseEntity courseEntity = courseEntityOptional.get();
            courseRepository.delete(courseEntity);
        } else {
            System.out.println("Course not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<CourseEntity> addStudent(Long studentId, Long courseId){
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(courseId);
        CourseEntity courseEntity = null;
        if(courseEntityOptional.isPresent()){
            Optional<StudentEntity> studentEntityOptional = studentRepository.findById(studentId);
            if(studentEntityOptional.isPresent()){
                courseEntity = courseEntityOptional.get();
                StudentEntity studentEntity = studentEntityOptional.get();
                courseEntity.getStudents().add(studentEntity);
                studentEntity.getCourses().add(courseEntity);
                courseRepository.save(courseEntity);
                studentRepository.save(studentEntity);
            } else{
                System.out.println("Student not found");
                return new ResponseEntity<CourseEntity>(HttpStatus.NOT_FOUND);
            }
        } else {
            System.out.println("Course not found");
            return new ResponseEntity<CourseEntity>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<CourseEntity>(courseEntity, HttpStatus.ACCEPTED);
    }

    @Override
    public ResponseEntity<CourseEntity> deleteStudent(Long studentId, Long courseId){
        Optional<CourseEntity> courseEntityOptional = courseRepository.findById(courseId);
        CourseEntity courseEntity = null;
        if(courseEntityOptional.isPresent()){
            Optional<StudentEntity> studentEntityOptional = studentRepository.findById(studentId);
            if(studentEntityOptional.isPresent()){
                courseEntity = courseEntityOptional.get();
                StudentEntity studentEntity = studentEntityOptional.get();
                courseEntity.getStudents().remove(studentEntity);
                studentEntity.getCourses().remove(courseEntity);
                courseRepository.save(courseEntity);
                studentRepository.save(studentEntity);
            } else{
                System.out.println("Student not found");
                return new ResponseEntity<CourseEntity>(HttpStatus.NOT_FOUND);
            }
        } else {
            System.out.println("Course not found");
            return new ResponseEntity<CourseEntity>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<CourseEntity>(courseEntity, HttpStatus.ACCEPTED);
    }

    @Override
    public Set<GradeDTO> getStudentGrades(Long courseId, Long studentId) {
        Set<GradeEntity> grades = gradeEntityRepository.findByCourse_IdAndStudent_Id(courseId, studentId);
        if (grades==null) throw new IllegalArgumentException();

        return grades.parallelStream().map(e -> new GradeDTO(e.getId(), e.getScore())).collect(Collectors.toSet());
    }

    @Override
    public Set<GradeDTO> addStudentGrade(Long courseId, Long studentId, float grade) {
        Set<GradeEntity> grades = gradeEntityRepository.findByCourse_IdAndStudent_Id(courseId, studentId);
        StudentEntity student = studentRepository.findById(studentId).orElseThrow();
        CourseEntity course = courseRepository.findById(courseId).orElseThrow();
        GradeEntity gradeToAdd = new GradeEntity(null, grade, student, course);
        gradeEntityRepository.save(gradeToAdd);
        grades.add(gradeToAdd);
        return grades.parallelStream().map(e -> new GradeDTO(e.getId(), e.getScore())).collect(Collectors.toSet());
    }

    @Override
    public double getStudentAverage(Long courseId, Long studentId) {
        Set<GradeEntity> grades = gradeEntityRepository.findByCourse_IdAndStudent_Id(courseId, studentId);
        return grades.parallelStream().mapToDouble(value -> value.getScore().doubleValue()).average().orElse(0d);
    }

    @Override
    public ResponseEntity<CourseEntity> setTeacher(Long courseId, Long teacherId) {
        Optional<CourseEntity> courseOptional = courseRepository.findById(courseId);
        CourseEntity courseEntity;
        if (courseOptional.isPresent()) {
            Optional<TeacherEntity> teacherOptional = teacherRepository.findById(teacherId);
            if (teacherOptional.isPresent()) {
                courseEntity = courseOptional.get();
                TeacherEntity teacherEntity = teacherOptional.get();
                courseEntity.setTeacher(teacherEntity);
                teacherEntity.getCourses().add(courseEntity);
                teacherRepository.save(teacherEntity);
                courseRepository.save(courseEntity);
            } else {
                System.out.println("Teacher not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            System.out.println("Course not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(courseEntity, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CourseEntity> removeTeacher(Long courseId, Long teacherId) {
        Optional<CourseEntity> courseOptional = courseRepository.findById(courseId);
        CourseEntity courseEntity = null;
        if (courseOptional.isPresent()) {
            Optional<TeacherEntity> teacherOptional = teacherRepository.findById(teacherId);
            if (teacherOptional.isPresent()) {
                courseEntity = courseOptional.get();
                TeacherEntity teacherEntity = teacherOptional.get();
                courseEntity.setTeacher(null);
                teacherEntity.getCourses().remove(courseEntity);
                teacherRepository.save(teacherEntity);
                courseRepository.save(courseEntity);
            } else {
                System.out.println("Teacher not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            System.out.println("Course not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<CourseEntity>(courseEntity, HttpStatus.OK);
    }
    public CourseEntity mapEntity(CourseDTO courseDTO){
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseDTO.getId());
        courseEntity.setName(courseDTO.getName());
        if (courseDTO.getTeacher()!=null)   courseEntity.setTeacher(teacherService.mapToEntity(courseDTO.getTeacher()));
        if (courseDTO.getStudents()!=null)  courseEntity.setStudents(courseDTO.getStudents().parallelStream().map(studentService::mapEntity).toList());
        if (courseDTO.getGrades()!=null)    courseEntity.setGrades(courseDTO.getGrades().entrySet().parallelStream().flatMap(
                e -> e.getValue().stream().map(gradeDTO -> gradeService.mapEntity(gradeDTO, e.getKey(), courseDTO.getId()))
        ).collect(Collectors.toSet()));

        return courseEntity;
    }

    public CourseDTO mapDTO(CourseEntity courseEntity){
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(courseEntity.getId());
        courseDTO.setName(courseEntity.getName());
        if (courseEntity.getTeacher()!=null)  courseDTO.setTeacher(teacherService.mapToDTO(courseEntity.getTeacher()));
        if (courseEntity.getStudents()!=null)
            courseDTO.setStudents(courseEntity.getStudents().stream().map(studentService::mapDTO).toList());

        try {
            if (courseEntity.getGrades()!=null)    courseDTO.setGrades(courseEntity.getGrades().stream().collect(Collectors.toMap(
                    o -> o.getStudent().getId(),
                    v -> gradeEntityRepository.findByCourse_IdAndStudent_Id(courseEntity.getId(), v.getStudent().getId()).stream().map(gradeService::mapDTO).collect(Collectors.toSet())
            )));
        } catch (IllegalStateException ignored) {}

        return courseDTO;
    }
}
