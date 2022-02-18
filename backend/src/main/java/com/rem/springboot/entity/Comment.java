package com.rem.springboot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Comment extends EntityDate {
  public Comment(String content, User user, Post post, Comment parent) {
    this.content = content;
    this.user = user;
    this.post = post;
    this.parent = parent;
    deleted = false;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @Lob
  private String content;

  @Column(nullable = false)
  private boolean deleted;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Comment parent;

  @OneToMany(mappedBy = "parent")
  private List<Comment> children = new ArrayList<>();

  public void delete() {
    deleted = true;
  }

  public Optional<Comment> findDeletableComment() {
    return hasChildren() ? Optional.empty() : Optional.of(findDeletableCommentByParent());
  }

  private boolean hasChildren() {
    return getChildren().size() != 0;
  }

  private Comment findDeletableCommentByParent() {
    return isDeletableParent() ? getParent().findDeletableCommentByParent() : this;
  }

  private boolean isDeletableParent() {
    return getParent() != null && getParent().isDeleted() && getParent().getChildren().size() == 1;
  }
}
