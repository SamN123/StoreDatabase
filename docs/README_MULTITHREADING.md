# Multi-threading Implementation

This document describes the multi-threading implementation in the store database application.

## Overview

Multi-threading has been implemented to enhance the user experience by making database operations non-blocking. This ensures the application remains responsive even during long-running operations such as retrieving large datasets or processing purchases.

## Implementation Details

### 1. Thread Manager

A central `ThreadManager` utility class has been created to manage background threads:

```java
package src.Util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
```

Key Features:
- uses a fixed thread pool with 5 threads to limit resource usage
- provides methods for executing tasks asynchronously with callbacks
- handles exceptions gracefully with error callbacks
- includes proper shutdown mechanism to release resources

### 2. Asynchronous Database Operations

Database operations that might take time are now executed in background threads:

- **Product Retrieval**: loading product lists is now done asynchronously
- **Search Operations**: product searches run in the background
- **Data Analysis**: sales analysis queries execute without blocking the UI

### 3. Data Transfer Objects

New classes were created to facilitate data transfer between threads:

- `ProductData`: holds individual product information
- `ProductPageData`: contains a collection of products and pagination information

### 4. User Experience Improvements

- loading indicators inform users when operations are in progress
- the UI remains responsive during database operations
- error handling is improved with specific error messages

### 5. Thread Safety Considerations

- atomic variables are used for thread synchronization
- final variables are used for lambda captures
- proper resource cleanup in all cases

## Usage Example

```java
// create a callable for the database operation
Callable<ProductPageData> dataTask = () -> {
    ProductPageData pageData = new ProductPageData();
    
    // database operations...
    
    return pageData;
};

// execute the database operation in a background thread
ThreadManager.executeAsync(
    dataTask,
    // success callback
    pageData -> {
        // display products
    },
    // error callback
    e -> {
        System.err.println("Error retrieving products: " + e.getMessage());
    }
);
```

## Benefits

1. **Improved Responsiveness**: the application remains responsive even during intensive operations
2. **Better User Experience**: users don't have to wait for operations to complete
3. **Resource Management**: thread pool prevents resource exhaustion
4. **Error Isolation**: errors in background tasks don't crash the main application

## Future Enhancements

1. add progress reporting for long-running operations
2. implement task cancellation for operations that take too long
3. add more sophisticated thread pool management based on system resources
4. extend multi-threading to more operations like batch updates
