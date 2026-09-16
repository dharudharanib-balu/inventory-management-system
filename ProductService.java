package com.inventory.service;

import com.inventory.dto.ProductDTO;
import com.inventory.entity.Product;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, 
                          SupplierRepository supplierRepository,
                          SaleRepository saleRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        if (productRepository.existsByProductName(dto.getProductName().trim())) {
            throw new DuplicateResourceException("Product with name '" + dto.getProductName() + "' already exists");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID " + dto.getSupplierId() + " does not exist"));

        Product product = new Product();
        product.setProductName(dto.getProductName().trim());
        product.setCategory(dto.getCategory().trim());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setSupplier(supplier);
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        if (productRepository.existsByProductNameAndProductIdNot(dto.getProductName().trim(), id)) {
            throw new DuplicateResourceException("Another product with name '" + dto.getProductName() + "' already exists");
        }

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID " + dto.getSupplierId() + " does not exist"));

        existing.setProductName(dto.getProductName().trim());
        existing.setCategory(dto.getCategory().trim());
        existing.setPrice(dto.getPrice());
        existing.setQuantity(dto.getQuantity());
        existing.setSupplier(supplier);
        existing.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);

        Product updated = productRepository.save(existing);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        if (!saleRepository.findByProduct_ProductId(id).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete product '" + product.getProductName() + 
                    "' because it has associated sales records. Please delete the sales records first.");
        }

        productRepository.delete(product);
    }

    public List<ProductDTO> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(query.trim()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategory(String category) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("All")) {
            return getAllProducts();
        }
        return productRepository.findByCategoryIgnoreCase(category.trim()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        int safeThreshold = (threshold != null && threshold > 0) ? threshold : 5;
        return productRepository.findByQuantityLessThanEqualOrderByQuantityAsc(safeThreshold).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    public ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getCategory(),
                product.getPrice(),
                product.getQuantity(),
                product.getSupplier() != null ? product.getSupplier().getSupplierId() : null,
                product.getSupplier() != null ? product.getSupplier().getSupplierName() : "N/A",
                product.getDescription()
        );
    }
}
