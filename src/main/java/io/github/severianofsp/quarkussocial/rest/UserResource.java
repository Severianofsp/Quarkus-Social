package io.github.severianofsp.quarkussocial.rest;


import io.github.severianofsp.quarkussocial.domain.model.User;
import io.github.severianofsp.quarkussocial.domain.repository.UserRepository;
import io.github.severianofsp.quarkussocial.rest.dto.CreateUserRequest;
import io.github.severianofsp.quarkussocial.rest.dto.ResponseError;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Set;

import static io.github.severianofsp.quarkussocial.rest.dto.ResponseError.UNPROCESSABLE_ENTITY_STATUS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class UserResource {

    private UserRepository userRepository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository userRepository, Validator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest createUserRequest) {
        Set<ConstraintViolation<CreateUserRequest>> validate = validator.validate(createUserRequest);

        if (!validate.isEmpty()) {

            return ResponseError
                    .createFromValidation(validate)
                    .withStatusCode(UNPROCESSABLE_ENTITY_STATUS);
        }

        User user = new User();
        user.setName(createUserRequest.getName());
        user.setAge(createUserRequest.getAge());

        userRepository.persist(user);

        return Response.status(CREATED).entity(user).build();
    }

    @GET
    public Response listAllUser() {

        PanacheQuery<User> queryList = userRepository.findAll();
        return Response.ok(queryList.list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User user = userRepository.findById(id);

        if (user != null) {
            userRepository.delete(user);
            return Response.noContent().build();
        }

        return Response.status(NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, CreateUserRequest dataUser) {
        User user = userRepository.findById(id);

        if (user != null) {
            user.setName(dataUser.getName());
            user.setAge(dataUser.getAge());
            return Response.noContent().build();
        }

        return Response.status(NOT_FOUND).build();
    }
}
