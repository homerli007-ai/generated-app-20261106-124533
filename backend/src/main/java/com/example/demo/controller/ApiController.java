package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, List<String>> body) {
        List<String> paths = body.getOrDefault("paths", List.of());
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();
        List<String> deleted = new ArrayList<>();
        List<Map<String, String>> refused = new ArrayList<>();

        for (String raw : paths) {
            try {
                Path p = Paths.get(raw).toAbsolutePath().normalize();
                if (!p.startsWith(root)) {
                    refused.add(Map.of("path", raw, "reason", "outside allowed_root"));
                    continue;
                }
                if (Files.isSymbolicLink(p)) {
                    refused.add(Map.of("path", raw, "reason", "symlink refused"));
                    continue;
                }
                if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", raw, "reason", "does not exist"));
                    continue;
                }
                if (!Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", raw, "reason", "not a regular file"));
                    continue;
                }
                Files.delete(p);
                deleted.add(raw);
            } catch (IOException | InvalidPathException e) {
                refused.add(Map.of("path", raw, "reason", e.getClass().getSimpleName()));
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("allowedRoot", root.toString());
        resp.put("deleted", deleted);
        resp.put("refused", refused);
        return ResponseEntity.ok(resp);
    }
}
