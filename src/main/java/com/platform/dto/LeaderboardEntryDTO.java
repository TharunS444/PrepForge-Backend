package com.platform.dto;

public class LeaderboardEntryDTO {
    private Long id;
    private String name;
    private Integer score;
    private Integer streak;
    private long totalSolved;

    public LeaderboardEntryDTO(Long id, String name, Integer score, Integer streak, long totalSolved) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.streak = streak;
        this.totalSolved = totalSolved;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Integer getScore() { return score; }
    public Integer getStreak() { return streak; }
    public long getTotalSolved() { return totalSolved; }
}
