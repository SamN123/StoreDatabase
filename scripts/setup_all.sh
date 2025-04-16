#!/bin/bash

# Complete Database Setup Script
# This script sets up the entire database with all necessary objects

# Display banner
echo "====================================================="
echo "  Store Database Complete Setup Script"
echo "====================================================="
echo

# Try to find MySQL in common locations
MYSQL_LOCATIONS=(
    "mysql"                                  # If MySQL is in PATH
    "/usr/bin/mysql"                         # Common Linux location
    "/usr/local/bin/mysql"                   # Common macOS location
    "/usr/local/mysql/bin/mysql"             # Official MySQL on macOS
    "/opt/homebrew/bin/mysql"                # Homebrew on Apple Silicon
    "/opt/homebrew/mysql/bin/mysql"          # Homebrew MySQL on macOS
    "/opt/homebrew/Cellar/mysql-client/*/bin/mysql"  # Homebrew MySQL client
    "/usr/local/mysql-*/bin/mysql"           # Official MySQL with version
    "C:/Program Files/MySQL/MySQL Server */bin/mysql.exe"  # Windows
    "C:/xampp/mysql/bin/mysql.exe"           # XAMPP on Windows
)

MYSQL_PATH=""

# Find the first available MySQL client
for location in "${MYSQL_LOCATIONS[@]}"; do
    # Handle wildcards in paths
    if [[ "$location" == *"*"* ]]; then
        # Use ls to expand wildcards and get the first match
        expanded_path=$(ls -d $location 2>/dev/null | head -n 1)
        if [ -n "$expanded_path" ] && [ -f "$expanded_path" ]; then
            MYSQL_PATH="$expanded_path"
            break
        fi
    elif command -v "$location" &>/dev/null || [ -f "$location" ]; then
        MYSQL_PATH="$location"
        break
    fi
done

# If MySQL wasn't found in common locations, ask the user
if [ -z "$MYSQL_PATH" ]; then
    echo "MySQL client not found in common locations."
    echo "Please enter the full path to your MySQL client:"
    read -p "> " MYSQL_PATH
    
    if [ ! -f "$MYSQL_PATH" ]; then
        echo "Error: MySQL client not found at the specified path."
        echo "Please install MySQL or provide the correct path."
        exit 1
    fi
fi

echo "Using MySQL client at: $MYSQL_PATH"

# Prompt for MySQL credentials
read -p "Enter MySQL username [root]: " mysql_user
mysql_user=${mysql_user:-root}

# Don't echo the password (input will be hidden for security)
echo -n "Enter MySQL password (input will be hidden): "
read -s mysql_password
echo -e "\nPassword received."

# Create a temporary my.cnf file for MySQL credentials
temp_mycnf=$(mktemp)
cat > "$temp_mycnf" << EOF
[client]
user=$mysql_user
password=$mysql_password
EOF

# Function to execute SQL script
execute_script() {
    echo
    echo "Executing $1..."
    "$MYSQL_PATH" --defaults-file="$temp_mycnf" $2 < "$1"
    if [ $? -eq 0 ]; then
        echo "✓ Successfully executed $1"
    else
        echo "✗ Error executing $1"
        exit 1
    fi
}

# Step 1: Create the database schema
echo
echo "Step 1: Creating database schema..."
execute_script "sql/schema/StoreDB.sql" ""

# Step 2: Add authentication support
echo
echo "Step 2: Adding authentication support..."
execute_script "sql/auth/StoreDB_Auth.sql" "storedb"

# Step 3: Create missing database objects
echo
echo "Step 3: Creating missing database objects..."
execute_script "sql/setup/SetupMissingDatabaseObjects.sql" "storedb"

# Step 4: Verify the setup
echo
echo "Step 4: Verifying the setup..."
echo "Checking stored procedures..."
"$MYSQL_PATH" --defaults-file="$temp_mycnf" storedb -e "SHOW PROCEDURE STATUS WHERE Db = 'storedb';"

echo
echo "Checking views..."
"$MYSQL_PATH" --defaults-file="$temp_mycnf" storedb -e "SHOW FULL TABLES IN storedb WHERE Table_type = 'VIEW';"

echo
echo "Checking Persons table structure..."
"$MYSQL_PATH" --defaults-file="$temp_mycnf" storedb -e "DESCRIBE Persons;"

# Clean up
rm "$temp_mycnf"

echo
echo "====================================================="
echo "  Database setup completed successfully!"
echo "====================================================="
echo
echo "You can now run the application and log in with:"
echo "  - Default admin: admin@store.com (set password on first login)"
echo "  - Or register a new user"
echo
echo "To find your customer ID, use the 'Find My Customer ID'"
echo "feature in the Transactions menu."
echo "====================================================="
