package de.hs_esslingen.besy.extern.bic.controllers;

import de.hs_esslingen.besy.extern.bic.BicCallbackService;
import de.hs_esslingen.besy.extern.bic.dtos.BicResDataDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/bic-callback")
public class BicResponsController {

    private final BicCallbackService bicCallbackService;

    @PostMapping("/{order-number}")
    public ResponseEntity<String> postOrderToInsy(
            @PathVariable("order-number") String orderNumber,
            @RequestBody BicResDataDTO bicResDataDTO,
            @AuthenticationPrincipal Jwt jwt) {
        //TODO: show for response from bicResDataDTO
        bicCallbackService.handleCallback(orderNumber, bicResDataDTO.getStatus(), jwt);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{order-number}/attachments", consumes = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<String> postAttachmentToInsy(
            @PathVariable("order-number") String orderNumber,
            @RequestParam("filename") String filename,
            @RequestBody byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            return ResponseEntity.badRequest().body("File content cannot be empty");
        }

        bicCallbackService.handleFileCallback(orderNumber, filename, fileContent);
        return ResponseEntity.ok().build();
    }

}