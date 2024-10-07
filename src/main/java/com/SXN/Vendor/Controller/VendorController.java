package com.SXN.Vendor.Controller;

import com.SXN.Vendor.Entity.VendorIdDetails;
import com.SXN.Vendor.ResponseUtils.ApiResponse;
import com.SXN.Vendor.ResponseUtils.ResponseUtils;
import com.SXN.Vendor.Service.VendorService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;


@Slf4j
@RestController
@RequestMapping("/api/VendorList/")
public class VendorController {

    @Autowired
    private VendorService vendorService;
    // AWS - http://13.202.38.226:8089/
    // checked -------version , location , onboarding ?
    //http://localhost:8085/api/VendorList/registration?vendorId=vendor1&vendorName=ExampleVendor&gst_No=1234567890&address=ExampleAddress&phoneNumber=1234567890&regNo=ABC123&isActive=1&latitude=37.7749&longitude=-122.4194
    //dudes----
    //http://localhost:8085/api/VendorList/registration?vendorId=vendor1&vendorName=ExampleVendor&gst_No=1234567890&address=ExampleAddress&phoneNumber=123456789&regNo=ABC123&isActive=1&latitude=37.7749&longitude=-122.4194
    //https://vendor-wbgq.onrender.com/api/VendorList/registration?vendorId=vendor1&vendorName=ExampleVendor&gst_No=1234567890&address=ExampleAddress&phoneNumber=123456789&regNo=ABC123&isActive=1&latitude=37.7749&longitude=-122.4194
    @PostMapping("registration")
    public ResponseEntity<ApiResponse<VendorIdDetails>> registerVendor(
            @RequestParam(required = false) String vendorId,
            @RequestParam String vendorName,
            @RequestParam String gst_No,
            @RequestParam String address,
            @RequestParam String phno,
            @RequestParam String regNo,
            @RequestParam(required = false, defaultValue = "1") int isActive,
            @RequestParam(required = false) String latitude,
            @RequestParam(required = false) String longitude) {
        try {
            // Validate mandatory fields
            if (vendorName == null || gst_No == null || address == null || phno == null || regNo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(null, "Missing mandatory fields", "Missing mandatory fields", "error", 400));
            }

            // Check if vendor with the provided phone number already exists
            Map<String, Object> existingVendor = vendorService.checkRegistration(phno);
            if (existingVendor != null) {
                // Vendor with the same phone number already exists
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(null, "Error occurred: Vendor with the provided phone number is already registered.",
                                "Vendor with the provided phone number is already registered.", "Failed", 409));
            }

            // No existing vendor found, proceed with registration
            VendorIdDetails vendor = new VendorIdDetails();
            if (vendorId != null) {
                vendor.setVendorId(vendorId);
            } else {
                vendor.setVendorId(UUID.randomUUID().toString());
            }
            vendor.setVendorName(vendorName);
            vendor.setGstNo(gst_No);
            vendor.setAddress(address);
            vendor.setPhno(phno);
            vendor.setRegNo(regNo);
            vendor.setIsActive(isActive);

