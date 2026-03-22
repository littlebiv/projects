package com.whist.game.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileStorage implements DataStore {
    private static final String FILE_PATH = "leaderboard.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveVictoryPoints(Map<String, Integer> points) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), points);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save leaderboard", e);
        }
    }

    @Override
    public Map<String, Integer> getLeaderboard() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load leaderboard", e);
        }
    }
}
