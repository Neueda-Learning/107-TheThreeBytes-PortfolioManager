import os
import re

model_dir = "app/src/main/java/com/hsbc/portfoliomanager/data/model"

for filename in os.listdir(model_dir):
    if not filename.endswith(".kt"):
        continue
    filepath = os.path.join(model_dir, filename)

    with open(filepath, "r") as f:
        content = f.read()

    original = content
    # Generic Int to BigDecimal for quantity fields
    content = re.sub(r'val quantity:\s*Int', r'val quantity: BigDecimal', content)
    
    # Specifics for DashboardResponse and PortfolioSummaryResponse
    content = content.replace("val totalQuantity: Long", "val totalQuantity: BigDecimal")
    content = content.replace("val quantityByAssetType: Map<String, Long>", "val quantityByAssetType: Map<String, BigDecimal>")
    
    # Add BigDecimal import if needed
    if "BigDecimal" in content and "import java.math.BigDecimal" not in content:
        content = "import java.math.BigDecimal\n" + content

    if content != original:
        with open(filepath, "w") as f:
            f.write(content)
        print(f"Updated {filename}")

print("Updated Kotlin models.")
