package com.rem.springboot.service;

import static java.util.stream.Collectors.toList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.rem.springboot.dto.PostDto;
import com.rem.springboot.entity.Category;
import com.rem.springboot.entity.Image;
import com.rem.springboot.entity.Post;
import com.rem.springboot.entity.User;
import com.rem.springboot.exception.CategoryNotFoundException;
import com.rem.springboot.exception.PostNotFoundException;
import com.rem.springboot.exception.UserNotFoundException;
import com.rem.springboot.payload.request.PostCreateRequest;
import com.rem.springboot.payload.response.PostCreateResponse;
import com.rem.springboot.repository.CategoryRepository;
import com.rem.springboot.repository.PostRepository;
import com.rem.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostServiceImpl {
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final FileService fileService;

  @Transactional
  public PostCreateResponse create(PostCreateRequest request) {
    User user = userRepository.findById(request.getUserId()).orElseThrow(UserNotFoundException::new);
    Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(CategoryNotFoundException::new);
    List<Image> images = request.getImages().stream().map(i -> new Image(i.getOriginalFilename())).collect(toList());
    Post post = postRepository.save(new Post(request.getTitle(), request.getContent(), user, category, images));

    uploadImages(post.getImages(), request.getImages());
    return new PostCreateResponse(post.getId());
  }

  public PostDto read(Long id) {
    return PostDto.toDto(postRepository.findById(id).orElseThrow(PostNotFoundException::new));
  }

  private void uploadImages(List<Image> images, List<MultipartFile> fileImages) {
    IntStream.range(0, images.size()).forEach(i -> fileService.upload(fileImages.get(i), images.get(i).getUniqueName()));
  }
}
