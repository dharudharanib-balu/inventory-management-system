package com.inventory.service;

import com.inventory.dto.SupplierDTO;
import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID " + id + " not found"));
        return convertToDTO(supplier);
    }

    @Transactional
    public SupplierDTO createSupplier(SupplierDTO dto) {
        if (supplierRepository.existsByEmail(dto.getEmail().trim())) {
            throw new DuplicateResourceException("Supplier with email '" + dto.getEmail() + "' already exists");
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierName(dto.getSupplierName().trim());
        supplier.setEmail(dto.getEmail().trim().toLowerCase());
        supplier.setPhone(dto.getPhone().trim());
        supplier.setAddress(dto.getAddress().trim());

        Supplier saved = supplierRepository.save(supplier);
        return convertToDTO(saved);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO dto) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID " + id + " not found"));

        if (supplierRepository.existsByEmailAndSupplierIdNot(dto.getEmail().trim(), id)) {
            throw new DuplicateResourceException("Another supplier with email '" + dto.getEmail() + "' already exists");
        }

        existing.setSupplierName(dto.getSupplierName().trim());
        existing.setEmail(dto.getEmail().trim().toLowerCase());
        existing.setPhone(dto.getPhone().trim());
        existing.setAddress(dto.getAddress().trim());

        Supplier updated = supplierRepository.save(existing);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID " + id + " not found"));

        if (!productRepository.findBySupplier_SupplierId(id).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete supplier '" + supplier.getSupplierName() + 
                    "' because they have associated products in inventory. Please delete or reassign the products first.");
        }

        supplierRepository.delete(supplier);
    }

    public List<SupplierDTO> searchSuppliers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSuppliers();
        }
        return supplierRepository.searchSuppliers(query.trim()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SupplierDTO convertToDTO(Supplier supplier) {
        return new SupplierDTO(
                supplier.getSupplierId(),
                supplier.getSupplierName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress()
        );
    }
}
