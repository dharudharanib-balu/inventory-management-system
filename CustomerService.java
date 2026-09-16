package com.inventory.service;

import com.inventory.dto.CustomerDTO;
import com.inventory.entity.Customer;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, SaleRepository saleRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + id + " not found"));
        return convertToDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO dto) {
        if (customerRepository.existsByEmail(dto.getEmail().trim())) {
            throw new DuplicateResourceException("Customer with email '" + dto.getEmail() + "' already exists");
        }

        Customer customer = new Customer();
        customer.setCustomerName(dto.getCustomerName().trim());
        customer.setEmail(dto.getEmail().trim().toLowerCase());
        customer.setPhone(dto.getPhone().trim());
        customer.setAddress(dto.getAddress().trim());

        Customer saved = customerRepository.save(customer);
        return convertToDTO(saved);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO dto) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + id + " not found"));

        if (customerRepository.existsByEmailAndCustomerIdNot(dto.getEmail().trim(), id)) {
            throw new DuplicateResourceException("Another customer with email '" + dto.getEmail() + "' already exists");
        }

        existing.setCustomerName(dto.getCustomerName().trim());
        existing.setEmail(dto.getEmail().trim().toLowerCase());
        existing.setPhone(dto.getPhone().trim());
        existing.setAddress(dto.getAddress().trim());

        Customer updated = customerRepository.save(existing);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with ID " + id + " not found"));

        if (!saleRepository.findByCustomer_CustomerId(id).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete customer '" + customer.getCustomerName() + 
                    "' because they have recorded sales transactions. Please remove sales records first.");
        }

        customerRepository.delete(customer);
    }

    public List<CustomerDTO> searchCustomers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchCustomers(query.trim()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO convertToDTO(Customer customer) {
        return new CustomerDTO(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
        );
    }
}
