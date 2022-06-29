package io.github.severianofsp.quarkussocial.rest;

import io.github.severianofsp.quarkussocial.domain.model.Follower;
import io.github.severianofsp.quarkussocial.domain.model.User;
import io.github.severianofsp.quarkussocial.domain.repository.FollowerRepository;
import io.github.severianofsp.quarkussocial.domain.repository.UserRepository;
import io.github.severianofsp.quarkussocial.rest.dto.FollowerPerUserResponse;
import io.github.severianofsp.quarkussocial.rest.dto.FollowerRequest;
import io.github.severianofsp.quarkussocial.rest.dto.FollowerResponse;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("/users/{userId}/follower")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }

    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest followerRequest) {

        if (userId.equals(followerRequest.getFollowerId())) {
            return Response.status(CONFLICT).entity("You can't follow yourself").build();
        }

        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        User follower = userRepository.findById(followerRequest.getFollowerId());
        boolean follows = followerRepository.follows(follower, user);
        if (!follows) {
            Follower entity = new Follower().toBuilder()
                    .user(user)
                    .follower(follower)
                    .build();
            followerRepository.persist(entity);
        }

        return Response.status(NO_CONTENT).build();
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        List<Follower> followerList = followerRepository.findByUser(userId);
        FollowerPerUserResponse followerResponse = new FollowerPerUserResponse();
        followerResponse.setFollowersCount(followerList.size());

        List<FollowerResponse> followers = followerList
                .stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());
        followerResponse.setContent(followers);
        return Response.ok().entity(followerResponse).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId){
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }

        followerRepository.deleteByFollowerAndUser(followerId,userId);

        return Response.status(NO_CONTENT).build();

    }
}
