package com.team10.recipes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_type_id", nullable = false)
    private DishType dishType;

    @Column(nullable = false)
    private Short servings;

    @Column(name = "total_time_minutes", nullable = false)
    private Integer totalTimeMinutes;

    @Column(columnDefinition = "text")
    private String story;

    @Column(name = "is_removed", nullable = false)
    private boolean removed;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Recipe() {
    }

    public Long getId() { return id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public DishType getDishType() { return dishType; }
    public void setDishType(DishType dishType) { this.dishType = dishType; }

    public Short getServings() { return servings; }
    public void setServings(Short servings) { this.servings = servings; }

    public Integer getTotalTimeMinutes() { return totalTimeMinutes; }
    public void setTotalTimeMinutes(Integer totalTimeMinutes) { this.totalTimeMinutes = totalTimeMinutes; }

    public String getStory() { return story; }
    public void setStory(String story) { this.story = story; }

    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}