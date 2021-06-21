package com.mingu.restfulwebapp.user;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class UserController {

    private UserDaoService service;

    public UserController(UserDaoService service) {
        this.service = service;
    }

    @GetMapping("/users")
    public List<User> retrieveAllUsers() {
        return service.findAll();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<EntityModel<User>> retrieveUser(@PathVariable int id) {
        User user = service.findOne(id);
        // 찾는 user가 없을 경우, null이 아닌 예외를 던져서 200 응답이 나오지 않게 한다.
        if (user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }
        /*
        HATEOAS
         */
        EntityModel entityModel = EntityModel.of(user);
        // 개별 사용자 조회에서 할 수 있는 추가 작업으로 '전체 사용자 조회' uri를 'all-users'라는 이름으로 알려준다
        WebMvcLinkBuilder linkTo = linkTo(methodOn(this.getClass()).retrieveAllUsers());
        // Link 객체를 만든 후, entityModel에 추가하여 리턴한다.
        entityModel.add(linkTo.withRel("all-users"));

        return ResponseEntity.ok(entityModel);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User savedUser = service.save(user);
        // 응답 헤더에 생성된 유저의 id를 path에 넣어서 location 이라는 데이터로 전달한다.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();
        // 유저가 정상 생성된 경우, 기본 성공 코드인 200이 아닌 create(201)로 엄밀하게 응답 코드를 지정한다.
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable int id) {
        User user = service.deleteById(id);
        if (user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }
    }

    @PutMapping("/users")
    public User modifyUser(@RequestBody User user) {
        User modifiedUser = service.modifyById(user);
        if (modifiedUser == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", user.getId()));
        }
        return modifiedUser;
    }
}