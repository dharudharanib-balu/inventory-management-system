package com.inventory.service;

import com.inventory.dto.SaleRequestDTO;
import com.inventory.dto.SaleResponseDTO;
import com.inventory.entity.Customer;
import com.inventory.entity.Product;
import com.inventory.entity.Sale;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public SaleService(SaleRepository saleRepository, 
                       ProductRepository productRepository, 
                       CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public List<SaleResponseDTO> getAllSales() {
        return saleRepository.findAllByOrderBySaleDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SaleResponseDTO getSaleById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record with ID " + id + " not found"));
        return convertToDTO(sale);
    }

    @Transactional
    public SaleResponseDTO createSale(SaleRequestDTO request) {
        // 1. Validate Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + request.getProductId() + " not found"));

        // 2. Validate Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + request.getCustomerId() + " not found"));

        // 3. Check Stock Availability
        if (product.getQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for product '%s'. Available: %d, Requested: %d",
                    product.getProductName(), product.getQuantity(), request.getQuantity()
            ));
        }

        // 4. Reduce Product Stock Automatically
        int updatedQuantity = product.getQuantity() - request.getQuantity();
        product.setQuantity(updatedQuantity);
        productRepository.save(product);

        // 5. Calculate Total Amount: Price * Quantity (with precise rounding)
        BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
        BigDecimal qty = BigDecimal.valueOf(request.getQuantity());
        BigDecimal total = unitPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);

        // 6. Persist Sale Record
        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setCustomer(customer);
        sale.setQuantity(request.getQuantity());
        sale.setTotalAmount(total.doubleValue());
        sale.setSaleDate(LocalDateTime.now());

        Sale saved = saleRepository.save(sale);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale record with ID " + id + " not found"));

        // Restore the product stock upon sale deletion
        Product product = sale.getProduct();
        if (product != null) {
            product.setQuantity(product.getQuantity() + sale.getQuantity());
            productRepository.save(product);
        }

        saleRepository.delete(sale);
    }

    public SaleResponseDTO convertToDTO(Sale sale) {
        return new SaleResponseDTO(
                sale.getSaleId(),
                sale.getProduct() != null ? sale.getProduct().getProductId() : null,
                sale.getProduct() != null ? sale.getProduct().getProductName() : "Unknown Product",
                sale.getProduct() != null ? sale.getProduct().getPrice() : 0.0,
                sale.getCustomer() != null ? sale.getCustomer().getCustomerId() : null,
                sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : "Unknown Customer",
                sale.getCustomer() != null ? sale.getCustomer().getEmail() : "N/A",
                sale.getQuantity(),
                sale.getTotalAmount(),
                sale.getSaleDate()
        );
    }
}
