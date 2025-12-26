package com.shopperspoint.service;


import com.shopperspoint.dto.*;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.Address;
import com.shopperspoint.entity.Role;
import com.shopperspoint.entity.Seller;
import com.shopperspoint.exceptionhandler.*;
import com.shopperspoint.jwt.JwtUtil;
import com.shopperspoint.repository.AddressRepo;
import com.shopperspoint.repository.SellerRepo;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class SellerService {
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private final AuthenticationManager authenticationManager;
    private final AddressRepo addressRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

    @Autowired
    public SellerService(UserRepo userRepo, SellerRepo sellerRepo, AuthenticationManager authenticationManager,
                         AddressRepo addressRepo, PasswordEncoder passwordEncoder,
                         EmailService emailService, JwtUtil jwtUtil, MessageSource messageSource) {
        this.userRepo = userRepo;
        this.sellerRepo = sellerRepo;
        this.authenticationManager = authenticationManager;
        this.addressRepo = addressRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
    }


    @Value("${sellerRole}")
    private String role;

    @Value("${email.name}")
    private String emailName;

    @Value("${image.user}")
    private String type;

    @Value("${success.message}")
    private String message;


    public ResponseEntity<GenericResponse> createSeller(SellerDTO sellerDto, Locale locale) {
        log.info("Attempting to register seller with email: {}", sellerDto.getEmail());
        if (Boolean.TRUE.equals(userRepo.existsByEmail(sellerDto.getEmail()))) {
            log.warn("Email already exists: {}", sellerDto.getEmail());
            throw new DuplicateEntryException("Email already exists, please try with another email");
        }

        if (Boolean.TRUE.equals(userRepo.existByCompanyNameIgnoreCase(sellerDto.getCompanyName()))) {
            log.warn("Company name already exists: {}", sellerDto.getCompanyName().trim());
            throw new DuplicateEntryException("Company name already exist try with another one");
        }

        if (Boolean.TRUE.equals(sellerRepo.existsByGst(sellerDto.getGst()))) {
            log.warn("GST number already exists: {}", sellerDto.getGst().trim());
            throw new DuplicateEntryException("GST no. already exist");
        }

        Seller seller = new Seller();
        seller.setFirstName(sellerDto.getFirstName());
        seller.setLastName(sellerDto.getLastName());
        seller.setEmail(sellerDto.getEmail());
        seller.setCompanyName(sellerDto.getCompanyName());
        seller.setGst(sellerDto.getGst());
        seller.setCompanyContact(sellerDto.getCompanyContact());
        seller.setPassword(passwordEncoder.encode(sellerDto.getPassword()));

        seller.setIsActive(false);
        seller.setIsExpired(false);
        seller.setIsDeleted(false);
        seller.setIsLocked(false);
        seller.setInvalidAttemptCount(0);


        List<Address> addresses = sellerDto.getAddress().stream()
                .map(
                        addressDTO ->
                        {
                            Address address = new Address();
                            address.setAddressLine(addressDTO.getAddressLine());
                            address.setLabel(addressDTO.getLabel());
                            address.setCity(addressDTO.getCity());
                            address.setState(addressDTO.getState());
                            address.setCountry(addressDTO.getCountry());
                            address.setZipCode(addressDTO.getZipCode());
                            address.setUser(seller);
                            return address;
                        }

                )
                .collect(Collectors.toList());


        seller.setAddresses(addresses);

        if (addresses.size() > 1) {
            log.warn("Seller registration failed: more than one address provided");
            throw new AddressLimitExceededException("A seller can have only one address");
        }

        Set<Role> roles = new HashSet<>();
        Role sellerRole = new Role();
        sellerRole.setAuthority(role);
        roles.add(sellerRole);

        seller.setRoles(roles);

        seller.setPasswordUpdatedDate(LocalDateTime.now());

        userRepo.save(seller);

        emailService.sendEmailToSeller(emailName);
        emailService.sendEmailToSeller(seller.getEmail());
        log.info("Seller created successfully with email: {}", seller.getEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("seller.register", null, locale), message, LocalDateTime.now()));

    }


    public List<SellerResponseDTO> getAllSellers(int page, int size, String sort, String emailFilter) {
        log.info("Fetching sellers with pagination page: {}, size: {}, sort: {}, filter: {}", page, size, sort, emailFilter);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        List<Seller> sellers = sellerRepo.findByRoleAndEmailFilter("SELLER",
                emailFilter,
                pageable

        );

        if (sellers.isEmpty()) {
            log.warn("No sellers found with filter: {}", emailFilter);
            throw new UserNotFoundException("No seller found");
        }

        return sellers.stream()
                .map(
                        seller -> {
                            List<AddressDTO> addressDTOList = seller.getAddresses().stream()
                                    .map(
                                            address ->

                                                    new AddressDTO(
                                                            address.getAddressLine(),
                                                            address.getLabel(),
                                                            address.getCity(),
                                                            address.getState(),
                                                            address.getCountry(),
                                                            address.getZipCode()

                                                    )

                                    ).toList();


                            return new SellerResponseDTO(
                                    seller.getId(),
                                    seller.getEmail(),
                                    seller.getFirstName() +
                                            (seller.getMiddleName() != null ? " " + seller.getMiddleName() : "") +
                                            " " + seller.getLastName(),
                                    seller.getIsActive(),
                                    seller.getCompanyName(),
                                    addressDTOList,
                                    seller.getCompanyContact()


                            );
                        }).toList();

    }


    public Seller getLoggedinSeller(HttpServletRequest request) {
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
            log.error("JWT access token not found in cookies");
            throw new AccessDeniedException("Jwt is not present in cookies");
        }

        email = jwtUtil.extractUserName(token);

        if (email == null) {
            log.error("Failed to extract email from JWT token");
            throw new AccessDeniedException("Invalid token");
        }

        Seller seller = sellerRepo.findByEmail(email);

        if (seller == null) {
            log.error("Seller not found for email: {}", email);
            throw new UserNotFoundException("User not found");
        }
        log.info("Logged-in seller identified: {}", email);
        return seller;
    }


    public SellerViewProfileDTO getProfileDetails(HttpServletRequest request) {

        Seller seller = getLoggedinSeller(request);

        Address address = seller.getAddresses().get(0);
        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setAddressLine(address.getAddressLine());
        addressDTO.setLabel(address.getLabel());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setZipCode(address.getZipCode());
        addressDTO.setCountry(address.getCountry());

        log.info("Fetched profile for seller: {}", seller.getEmail());
        return new SellerViewProfileDTO(
                seller.getId(),
                seller.getFirstName(),
                seller.getLastName(),
                seller.getIsActive(),
                seller.getCompanyContact(),
                seller.getCompanyName(),
                seller.getGst(),
                addressDTO,
                ImageUtils.getImage(seller.getId(), type)
        );

    }


    public ResponseEntity<GenericResponse> updateProfile(SellerUpdateProfileDTO sellerUpdateProfileDTO, HttpServletRequest request, Locale locale) {
        Seller seller = getLoggedinSeller(request);

        if (Boolean.TRUE.equals(userRepo.existByCompanyNameIgnoreCase(sellerUpdateProfileDTO.getCompanyName()))) {
            log.warn("Company name already exists: {}", sellerUpdateProfileDTO.getCompanyName().trim());
            throw new DuplicateEntryException("Company name already exist");
        }

        if (Boolean.TRUE.equals(sellerRepo.existsByGst(sellerUpdateProfileDTO.getGst()))) {
            log.warn("GST number already exists: {}", sellerUpdateProfileDTO.getGst().trim());
            throw new DuplicateEntryException("GST no. already exist");
        }

        seller.setFirstName(Optional.ofNullable(sellerUpdateProfileDTO.getFirstName()).orElse(seller.getFirstName()));
        seller.setMiddleName(Optional.ofNullable(sellerUpdateProfileDTO.getMiddleName()).orElse(seller.getMiddleName()));
        seller.setLastName(Optional.ofNullable(sellerUpdateProfileDTO.getLastName()).orElse(seller.getLastName()));
        seller.setGst(Optional.ofNullable(sellerUpdateProfileDTO.getGst()).orElse(seller.getGst()));
        seller.setCompanyContact(Optional.ofNullable(sellerUpdateProfileDTO.getCompanyContact()).orElse(seller.getCompanyContact()));
        seller.setCompanyName(Optional.ofNullable(sellerUpdateProfileDTO.getCompanyName()).orElse(seller.getCompanyName()));

        ImageUtils.uploadImage(sellerUpdateProfileDTO.getImage(), seller.getId(), type);
        userRepo.save(seller);
        log.info("Seller profile updated for email: {}", seller.getEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("profile.update", null, locale), message, LocalDateTime.now()));

    }

    @Transactional
    public ResponseEntity<GenericResponse> updatePassword(PasswordDTO passwordDTO, HttpServletRequest request) {
        Seller seller = getLoggedinSeller(request);
        seller.setPassword(passwordEncoder.encode(passwordDTO.getPassword()));
        seller.setPasswordUpdatedDate(LocalDateTime.now());
        userRepo.save(seller);
        emailService.notifyPasswordChanged(seller.getEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Password updated successfully", message, LocalDateTime.now()));

    }

    public ResponseEntity<GenericResponse> updateAddress(Long id, AddressDTO addressDTO, HttpServletRequest request) {
        Seller seller = getLoggedinSeller(request);

        Address address = addressRepo.findById(id).orElseThrow(() -> {
            log.error("Address not found with id: {}", id);
            return new ResouceNotFound("Address not found with this id");
        });

        if (!address.getUser().getId().equals(seller.getId())) {
            log.error("Seller [{}] does not own address ID: {}", seller.getEmail(), id);
            throw new AccessDeniedException("User do not have permission to update this address ");
        }


        address.setAddressLine(Optional.ofNullable(addressDTO.getAddressLine()).orElse(address.getAddressLine()));
        address.setCity(Optional.ofNullable(addressDTO.getCity()).orElse(address.getCity()));
        address.setLabel(Optional.ofNullable(addressDTO.getLabel()).orElse(address.getLabel()));
        address.setState(Optional.ofNullable(addressDTO.getState()).orElse(address.getState()));
        address.setCountry(Optional.ofNullable(addressDTO.getCountry()).orElse(address.getCountry()));
        address.setZipCode(Optional.ofNullable(addressDTO.getZipCode()).orElse(address.getZipCode()));

        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        seller.setAddresses(addresses);
        userRepo.save(seller);
        log.info("Seller [{}] updated address ID: {}", seller.getEmail(), id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Address updated successfully", message, LocalDateTime.now()));

    }


}
