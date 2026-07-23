package dealerShipOrder.presentation.controllers.testDriveControllers;

import dealerShipOrder.application.services.testDriveService.TestDriveService;
import dealerShipOrder.presentation.dtos.request.testDriveRequestPresentationDto.TestDriveFilterPresentationRequest;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDriveListPresentationResponse;
import dealerShipOrder.presentation.dtos.response.testDriveResponsePresentationDto.TestDrivePresentationResponse;
import dealerShipOrder.presentation.mappers.TestDrivePresentationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
@Tag(name = "Test Drives (Public)", description = "Public test drive endpoints")
public class TestDrivePublicController {

    private final TestDriveService testDriveService;
    private final TestDrivePresentationMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Get test drive by ID")
    public ResponseEntity<TestDrivePresentationResponse> getTestDrive(@PathVariable String id) {
        var response = testDriveService.getTestDriveById(id);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }

    @GetMapping
    @Operation(summary = "Get all test drives with filters")
    public ResponseEntity<TestDriveListPresentationResponse> getAllTestDrives(@Valid TestDriveFilterPresentationRequest request) {
        var appFilter = mapper.toApplication(request);
        var response = testDriveService.getTestDrivesWithFilters(appFilter);
        return ResponseEntity.ok(mapper.toPresentation(response));
    }
}