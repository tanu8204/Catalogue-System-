package com.SXN.Vendor.Controller;

import com.SXN.Vendor.ResponseUtils.ApiResponse;
import com.SXN.Vendor.ResponseUtils.ResponseUtils;
import com.SXN.Vendor.Service.CategoryService;
import com.google.cloud.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/VendorList/")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //checked - additems
   //http://localhost:8085/api/VendorList/addMenswear?outOfStock=false&name=T-shirt&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=12&vendorId=vendor&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&subcategory=Kurtas&Category=Menswear
   //dudes
    //http://localhost:8085/api/VendorList/addMenswear?outOfStock=false&name=T-shirt&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=12&vendorId=vendor1&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&subcategory=Shirts&Category=Menswear
    //https://vendor-wbgq.onrender.com/api/VendorList/addMenswear?outOfStock=false&name=T-shirt&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=MS123&vendorId=vendor1&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&subcategory=Shirts&Category=Menswear

    @PostMapping("addMenswear")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveMensDetails(
            @RequestParam String vendorId,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String itemId,
            @RequestParam List<String> pictures,
            @RequestParam int price,
            @RequestParam boolean outOfStock,
            @RequestParam(required = false) Integer S,
            @RequestParam(required = false) Integer M,
            @RequestParam(required = false) Integer L,
            @RequestParam(required = false) Integer XL,
            @RequestParam(required = false) Integer XXL,
            @RequestParam int lockinPeriod) {
        try {
            // Validate mandatory fields
            if (vendorId == null || vendorId.isEmpty() || category == null || category.isEmpty() ||
                    subcategory == null || subcategory.isEmpty() || name == null || name.isEmpty() ||
                    description == null || description.isEmpty() || itemId == null || itemId.isEmpty() ||
                    pictures == null || pictures.isEmpty() || price <= 0 || lockinPeriod <= 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameters","Missing or invalid request parameters", "error", 400));
            }

            // Validate size parameters
            Map<String, Integer> size = new HashMap<>();
            if (S != null && S >= 0) {
                size.put("S", S);
            }
            if (M != null && M >= 0) {
                size.put("M", M);
            }
            if (L != null && L >= 0) {
                size.put("L", L);
            }
            if (XL != null && XL >= 0) {
                size.put("XL", XL);
            }
            if (XXL != null && XXL >= 0) {
                size.put("XXL", XXL);
            }

            // Check if at least one size is provided
            if (size.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "At least one size must be provided","At least one size must be provided", "error", 400));
            }

            // Save category details
            Map<String, Object> savedCategory = categoryService.saveCategoryDetails(vendorId, category, subcategory, name, description, itemId, pictures, price, size, outOfStock, lockinPeriod);
            return ResponseEntity.ok(ResponseUtils.createOkResponse(savedCategory));
        } catch (IllegalArgumentException e) {
            // Log the validation error
            log.error("Validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Validation error: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (CategoryNotFoundException e) {
            // Log the category not found error
            log.error("Category not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Category not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (OutOfStockException e) {
            // Log
            log.error("Item is out of stock: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Item is out of stock: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (Exception e) {
            // Log any other unexpected error
            log.error("Failed to add category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to add category: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }
    public class OutOfStockException extends RuntimeException {
        public OutOfStockException(String message) {
            super(message);
        }
    }

    public class CategoryNotFoundException extends RuntimeException {
        public CategoryNotFoundException(String message) {
            super(message);
        }
    }


    //http://localhost:8085/api/VendorList/addWomenswear?outOfStock=false&name=SilkSaree&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=w12&vendorId=vendor&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&subcategory=Sarees&Category=Womenswear
    //https://vendor-wbgq.onrender.com/api/VendorList/addMenswear?outOfStock=false&name=Cotton Saree&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=W123&vendorId=vendor1&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&subcategory=Sarees&Category=Womenswear
    @PostMapping("addWomenswear")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveWomensDetails(
            @RequestParam String vendorId,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) String itemId,
            @RequestParam List<String> pictures,
            @RequestParam int price,
            @RequestParam boolean outOfStock,
            @RequestParam(required = false) int S,
            @RequestParam(required = false) int M,
            @RequestParam(required = false) int L,
            @RequestParam(required = false) int XL,
            @RequestParam(required = false) int XXL,
            @RequestParam int lockinPeriod) {
        try {

            // Convert Date to com.google.cloud.Timestamp

            Map<String, Integer> size = new HashMap<>();
            size.put("S", S);
            size.put("M", M);
            size.put("L", L);
            size.put("XL", XL);
            size.put("XXL", XXL);

            Map<String, Object> savedCategory = categoryService.saveCategoryDetails(vendorId,category,subcategory,name,description, itemId, pictures, price, size, outOfStock, lockinPeriod);
            return ResponseEntity.ok(ResponseUtils.createOkResponse(savedCategory));
        } catch (IllegalArgumentException e) {
            // Log the validation error
            log.error("Validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Validation error: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (CategoryNotFoundException e) {
            // Log the category not found error
            log.error("Category not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Category not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (OutOfStockException e) {
            // Log
            log.error("Item is out of stock: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Item is out of stock: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (Exception e) {
            // Log any other unexpected error
            log.error("Failed to add category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to add category: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }

    //http://localhost:8085/api/VendorList/addKidswear?outOfStock=false&name=kido&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&vendorId=vendor&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&Category=Kidswear&itemId=k01&subcategory=Boy
    //https://vendor-wbgq.onrender.com/api/VendorList/addKidswear?outOfStock=false&name=kido&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&vendorId=vendor1&lockinPeriod=15&S=10&L=23&XL=3&XXL=0&M=1&Category=Kidswear&itemId=k01&subcategory=Boy
    @PostMapping("addKidswear")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveKidsDetails(
            @RequestParam String vendorId,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) String itemId,
            @RequestParam List<String> pictures,
            @RequestParam int price,
            @RequestParam boolean outOfStock,
            @RequestParam(required = false) int S,
            @RequestParam(required = false) int M,
            @RequestParam(required = false) int L,
            @RequestParam(required = false) int XL,
            @RequestParam(required = false) int XXL,
            @RequestParam int lockinPeriod) {
        try {
            // Convert Date to com.google.cloud.Timestamp

            Map<String, Integer> size = new HashMap<>();
            size.put("S", S);
            size.put("M", M);
            size.put("L", L);
            size.put("XL", XL);
            size.put("XXL", XXL);

            Map<String, Object> savedCategory = categoryService.saveCategoryDetails(vendorId,category,subcategory,description,name, itemId, pictures, price, size, outOfStock, lockinPeriod);
            return ResponseEntity.ok(ResponseUtils.createOkResponse(savedCategory));
        } catch (IllegalArgumentException e) {
            // Log the validation error
            log.error("Validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Validation error: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (CategoryNotFoundException e) {
            // Log the category not found error
            log.error("Category not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Category not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (OutOfStockException e) {
            // Log
            log.error("Item is out of stock: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Item is out of stock: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (Exception e) {
            // Log any other unexpected error
            log.error("Failed to add category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to add category: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }
    //checked----
    //https://vendor-wbgq.onrender.com/api/VendorList/getcatalogue?vendorId=vendor1
    //http://localhost:8085/api/VendorList/getcatalogue?vendorId=vendor
    //dudes
    //http://localhost:8085/api/VendorList/getcatalogue?vendorId=vendor1
    @GetMapping("getcatalogue")
    public ResponseEntity<ApiResponse<List<Map<String,Object>>>> getCatalogue(@RequestParam String vendorId) {
        try {
            // Validate vendorId
            if (vendorId == null || vendorId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Vendor ID is required","Vendor Id is required", "error", 400));
            }

            List<Map<String, Object>> catalogue = categoryService.getCatalogue(vendorId);

            // Check if catalogue is empty
            if (catalogue.isEmpty()) {
                return ResponseEntity.notFound().build();
                        //.body(new ApiResponse<>(null, "Catalogue is empty", "Catalogue is empty", "error", 404));
            }

            return ResponseEntity.ok(ResponseUtils.createOkResponse(catalogue));
        } catch (IllegalArgumentException e) {
            // Log the validation error
            log.error("Validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(null, "Validation error: " + e.getMessage(),e.getMessage(), "error", 400));
        } catch (VendorNotFoundException e) {
            // Log the vendor not found error
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
                    //.body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            // Log the execution or interruption error
            log.error("Failed to fetch catalogue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to fetch catalogue: " + e.getMessage(),e.getMessage(), "error", 500));
        } catch (Exception e) {
            // Log any other unexpected error
            log.error("Failed to fetch catalogue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to fetch catalogue: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }

    public class VendorNotFoundException extends RuntimeException {
        public VendorNotFoundException(String message) {
            super(message);
        }
    }


    //delete product when stock == true ----------------------------checked
    //http://localhost:8085/api/VendorList/deleteOutOfStockItems?vendorId=vendor&Category=Womenswear&subcategory=Sarees&itemId=22
    //https://vendor-wbgq.onrender.com/api/VendorList/deleteOutOfStockItems?vendorId=vendor1&Category=Kidswear&subcategory=Boy&itemId=tk11

    public class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String message) {
            super(message);
        }
    }


    // Controller Method
    @PostMapping("deleteOutOfStockItems")
    public ResponseEntity<ApiResponse<String>> deleteOutOfStockItem(@RequestParam String vendorId,
                                                                    @RequestParam String Category,
                                                                    @RequestParam String subcategory,
                                                                    @RequestParam String itemId) {
        try {
            // Validate input parameters
            if (vendorId == null || vendorId.isEmpty() ||
                    Category == null || Category.isEmpty() ||
                    subcategory == null || subcategory.isEmpty() ||
                    itemId == null || itemId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameters","Missing or invalid request parameters", "error", 400));
            }

            if (!categoryService.vendorExists(vendorId)) {
                throw new VendorNotFoundException("Vendor not found");
            }

            if (!categoryService.categoryExists(vendorId, Category, subcategory)) {
                throw new CategoryNotFoundException("Category or Subcategory not found");
            }

            if (!categoryService.itemExists(vendorId, Category, subcategory, itemId)) {
                throw new ItemNotFoundException("Item not found with itemId: " + itemId);
            }
            // Check if item is out of stock before deletion
            if (!categoryService.isOutOfStock(vendorId, Category, subcategory, itemId)) {
                return ResponseEntity.ok(ResponseUtils.createOkResponse("Item with itemId: " + itemId + " is not out of stock, deletion skipped."));
            }

            // Proceed with deletion if the item is out of stock
            categoryService.deleteOutOfStockItems(vendorId, Category, subcategory, itemId);
            return ResponseEntity.ok(ResponseUtils.createOkResponse("ItemId " + itemId + " is deleted successfully from " + Category));

        } catch (VendorNotFoundException e) {
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (CategoryNotFoundException e) {
            log.error("Category not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Category not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (ItemNotFoundException e) {
            log.error("Item not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Item not found: " + e.getMessage(),e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error during execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error occurred while deleting item with itemId: " + itemId,"Error occurred while deleting item", "error", 500));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Unexpected error occurred: " + e.getMessage(),e.getMessage(), "error", 500));
        }
    }



    //update Products -----------------------------------
    //http://localhost:8085/api/VendorList/updateItem?outOfStock=false&name=SilkSaree&description=demo&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=w12&vendorId=vendor&lockinPeriod=15&S=11&L=23&XL=3&XXL=0&M=1&subcategory=Sarees&Category=Womenswear
    //https://vendor-wbgq.onrender.com/api/VendorList/updateItem?outOfStock=false&name=SilkSaree&description=demo1&price=1234&pictures=link1,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f,https://firebasestorage.googleapis.com/v0/b/duds-68a6d.appspot.com/o/pic1.jpg?alt=media%26token=452ba8c3-928b-490e-87e6-d567419bbf5f&itemId=w12&vendorId=vendor1&lockinPeriod=15&S=11&L=23&XL=3&XXL=0&M=1&subcategory=Sarees&Category=Womenswear
    @PostMapping("updateItem")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateItem(
            @RequestParam String vendorId,
            @RequestParam String category,
            @RequestParam String subcategory,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String itemId,
            @RequestParam List<String> pictures,
            @RequestParam int price,
            @RequestParam boolean outOfStock,
            @RequestParam(required = false) Integer S,
            @RequestParam(required = false) Integer M,
            @RequestParam(required = false) Integer L,
            @RequestParam(required = false) Integer XL,
            @RequestParam(required = false) Integer XXL,
            @RequestParam int lockinPeriod) {
        try {
            // Validate input parameters
            if (vendorId == null || vendorId.isEmpty() ||
                    category == null || category.isEmpty() ||
                    subcategory == null || subcategory.isEmpty() ||
                    name == null || name.isEmpty() ||
                    description == null || description.isEmpty() ||
                    itemId == null || itemId.isEmpty() ||
                    pictures == null || pictures.isEmpty() ||
                    price <= 0 || lockinPeriod < 0) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameters", "Missing or invalid request parameters", "error", 400));
            }

            if (!categoryService.vendorExists(vendorId)) {
                throw new VendorNotFoundException("Vendor not found");
            }

            if (!categoryService.categoryExists(vendorId, category, subcategory)) {
                throw new CategoryNotFoundException("Category or Subcategory not found");
            }

            if (!categoryService.itemExists(vendorId, category, subcategory, itemId)) {
                throw new ItemNotFoundException("Item not found with itemId: " + itemId);
            }

            Map<String, Integer> size = new HashMap<>();
            size.put("S", S != null ? S : 0);
            size.put("M", M != null ? M : 0);
            size.put("L", L != null ? L : 0);
            size.put("XL", XL != null ? XL : 0);
            size.put("XXL", XXL != null ? XXL : 0);

            Map<String, Object> updatedItem = categoryService.saveCategoryDetails(
                    vendorId, category, subcategory, name, description, itemId, pictures, price, size, outOfStock, lockinPeriod);

            return ResponseEntity.ok(ResponseUtils.createOkResponse(updatedItem));

        } catch (VendorNotFoundException e) {
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (CategoryNotFoundException e) {
            log.error("Category not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Category not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ItemNotFoundException e) {
            log.error("Item not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Item not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error during execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Error occurred while updating item with itemId: " + itemId, "Error occurred while updating item", "error", 500));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Unexpected error occurred: " + e.getMessage(), e.getMessage(), "error", 500));
        }
    }

    //pending orders -------------------------------------

    //servicekey - duds ----checked
    //https://vendor-wbgq.onrender.com/api/VendorList/pendingOrders?vendorId=QY7AhEbn2kZ9IWVPo7Il2wAsHZj1
    //http://localhost:8085/api/VendorList/pendingOrders?vendorId=QY7AhEbn2kZ9IWVPo7Il2wAsHZj1
    //http://localhost:8085/api/VendorList/pendingOrders?vendorId=vendor
    @GetMapping("pendingOrders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPendingOrders(@RequestParam String vendorId) {
        try {
            // Validate the vendorId parameter
            if (vendorId == null || vendorId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameter: vendorId", "Missing or invalid request parameter: vendorId", "error", 400));
            }

            // Check if vendor exists
            if (!categoryService.vendorExists(vendorId)) {
                throw new VendorNotFoundException("Vendor not found");
            }

            // Fetch pending orders
            List<Map<String, Object>> pendingOrders = categoryService.getPending(vendorId);

            return ResponseEntity.ok(ResponseUtils.createOkResponse(pendingOrders));
        } catch (VendorNotFoundException e) {
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error during execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to fetch Pending Orders: " + e.getMessage(), "Failed to fetch Pending Orders", "error", 500));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Unexpected error occurred: " + e.getMessage(), e.getMessage(), "error", 500));
        }
    }


    //service key - duds ---- checked
    //https://vendor-wbgq.onrender.com/api/VendorList/completedOrders?vendorId=QY7AhEbn2kZ9IWVPo7Il2wAsHZj1
    //http://localhost:8085/api/VendorList/completedOrders?vendorId=QY7AhEbn2kZ9IWVPo7Il2wAsHZj1
    @GetMapping("completedOrders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompletedOrders(@RequestParam String vendorId) {
        try {
            // Validate the vendorId parameter
            if (vendorId == null || vendorId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameter: vendorId", "Missing or invalid request parameter: vendorId", "error", 400));
            }

            // Check if vendor exists
            if (!categoryService.vendorExists(vendorId)) {
                throw new VendorNotFoundException("Vendor not found");
            }

            // Fetch completed orders
            List<Map<String, Object>> completedOrders = categoryService.getCompletedOrders(vendorId);

            return ResponseEntity.ok(ResponseUtils.createOkResponse(completedOrders));
        } catch (VendorNotFoundException e) {
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Error during execution: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Failed to fetch completed orders: " + e.getMessage(), "Failed to fetch completed orders", "error", 500));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(null, "Unexpected error occurred: " + e.getMessage(), e.getMessage(), "error", 500));
        }
    }


    //checked
    //http://localhost:8085/api/VendorList/getitem?vendorId=vendor&itemId=w11
    //duds
    //http://localhost:8085/api/VendorList/getitem?vendorId=vendor1&itemId=tm22
    //https://vendor-wbgq.onrender.com/api/VendorList/getitem?vendorId=vendor1&itemId=tm22
    @GetMapping("getitem")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getItemDetailsByVendorId(
            @RequestParam String vendorId,
            @RequestParam String itemId) {
        try {
            // Validate input parameters
            if (vendorId == null || vendorId.isEmpty() || itemId == null || itemId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(null, "Missing or invalid request parameters", "Missing or invalid request parameters", "error", 400));
            }

            log.info("Fetching item details for vendorId: {} and itemId: {}", vendorId, itemId);

            // Check if vendor exists
            if (!categoryService.vendorExists(vendorId)) {
                throw new VendorNotFoundException("Vendor not found");
            }

            // Check if item exists in the catalogue
            if (!categoryService.itemExistsFromCata(vendorId, itemId)) {
                throw new ItemNotFoundException("Item not found with itemId: " + itemId);
            }

            // Fetch item details
            Map<String, Object> itemDetails = categoryService.getItemDetailsByVendorId(vendorId, itemId);

            if (itemDetails != null) {
                // Return the item details if found
                return ResponseEntity.ok(ResponseUtils.createOkResponse(itemDetails));
            } else {
                // Return a not found response if item details are not found
                return ResponseEntity.notFound().build();
            }
        } catch (VendorNotFoundException e) {
            // Log the exception
            log.error("Vendor not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Vendor not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ItemNotFoundException e) {
            // Log the exception
            log.error("Item not found: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(null, "Item not found: " + e.getMessage(), e.getMessage(), "error", 404));
        } catch (ExecutionException | InterruptedException e) {
            // Log the exception
            log.error("Error fetching item details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseUtils.createErrorResponse("Failed to fetch item details: " + e.getMessage()));
        } catch (Exception e) {
            // Log the exception
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseUtils.createErrorResponse("Unexpected error occurred: " + e.getMessage()));
        }
    }







/*


    @PostMapping("/updateUnits")
    public ResponseEntity<ApiResponse<String>> updateUnits(
            @RequestParam String vendorId,
            @RequestParam String categoryName,
            @RequestParam String itemId,
            @RequestParam int Units) {

        try {
            int updatedUnits = categoryService.updateUnits(vendorId, categoryName, itemId, Units);
            return ResponseEntity.ok(ResponseUtils.createOkResponse("Units updated successfully. New units: " + updatedUnits));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseUtils.createErrorResponse("Error updating units of an item: " + e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseUtils.createErrorResponse("Error updating units of an item: " + e.getMessage()));
        }
    }

    @PostMapping("/updateOutOfStock")
    public ResponseEntity<ApiResponse<String>> updateOutOfStock(
            @RequestParam String vendorId,
            @RequestParam String categoryName,
            @RequestParam String itemId) {

        try {
            boolean outOfStock = categoryService.updateOutOfStock(vendorId, categoryName, itemId);
            String message = outOfStock ? "Out of stock." : "In stock.";
            ApiResponse<String> response = ResponseUtils.createOkResponse(String.valueOf(outOfStock));
            response.setMessage(message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> errorResponse = ResponseUtils.createErrorResponse("Error updating outOfStock field of an item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }*/
}
