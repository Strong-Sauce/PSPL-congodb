package com.postSale.amcProject.Services;

import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.nodes.Customer;
import com.postSale.amcProject.Repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCust(Customer customer) {
        customer.setCustId(UUID.randomUUID().toString());
        customerRepository.createCustomer(customer.getCustId(), customer.getCustName());
        return customer;
    }

    @Transactional
    public Customer updateCus(Customer customers) {
        if (!customerRepository.existsCustomerById(customers.getCustId())) {
            throw new ResourceNotFoundException("Customer", customers.getCustId());
        }
        customerRepository.updateCustomer(customers.getCustId(), customers.getCustName());
        return customers;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllCustomers();
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findCustomerById(id);
    }

    @Transactional
    public boolean deleteCustomer(String id) {
        if (!customerRepository.existsCustomerById(id)) {
            throw new ResourceNotFoundException("Customer", id);
        }
        customerRepository.deleteCustomerByAppId(id);
        return true;
    }
}
