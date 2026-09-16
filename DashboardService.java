package com.inventory.service;

import com.inventory.dto.DashboardSummaryDTO;
import com.inventory.dto.ProductDTO;
import com.inventory.dto.SaleResponseDTO;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final SaleService saleService;

    @Autowired
    public DashboardService(ProductRepository productRepository, 
                            SupplierRepository supplierRepository, 
                            CustomerRepository customerRepository, 
                            SaleRepository saleRepository,
                            ProductService productService,
                            SaleService saleService) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.productService = productService;
        this.saleService = saleService;
    }

    public DashboardSummaryDTO getDashboardSummary() {
        long totalProducts = productRepository.count();
        long totalSuppliers = supplierRepository.count();
        long totalCustomers = customerRepository.count();
        long totalSales = saleRepository.count();
        Double totalRevenue = saleRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = 0.0;

        long lowStockCount = productRepository.countByQuantityLessThanEqual(5);

        List<ProductDTO> lowStockProducts = productRepository.findByQuantityLessThanEqualOrderByQuantityAsc(5)
                .stream()
                .map(productService::convertToDTO)
                .collect(Collectors.toList());

        List<SaleResponseDTO> recentSales = saleRepository.findTop5ByOrderBySaleDateDesc()
                .stream()
                .map(saleService::convertToDTO)
                .collect(Collectors.toList());

        List<String> categories = productRepository.findDistinctCategories();

        return new DashboardSummaryDTO(
                totalProducts,
                totalSuppliers,
                totalCustomers,
                totalSales,
                Math.round(totalRevenue * 100.0) / 100.0,
                lowStockCount,
                lowStockProducts,
                recentSales,
                categories
        );
    }
}
