package com.sian.noteshare.controller;

import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUploadRequest;
import com.sian.noteshare.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/upload")
    public ResponseEntity<NoteResponse> uploadNote(
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file
    ) {
        NoteUploadRequest request = new NoteUploadRequest();
        request.setTitle(title);
        request.setSubject(subject);
        request.setDescription(description);
        request.setFile(file);

        NoteResponse response = noteService.uploadNote(request);
        return ResponseEntity.ok(response);
    }

}
