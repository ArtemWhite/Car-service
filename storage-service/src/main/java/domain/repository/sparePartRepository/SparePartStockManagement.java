package domain.repository.sparePartRepository;

public interface SparePartStockManagement {
    int getStockQuantity(String sparePartId);
    void updateStock(String sparePartId, int newQuantity, String sectionId, String location);

    String getSectionId(String sparePartId);
    String getLocation(String sparePartId);
}