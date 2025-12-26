package com.shopperspoint.service;


import com.shopperspoint.dto.*;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.Address;
import com.shopperspoint.entity.Customer;
import com.shopperspoint.entity.Role;
import com.shopperspoint.entity.User;
import com.shopperspoint.exceptionhandler.*;
import com.shopperspoint.jwt.JwtUtil;
import com.shopperspoint.repository.ActivationTokenRepo;
import com.shopperspoint.repository.AddressRepo;
import com.shopperspoint.repository.CustomerRepo;
import com.shopperspoint.repository.UserRepo;
import com.shopperspoint.utill.ImageUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class CustomerService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ActivationTokenService tokenService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final ActivationTokenRepo tokenRepo;
    private final CustomerRepo customerRepo;
    private final JwtUtil jwtUtil;
    private final AddressRepo addressRepo;
    private final MessageSource messageSource;

    @Autowired
    public CustomerService(UserRepo userRepo, PasswordEncoder passwordEncoder, ActivationTokenService tokenService,
                           EmailService emailService, AuthenticationManager authenticationManager,
                           ActivationTokenRepo tokenRepo, CustomerRepo customerRepo,
                           JwtUtil jwtUtil, AddressRepo addressRepo, MessageSource messageSource) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.tokenRepo = tokenRepo;
        this.customerRepo = customerRepo;
        this.jwtUtil = jwtUtil;
        this.addressRepo = addressRepo;
        this.messageSource = messageSource;
    }


    @Value("${customerRole}")
    private String role;

    @Value("${app.regex.email}")
    private String emailRegex;

    @Value("${email.name}")
    private String emailName;

    @Value("${image.user}")
    private String type;

    @Value("${success.message}")
    private String message;


    public ResponseEntity<GenericResponse> registerCustomer(CustomerDTO customerDto, Locale locale) {
        log.info("Registering customer with email: {}", customerDto.getEmail());
        if (Boolean.TRUE.equals(userRepo.existsByEmail(customerDto.getEmail()))) {
            log.warn("Email already exists: {}", customerDto.getEmail());
            throw new DuplicateEntryException("Email already exists, please try with another email");
        }

        Customer customer = new Customer();
        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setEmail(customerDto.getEmail());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        customer.setIsActive(false);
        customer.setIsExpired(false);
        customer.setIsDeleted(false);
        customer.setIsLocked(false);
        customer.setInvalidAttemptCount(0);

        if (customerDto.getAddress() != null && !customerDto.getAddress().isEmpty()) {
            List<Address> addresses = customerDto.getAddress().stream()
                    .map(
                            addressDTO ->
                            {
                                Address address = new Address();
                                address.setAddressLine(addressDTO.getAddressLine());
                                address.setCountry(addressDTO.getCountry());
                                address.setLabel(addressDTO.getLabel());
                                address.setState(addressDTO.getState());
                                address.setZipCode(addressDTO.getZipCode());
                                address.setCity(addressDTO.getCity());
                                address.setUser(customer);
                                return address;
                            }
                    ).toList();

            customer.setAddresses(addresses);
        }

        Set<Role> roles = new HashSet<>();
        Role customerRole = new Role();
        customerRole.setAuthority(role);
        roles.add(customerRole);
        customer.setRoles(roles);
        customer.setPasswordUpdatedDate(LocalDateTime.now());
        userRepo.save(customer);

        String token = tokenService.generateToken(customer.getEmail());

        emailService.sendActivationMail(emailName, token);
        emailService.sendActivationMail(customer.getEmail(), token);

        log.info("Customer registered successfully, activation token: {}", token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("customer.register", null, locale), message, LocalDateTime.now()));

    }


    @Transactional
    public ResponseEntity<GenericResponse> activateAccount(String token) {
        log.info("Activating account using token: {}", token);
        String email = tokenService.validateToken(token);


        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            log.error("User not found for activation token: {}", token);
            throw new UserNotFoundException("User not found");
        }

        user.setIsActive(true);
        userRepo.save(user);
        tokenRepo.deleteByToken(token);

        log.info("Account activated for email: {}", email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Account activated Successfully", message, LocalDateTime.now()));
    }


    @Transactional
    public ResponseEntity<GenericResponse> resendActivationLink(String email) {
        log.info("Resending activation link to: {}", email);
        if (!email.matches(emailRegex)) {
            log.warn("Invalid email format: {}", email);
            throw new BadRequestException("Invalid email format, valid format is email@example.com");
        }

        User user = userRepo.findByEmail(email.toLowerCase()).orElse(null);
        if (user == null) {
            log.error("User not found for email: {}", email);
            throw new UserNotFoundException("User not found");
        }

        if (Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("Account already activated for email: {}", email);
            throw new TokenAlreadyUsedException("Account already activated");
        }
        tokenService.deleteOldToken(email);
        String token = tokenService.generateToken(email);
        emailService.sendActivationMail(email, token);

        log.info("Activation link sent to: {}", email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("A new activation sent to your email", message, LocalDateTime.now()));
    }


    //Get all customer by admin
    public List<CustomerResponseDTO> getAllCustomer(int page, int size, String sort, String emailFilter) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        List<Customer> customers = customerRepo.findByRoleAndEmailFilter("CUSTOMER",
                emailFilter,
                pageable
        );

        if (customers.isEmpty()) {
            throw new UserNotFoundException("No customer is present");
        }

        return customers.stream().map(
                user -> (
                        new CustomerResponseDTO(
                                user.getId(),
                                user.getFirstName() +
                                        (user.getMiddleName() != null ? " " + user.getMiddleName() : "") +
                                        " " + user.getLastName(),
                                user.getEmail(),
                                user.getIsActive()
                        )
                )
        ).toList();

    }


    private Customer getLoggedinCustomer(HttpServletRequest request) {
        log.info("Fetching logged-in customer from token");
        String token = null;
        String email = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            log.error("Access token is missing in cookies");
            throw new AccessDeniedException("Token is not present in cookies");
        }

        email = jwtUtil.extractUserName(token);

        if (email == null) {
            log.error("Token is invalid or expired");
            throw new AccessDeniedException("Invalid token");
        }

        Customer customer = customerRepo.findByEmail(email);

        if (customer == null) {
            log.error("Customer not found for email: {}", email);
            throw new UserNotFoundException("User not found");
        }
        log.info("Customer fetched successfully with email: {}", email);
        return customer;
    }


    public CustomerViewProfileDTO getProfile(HttpServletRequest request) {
        log.info("Fetching profile for logged-in customer");
        Customer customer = getLoggedinCustomer(request);

        String imageUrl = ImageUtils.getImage(customer.getId(), type);
        log.info("Customer profile fetched: ID = {}, Name = {} {}", customer.getId(), customer.getFirstName(), customer.getLastName());
        return new CustomerViewProfileDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getIsActive(),
                customer.getPhoneNumber(),
                imageUrl
        );

    }


    public List<AddressDTO> getAllAddresses(HttpServletRequest request) {
        log.info("Fetching all addresses for logged-in customer");
        Customer customer = getLoggedinCustomer(request);

        List<Address> addresses = customer.getAddresses();

        if (addresses.isEmpty()) {
            log.warn("No addresses found for customer ID: {}", customer.getId());
        }


        return addresses.stream()
                .map(address -> new AddressDTO(
                        address.getAddressLine(),
                        address.getLabel(),
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getZipCode()
                )).toList();

    }

    public ResponseEntity<GenericResponse> updateProfile(CustomerUpdateProfileDTO customerUpdateProfileDTO, HttpServletRequest request, Locale locale) {
        log.info("Updating profile for customer ID: {}", request.getAttribute("customerId"));
        Customer customer = getLoggedinCustomer(request);


        customer.setFirstName(Optional.ofNullable(customerUpdateProfileDTO.getFirstName()).orElse(customer.getFirstName()));
        customer.setMiddleName(Optional.ofNullable(customerUpdateProfileDTO.getMiddleName()).orElse(customer.getMiddleName()));
        customer.setLastName(Optional.ofNullable(customerUpdateProfileDTO.getLastName()).orElse(customer.getLastName()));
        customer.setPhoneNumber(Optional.ofNullable(customerUpdateProfileDTO.getPhoneNumber()).orElse(customer.getPhoneNumber()));

        if (customerUpdateProfileDTO.getImage() != null) {
            log.debug("Uploading image for customer ID: {}", customer.getId());
            ImageUtils.uploadImage(customerUpdateProfileDTO.getImage(), customer.getId(), type);
        }

        userRepo.save(customer);
        log.info("Profile updated successfully for customer ID: {}", customer.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("profile.update", null, locale), message, LocalDateTime.now()));


    }


    public ResponseEntity<GenericResponse> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        log.info("Updating password for customer ID: {}", request.getAttribute("customerId"));
        Customer customer = getLoggedinCustomer(request);

        customer.setPassword(passwordEncoder.encode(passwordDTO.getPassword()));
        customer.setPasswordUpdatedDate(LocalDateTime.now());
        userRepo.save(customer);
        emailService.notifyPasswordChanged(customer.getEmail());
        log.info("Password updated successfully for customer ID: {}", customer.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Password updated successfully", message, LocalDateTime.now()));
    }


    public ResponseEntity<GenericResponse> addNewAddress(AddressDTO addressDTO, HttpServletRequest request) {
        log.info("Adding new address for customer ID: {}", request.getAttribute("customerId"));
        Customer customer = getLoggedinCustomer(request);

        Address address = new Address();

        address.setAddressLine(addressDTO.getAddressLine());
        address.setLabel(addressDTO.getLabel());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setZipCode(addressDTO.getZipCode());
        address.setCity(addressDTO.getCity());
        address.setUser(customer);

        List<Address> addresses = new ArrayList<>();
        addresses.add(address);

        customer.setAddresses(addresses);

        userRepo.save(customer);

        log.info("New address added for customer ID: {}", customer.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("New address added successfully", message, LocalDateTime.now()));


    }


    public ResponseEntity<GenericResponse> deleteAddress(Long id, HttpServletRequest request) {
        log.info("Deleting address with ID: {} for customer ID: {}", id, request.getAttribute("customerId"));
        Customer customer = getLoggedinCustomer(request);

        Address address = addressRepo.findById(id).orElseThrow(
                () -> {
                    log.error("Address not found with ID: {}", id);
                    return new UserNotFoundException("Address not found with this id");
                }
        );

        if (!address.getUser().getId().equals(customer.getId())) {
            log.error("Access denied: Customer ID: {} is not authorized to delete address ID: {}", customer.getId(), id);
            throw new AccessDeniedException("You  are not allowed to delete this address");
        }

        addressRepo.deleteById(id);
        userRepo.save(customer);
        log.info("Address with ID: {} deleted successfully for customer ID: {}", id, customer.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Address deleted successfully", message, LocalDateTime.now()));

    }


    public ResponseEntity<GenericResponse> updateAddress(Long id, AddressDTO addressDTO, HttpServletRequest request) {
        log.info("Updating address with ID: {} for customer ID: {}", id, request.getAttribute("customerId"));
        Customer customer = getLoggedinCustomer(request);

        Address address = addressRepo.findById(id).orElseThrow(
                () -> {
                    log.error("Address not found with ID: {}", id);
                    return new UserNotFoundException("Address not found with this id");
                });

        if (!address.getUser().getId().equals(customer.getId())) {
            log.error("Access denied: Customer ID: {} is not authorized to update address ID: {}", customer.getId(), id);
            throw new AccessDeniedException("You are not allowed update this address");
        }

        if (addressDTO.getAddressLine() != null) {
            address.setAddressLine(addressDTO.getAddressLine());
        }

        if (addressDTO.getCity() != null) {
            address.setCity(addressDTO.getCity());
        }

        if (addressDTO.getState() != null) {
            address.setState(addressDTO.getState());
        }

        if (addressDTO.getCountry() != null) {
            address.setCountry(addressDTO.getCountry());
        }

        if (addressDTO.getZipCode() != null) {
            address.setZipCode(addressDTO.getZipCode());
        }

        if (addressDTO.getLabel() != null) {
            address.setLabel(addressDTO.getLabel());
        }

        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        customer.setAddresses(addresses);
        userRepo.save(customer);

        log.info("Address with ID: {} updated successfully for customer ID: {}", id, customer.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Address updated successfully", message, LocalDateTime.now()));

    }


}