            // Handle location if provided
            Map<String, Double> location = new HashMap<>();
            if (latitude != null && longitude != null) {
                try {
                    location.put("latitude", Double.parseDouble(latitude));
                    location.put("longitude", Double.parseDouble(longitude));
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(null, "Invalid latitude or longitude format",e.getMessage(), "error", 400));
                }
            }
            vendor.setLocation(location);

            // Set onboarding to current timestamp
            vendor.setOnBoarding(String.valueOf(Instant.now().toEpochMilli()));

            String savedVendor = vendorService.saveVendor(vendor);

            log.info("Vendor registration successful for ID: {}", vendor.getVendorId());

            return ResponseEntity.ok(new ApiResponse<>(vendor, "Vendor registered successfully","null", "success", 200));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error registering vendor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error registering vendor: " + e.getMessage(), e.getMessage(), "error", 500));
        } catch (Exception e) {
            log.error("Unexpected error registering vendor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Unexpected error registering vendor: " + e.getMessage(), e.getMessage(), "error", 500));
        }
    }

    //http://localhost:8085/api/VendorList/LogIn?phoneNumber=01010100&type=IOS
    //http://localhost:8085/api/VendorList/LogIn?phoneNumber=01010100&type=Android
    //https://vendor-wbgq.onrender.com/api/VendorList/LogIn?phoneNumber=01010100&type=Android
    @GetMapping("LogIn")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestParam String phno,
                                                                  @RequestParam String type
                                                                  ) throws Exception {
        try {
            // Retrieve vendor details based on phoneNumber
            VendorIdDetails vendorDetails = vendorService.login(phno, type);

            if (vendorDetails == null) {
                log.info("Vendor with phoneNumber {} does not exist", phno);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(null,"Vendor with phoneNumber does not exist "," Phone nUmber doesn't exist","error",404));
            }

            // Check compatibility using a separate method (optional)
            boolean isStable = isStable(phno, type);

            // Create a Map to hold vendor details and isStable flag
            Map<String, Object> responseMap = new HashMap<>();

            // Option 1 (Using Lombok - if available):
            // Assuming @Data annotation is added to VendorIdDetails class
            // responseMap.putAll(vendorDetails);

            // Option 2 (Manual Property Copying - if not using Lombok):
            for (Field field : vendorDetails.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    responseMap.put(field.getName(), field.get(vendorDetails));
                } catch (IllegalAccessException e) {
                    log.error("Error copying property: {}", field.getName(), e);
                    // Handle access exception (optional)
                }
            }

            responseMap.put("isStable", isStable);

            // Create the response with the map
            ApiResponse<Map<String, Object>> response = ResponseUtils.createOkResponse(responseMap);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving vendor details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(null,"Error in retrieving vendor details",e.getMessage(),"error",401));
        }
    }


    private boolean isStable(String phno, String type) throws Exception {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        try {
            // Get the version document reference
            DocumentReference versionDocRef = dbFirestore.collection("Version").document(type + " Version");

            // Get the document (blocking operation)
            DocumentSnapshot document = versionDocRef.get().get();

            if (document.exists()) {
                // Get the latest version from the document
                String latestVersion = document.getString("version");
                log.info("Latest version for {} is {}", type, latestVersion);

                // Retrieve the vendor details based on vendorId
                VendorIdDetails vendorDetails = vendorService.login(phno, type);
                String existingVersion = null;
                if (type.equalsIgnoreCase("Android")) {
                    existingVersion = vendorDetails.getVendorAndroidVersion();
                    if (!latestVersion.equals(existingVersion)) {
                        log.info("Need to update the Android version ");
                        return false; // Vendor is not stable (version mismatch)
                    }
                } else if (type.equalsIgnoreCase("IOS")) {
                    existingVersion = vendorDetails.getVendorIOSVersion();
                    if (!latestVersion.equals(existingVersion)) {
                        log.info("Need to update the IOS version ");
                        return false; // Vendor is not stable (version mismatch)
                    }
                }

                // Check if the vendor version is null
                if (existingVersion == null) {
                    log.info("Vendor version not found for vendorId");
                    // Handle missing version appropriately (e.g., return false or specific error)
                    return false;
                }

                // If versions match, vendor is considered stable
                return true;
            } else {
                // If the document doesn't exist, consider returning a specific error
                log.info("Document does not exist for {} Version", type);
                return false; // Consider unstable due to missing version info
            }
        } catch (Exception e) {
            log.error("Error retrieving version from Firestore: {}", e.getMessage(), e);
            // Handle Firestore related errors (e.g., return false or specific error)
            return false;
        }
    }

    // updateProfile --------------------------
    //http://localhost:8085/api/VendorList/updateProfile?vendorId=vendor&phoneNumber=1233&vendorName=Tannu&latitude=11.11&longitude=11.11&address=abca
    //https://vendor-wbgq.onrender.com/api/VendorList/updateProfile?vendorId=vendor&phoneNumber=1233&vendorName=Tannu&latitude=11.11&longitude=11.11&address=abca
    @PostMapping("updateProfile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestParam String vendorId,
            @RequestParam(required = false) String vendorName,
            @RequestParam(required = false) String phno,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        try {
            // Check if any field is updated
            if (vendorName == null && phno == null && address == null && latitude == null && longitude == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "No fields to update", "No fields to update", "error", 400));
            }

            // Construct location map if latitude and longitude are provided
            Map<String, Double> location = null;
            if (latitude != null && longitude != null) {
                location = new HashMap<>();
                location.put("latitude", latitude);
                location.put("longitude", longitude);
            }

            // Update the profile and get the updated fields
            Map<String, Object> updatedFields = vendorService.updateProfile(vendorId, vendorName, phno, location, address);

            // Check if any fields are updated
            if (updatedFields.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Return only the updated fields in JSON format
            return ResponseEntity.ok(new ApiResponse<>(updatedFields, "Profile updated successfully", null, "success", 200));
        } catch (Exception e) {
            // Log the exception details
            log.error("Error occurred while updating profile: {}", e.getMessage(), e);
            // Return an appropriate error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error updating profile: " + e.getMessage(), e.getMessage(), "error", 500));
        }
    }

    //http://localhost:8085/api/VendorList/updateversion?vendorId=vendor1&type=IOS
    //https://vendor-wbgq.onrender.com/api/VendorList/updateversion?vendorId=vendor1&type=IOS
    @PostMapping("updateversion")
    public ResponseEntity<ApiResponse<Object>> updateVendorVersion(
            @RequestParam String vendorId,
            @RequestParam String type) {
        try {
            if (vendorId == null || vendorId.isEmpty()) {
                throw new IllegalArgumentException("vendorId cannot be null or empty");
            }
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("type cannot be null or empty");
            }

            vendorService.updateVendorVersion(vendorId, type);

            ApiResponse<Object> response = new ApiResponse<>();
            response.setStatus("No Content");
            response.setStatusCode(204);
            response.setMessage(type + " Version is already updated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating vendor version: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Error updating vendor version: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (Exception e) {
            log.error("Error updating vendor version for vendorId: {} and type: {}", vendorId, type, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Internal Server Error: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }


}