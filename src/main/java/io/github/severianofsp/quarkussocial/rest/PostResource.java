package io.github.severianofsp.quarkussocial.rest;

import io.github.severianofsp.quarkussocial.domain.model.Post;
import io.github.severianofsp.quarkussocial.domain.model.User;
import io.github.severianofsp.quarkussocial.domain.repository.FollowerRepository;
import io.github.severianofsp.quarkussocial.domain.repository.PostRepository;
import io.github.severianofsp.quarkussocial.domain.repository.UserRepository;
import io.github.severianofsp.quarkussocial.rest.dto.CreatePostRequest;
import io.github.severianofsp.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static io.quarkus.panache.common.Sort.Direction.Descending;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("/users/{userId}/posts")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository, FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response createPost(@PathParam("userId") Long userId, CreatePostRequest createPostRequest) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setUser(user);
        postRepository.persist(post);
        return Response.status(CREATED).build();
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId, @HeaderParam("followerId") Long followerId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(NOT_FOUND).build();
        }
        if (followerId == null) {
            return Response.status(BAD_REQUEST).entity("You forgot the header followerId").build();
        }
        User follower = userRepository.findById(followerId);

        if (follower == null) {
            return Response.status(NOT_FOUND).entity("Inexistent followerId").build();
        }

        boolean follows = followerRepository.follows(follower, user);

        if (!follows) {
            return Response.status(FORBIDDEN).entity("You can't see these posts").build();
        }

        PanacheQuery<Post> post = postRepository.find("user", Sort.by("dateTime", Descending), user);

        List<PostResponse> postResponseList = post.list()
                .stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok().entity(postResponseList).build();
    }
}
