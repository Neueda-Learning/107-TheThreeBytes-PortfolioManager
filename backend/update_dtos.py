import os
import re

dto_dir = "src/main/java/com/example/hsbcproject/dto"

files_to_update = [
    "CreatePortfolioItemRequest.java",
    "UpdatePortfolioItemRequest.java",
    "CreateTransactionRequest.java",
    "PortfolioItemResponse.java",
    "TransactionResponse.java",
    "PerformanceItemResponse.java",
    "DashboardResponse.java",
    "PortfolioSummaryResponse.java",
    "PortfolioSnapshotResponse.java",
    "SellHoldingRequest.java"
]

for filename in files_to_update:
    filepath = os.path.join(dto_dir, filename)
    if not os.path.exists(filepath):
        print(f"Skipping {filename} - not found")
        continue

    with open(filepath, "r") as f:
        content = f.read()

    # Create/Update/Transaction requests
    content = re.sub(r'Integer quantity,', r'BigDecimal quantity,', content)
    content = re.sub(r'@Min\(value\s*=\s*1\s*(,\s*message\s*=\s*"[^"]*")?\)', r'@DecimalMin(value = "0.00000001"\1)', content)
    
    # SellHoldingRequest may not have quantity yet, add it
    if filename == "SellHoldingRequest.java":
        if "BigDecimal quantity" not in content:
            content = content.replace("BigDecimal pricePerUnit\n)", "BigDecimal pricePerUnit,\n        BigDecimal quantity\n)")

    # Responses
    content = content.replace("long totalQuantity,", "BigDecimal totalQuantity,")
    content = content.replace("Long totalQuantity", "BigDecimal totalQuantity")
    content = content.replace("Map<String, Long> quantityByAssetType", "Map<String, BigDecimal> quantityByAssetType")

    # Add BigDecimal import if needed
    if "BigDecimal" in content and "import java.math.BigDecimal;" not in content:
        content = content.replace("public record", "import java.math.BigDecimal;\n\npublic record")
    
    # Add DecimalMin import if needed
    if "@DecimalMin" in content and "import jakarta.validation.constraints.DecimalMin;" not in content:
        content = content.replace("import jakarta.validation.constraints.NotNull;", "import jakarta.validation.constraints.NotNull;\nimport jakarta.validation.constraints.DecimalMin;")

    # Remove @Min import if unused
    if "@Min" not in content:
        content = content.replace("import jakarta.validation.constraints.Min;\n", "")

    with open(filepath, "w") as f:
        f.write(content)

print("Updated DTOs.")
