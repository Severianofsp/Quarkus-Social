package io.github.severianofsp.quarkussocial.rest.dto;

import lombok.Data;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ResponseError {

    public static final int UNPROCESSABLE_ENTITY_STATUS = 422;

    private String message;
    private Collection<FieldError> errors;


    public ResponseError(String message, Collection<FieldError> errors) {
        this.message = message;
        this.errors = errors;
    }

    public static <T> ResponseError createFromValidation(Set<ConstraintViolation<T>> violations) {
        List<FieldError> errorList = violations
                .stream()
                .map(v -> new FieldError(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());

        return new ResponseError("Validation Error", errorList);
    }

    public Response withStatusCode(int statusCode) {
        return Response.status(statusCode).entity(this).build();
    }
}
