# StoreDatabase

A store database java application that manages transactions using an external mysql database.

## Features

- **Product Management**: add, modify, and remove products from inventory
- **Transaction Processing**: complete purchase transactions
- **Customer History**: view purchase history for customers
- **Authentication**: secure login with role-based access control
- **Optimized Queries**: efficient database operations for better performance
- **Multi-threading**: responsive ui with background processing
- **Error Handling**: comprehensive error handling and logging

## Documentation

Detailed documentation is available in the `docs` folder:

- [documentation index](docs/index.md) - central hub for all documentation
- [optimized queries](docs/README_OPTIMIZED_QUERIES.md) - database optimization techniques
- [multi-threading](docs/README_MULTITHREADING.md) - background thread implementation
- [error handling](docs/README_ERROR_HANDLING.md) - error handling system
- [authentication](docs/README_AUTH.md) - authentication and security

## Technical Details

### Java Application
- java database connectivity library for database access
- java.security library for password hashing with sha-256
- logging api for tracking critical actions
- jdbc transactions with connection.setAutoCommit(false)

### SQL Database
- prepared statements for secure sql queries
- optimized queries with joins, groupby, indexing, and limits
- triggers for validating data (e.g., preventing negative prices)
- secure password storage with hashing

## Resources
1. mysql installation: https://www.youtube.com/watch?v=7S_tz1z_5bA
2. password hashing method: https://www.youtube.com/watch?v=ef3kenC4xa0
