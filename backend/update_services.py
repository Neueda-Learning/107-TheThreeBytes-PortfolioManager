import os
import re

service_dir = "src/main/java/com/example/hsbcproject/service"

files_to_update = [
    "TransactionService.java",
    "TaxService.java",
    "RiskService.java",
    "DashboardService.java",
    "PerformanceService.java",
    "PortfolioTrackingService.java"
]

for filename in files_to_update:
    filepath = os.path.join(service_dir, filename)
    if not os.path.exists(filepath):
        continue
    
    with open(filepath, "r") as f:
        content = f.read()

    # Replace BigDecimal.valueOf(xxx.getQuantity()) -> xxx.getQuantity()
    content = re.sub(r'BigDecimal\.valueOf\(([^)]+\.getQuantity\(\))\)', r'\1', content)

    # For DashboardService
    if filename == "DashboardService.java":
        content = content.replace("Map<String, Long> quantityByType = new HashMap<>();", "Map<String, BigDecimal> quantityByType = new HashMap<>();")
        content = content.replace("long totalQuantity = 0;", "BigDecimal totalQuantity = BigDecimal.ZERO;")
        content = content.replace("totalQuantity += item.getQuantity();", "totalQuantity = totalQuantity.add(item.getQuantity());")
        content = content.replace("quantityByType.merge(type, (long) item.getQuantity(), Long::sum);", "quantityByType.merge(type, item.getQuantity(), BigDecimal::add);")

    # For PortfolioTrackingService
    if filename == "PortfolioTrackingService.java":
        # totalQuantity comes from DashboardResponse, we just set it (it's already BigDecimal in DashboardResponse)
        pass

    with open(filepath, "w") as f:
        f.write(content)

print("Updated Services")
