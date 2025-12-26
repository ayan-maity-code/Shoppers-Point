package com.shopperspoint.service;

import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.Customer;
import com.shopperspoint.entity.Seller;
import com.shopperspoint.exceptionhandler.AccountAlreadyActivatedException;
import com.shopperspoint.exceptionhandler.UserNotFoundException;
import com.shopperspoint.repository.CustomerRepo;
import com.shopperspoint.repository.SellerRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserActivationService {

    private final CustomerRepo customerRepo;
    private final SellerRepo sellerRepo;
    private final EmailService emailService;

    @Autowired
    public UserActivationService(CustomerRepo customerRepo, SellerRepo sellerRepo, EmailService emailService) {
        this.customerRepo = customerRepo;
        this.sellerRepo = sellerRepo;
        this.emailService = emailService;
    }

    @Value("${success.message}")
    private String message;

    @Transactional
    public GenericResponse activateCustomer(Long id) {
        log.info("Activating customer account with ID: {}", id);
        Optional<Customer> optionalCustomer = customerRepo.findById(id);

        if (optionalCustomer.isEmpty()) {
            log.warn("Customer not found for ID: {}", id);
            throw new UserNotFoundException("User id not found");
        }

        Customer customer = optionalCustomer.get();


        if (Boolean.TRUE.equals(customer.getIsActive())) {
            log.warn("Customer account already activated for email: {}", customer.getEmail());
            throw new AccountAlreadyActivatedException("Account is already activated");
        }

        customer.setIsActive(true);
        customerRepo.save(customer);

        emailService.notifyAccountActivationSuccess(customer.getEmail());

        log.info("Customer account activated successfully for email: {}", customer.getEmail());
        return new GenericResponse("Account has been successfully activated", message, LocalDateTime.now());

    }

    @Transactional
    public GenericResponse deActivateCustomer(Long id) {
        log.info("Deactivating customer account with ID: {}", id);
        Optional<Customer> optionalCustomer = customerRepo.findById(id);

        if (optionalCustomer.isEmpty()) {
            log.warn("Customer not found for ID: {}", id);
            throw new UserNotFoundException("User id not found");
        }

        Customer customer = optionalCustomer.get();


        if (Boolean.TRUE.equals(customer.getIsActive())) {
            customer.setIsActive(false);
            customerRepo.save(customer);
            emailService.notifyAccountDeActivation(customer.getEmail());
            log.info("Customer account deactivated successfully for email: {}", customer.getEmail());
        } else {
            log.warn("Customer account already deactivated for email: {}", customer.getEmail());
            throw new AccountAlreadyActivatedException("Account already deactivated");
        }

        return new GenericResponse("Account has been successfully deactivated", message, LocalDateTime.now());

    }


    @Transactional
    public GenericResponse activateSeller(Long id) {
        log.info("Activating seller account with ID: {}", id);
        Optional<Seller> optionalSeller = sellerRepo.findById(id);

        if (optionalSeller.isEmpty()) {
            log.warn("Seller not found for ID: {}", id);
            throw new UserNotFoundException("User id not found");
        }

        Seller seller = optionalSeller.get();


        if (Boolean.TRUE.equals(seller.getIsActive())) {
            log.warn("Seller account already activated for email: {}", seller.getEmail());
            throw new AccountAlreadyActivatedException("Account is already activated");
        }

        seller.setIsActive(true);
        sellerRepo.save(seller);

        emailService.notifyAccountActivationSuccess(seller.getEmail());
        log.info("Seller account activated successfully for email: {}", seller.getEmail());

        return new GenericResponse("Account has been successfully activated", message, LocalDateTime.now());

    }


    @Transactional
    public GenericResponse deActivateSeller(Long id) {
        log.info("Deactivating seller account with ID: {}", id);
        Optional<Seller> optionalSeller = sellerRepo.findById(id);

        if (optionalSeller.isEmpty()) {
            log.warn("Seller not found for ID: {}", id);
            throw new UserNotFoundException("User id not found");
        }

        Seller seller = optionalSeller.get();

        if (Boolean.TRUE.equals(seller.getIsActive())) {
            seller.setIsActive(false);
            sellerRepo.save(seller);
            emailService.notifyAccountDeActivation(seller.getEmail());
            log.info("Seller account deactivated successfully for email: {}", seller.getEmail());
        } else {
            log.warn("Seller account already deactivated for email: {}", seller.getEmail());
            throw new AccountAlreadyActivatedException("Account already deactivated");
        }

        return new GenericResponse("Account has been successfully deactivated", message, LocalDateTime.now());

    }
}
