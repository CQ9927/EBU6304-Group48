package com.ebu6304.group48.repository;

import com.ebu6304.group48.config.AppPaths;
import com.ebu6304.group48.model.Invitation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JSON persistence for {@code invitations.json}.
 * Thread-safe with {@code FILE_LOCK}. Deduplicates on (jobId + taUserId).
 */
public class InvitationRepository {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Invitation>>() {}.getType();
    private static final Object FILE_LOCK = new Object();
    private static final Set<String> VALID_STATUSES = Set.of("PENDING", "ACCEPTED", "DECLINED", "EXPIRED");

    private final Path file;

    public InvitationRepository(ServletContext context) {
        String dataDir = AppPaths.resolveDataDirectory(context);
        this.file = Path.of(dataDir, "invitations.json");
    }

    public List<Invitation> findAll() {
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Invitation> list = readAllInternal();
                list.sort(Comparator.comparing(Invitation::getCreatedAt,
                        Comparator.nullsLast(String::compareTo)).reversed());
                return list;
            } catch (IOException | RuntimeException e) {
                return new ArrayList<>();
            }
        }
    }

    public List<Invitation> findByTaUserId(String taUserId) {
        return findAll().stream()
                .filter(i -> taUserId.equals(i.getTaUserId()))
                .collect(Collectors.toList());
    }

    public List<Invitation> findByJobId(String jobId) {
        return findAll().stream()
                .filter(i -> jobId.equals(i.getJobId()))
                .collect(Collectors.toList());
    }

    public List<Invitation> findByMoUserId(String moUserId) {
        return findAll().stream()
                .filter(i -> moUserId.equals(i.getMoUserId()))
                .collect(Collectors.toList());
    }

    public Invitation findById(String invitationId) {
        if (invitationId == null || invitationId.isBlank()) return null;
        return findAll().stream()
                .filter(i -> invitationId.equals(i.getInvitationId()))
                .findFirst().orElse(null);
    }

    /**
     * Returns true if a PENDING invitation already exists for this job+TA pair.
     */
    public boolean existsPending(String jobId, String taUserId) {
        return findAll().stream()
                .anyMatch(i -> jobId.equals(i.getJobId())
                        && taUserId.equals(i.getTaUserId())
                        && "PENDING".equalsIgnoreCase(
                                i.getStatus() != null ? i.getStatus().trim() : ""));
    }

    public boolean save(Invitation inv) {
        if (inv == null || inv.getJobId() == null || inv.getTaUserId() == null) {
            return false;
        }
        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Invitation> list = readAllInternal();

                // Dedup: if same (jobId + taUserId) PENDING exists, skip
                for (Invitation existing : list) {
                    if (inv.getJobId().equals(existing.getJobId())
                            && inv.getTaUserId().equals(existing.getTaUserId())
                            && "PENDING".equalsIgnoreCase(
                                    existing.getStatus() != null ? existing.getStatus().trim() : "")) {
                        return false;
                    }
                }

                normalizeBeforeSave(inv);
                list.add(inv);
                Files.writeString(file, GSON.toJson(list), StandardCharsets.UTF_8);
                return true;
            } catch (IOException | RuntimeException e) {
                return false;
            }
        }
    }

    public boolean updateStatus(String invitationId, String newStatus) {
        if (invitationId == null || invitationId.isBlank()
                || newStatus == null || newStatus.isBlank()) {
            return false;
        }
        String normalized = newStatus.trim().toUpperCase();
        if (!VALID_STATUSES.contains(normalized)) return false;

        synchronized (FILE_LOCK) {
            try {
                ensureStorage();
                List<Invitation> list = readAllInternal();
                for (int i = 0; i < list.size(); i++) {
                    if (invitationId.equals(list.get(i).getInvitationId())) {
                        list.get(i).setStatus(normalized);
                        list.get(i).setUpdatedAt(Instant.now().toString());
                        Files.writeString(file, GSON.toJson(list), StandardCharsets.UTF_8);
                        return true;
                    }
                }
                return false;
            } catch (IOException | RuntimeException e) {
                return false;
            }
        }
    }

    /**
     * Count pending invitations for a TA.
     */
    public int countPendingByTaUserId(String taUserId) {
        return (int) findByTaUserId(taUserId).stream()
                .filter(i -> "PENDING".equalsIgnoreCase(
                        i.getStatus() != null ? i.getStatus().trim() : ""))
                .count();
    }

    private void normalizeBeforeSave(Invitation inv) {
        String now = Instant.now().toString();
        if (inv.getInvitationId() == null || inv.getInvitationId().isBlank()) {
            inv.setInvitationId("I-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 8).toUpperCase());
        }
        if (inv.getStatus() == null || inv.getStatus().isBlank()) {
            inv.setStatus("PENDING");
        } else {
            inv.setStatus(inv.getStatus().trim().toUpperCase());
        }
        if (inv.getCreatedAt() == null || inv.getCreatedAt().isBlank()) {
            inv.setCreatedAt(now);
        }
        inv.setUpdatedAt(now);
    }

    private void ensureStorage() throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.writeString(file, "[]", StandardCharsets.UTF_8);
        }
    }

    private List<Invitation> readAllInternal() throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        List<Invitation> list = GSON.fromJson(json, LIST_TYPE);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }
}
