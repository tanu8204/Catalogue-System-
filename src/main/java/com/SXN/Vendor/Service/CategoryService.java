package com.SXN.Vendor.Service;


import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface CategoryService {

    Map<String, Object> saveCategoryDetails(String vendorId, String Category, String subcategory, String name, String description, String itemId,
                                            List<String> pictures, int price,
                                            Map<String, Integer> size, boolean outOfStock,
                                            int lockinPeriod)
            throws ExecutionException, InterruptedException;

    List<Map<String,Object>> getCatalogue(String vendorId) throws ExecutionException, InterruptedException;

    boolean vendorExists(String vendorId) throws ExecutionException, InterruptedException;

    boolean categoryExists(String vendorId, String category, String subcategory) throws ExecutionException, InterruptedException;

    boolean itemExists(String vendorId, String category, String subcategory, String itemId) throws ExecutionException, InterruptedException;

    boolean itemExistsFromCata(String vendorId, String itemId) throws ExecutionException, InterruptedException;

    boolean isOutOfStock(String vendorId, String category, String subcategory, String itemId) throws ExecutionException, InterruptedException;

    void deleteOutOfStockItems(String vendorId, String Category, String subcategory, String itemId) throws ExecutionException, InterruptedException;


    List<Map<String, Object>> getPending(String vendorId) throws ExecutionException, InterruptedException;

    List<Map<String, Object>> getCompletedOrders(String vendorId) throws ExecutionException, InterruptedException;

    Map<String,Object> getItemDetailsByVendorId(String vendorId, String itemId) throws ExecutionException, InterruptedException;


}
