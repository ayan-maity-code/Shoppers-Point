package com.shopperspoint.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopperspoint.dto.*;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.*;
import com.shopperspoint.exceptionhandler.*;
import com.shopperspoint.repository.CategoryMetadataFieldValuesRepo;
import com.shopperspoint.repository.CategoryRepo;
import com.shopperspoint.repository.ProductRepo;
import com.shopperspoint.repository.ProductVariationRepo;
import com.shopperspoint.utill.ImageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final ProductVariationRepo productVariationRepo;
    private final CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo;
    private final SellerService sellerService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Autowired
    public ProductService(ProductRepo productRepo, CategoryRepo categoryRepo,
                          ProductVariationRepo productVariationRepo, CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo,
                          SellerService sellerService, EmailService emailService,
                          ObjectMapper objectMapper, MessageSource messageSource) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.productVariationRepo = productVariationRepo;
        this.categoryMetadataFieldValuesRepo = categoryMetadataFieldValuesRepo;
        this.sellerService = sellerService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    @Value("${email.name}")
    private String emailName;

    @Value(("${admin.email.name}"))
    private String adminEmail;

    @Value("${image.product.variation}")
    private String type;

    @Value(("${image.product.variation.secondary}"))
    private String secondary;

    @Value(("${id.value}"))
    private Long value;

    @Value("${success.message}")
    private String message;


    public ResponseEntity<GenericResponse> addProduct(ProductDTO productDTO, HttpServletRequest request, Locale locale) {
        log.info("Request received to add product: {}", productDTO.getName());
        Seller seller = sellerService.getLoggedinSeller(request);

        if (seller.getId() == null) {
            log.error("Seller not found in session");
            throw new UserNotFoundException("Seller not found");
        }

        if (productRepo.existsByNameIgnoreCaseAndBrandAndCategoryIdAndSellerId(
                productDTO.getName().trim(),
                productDTO.getBrand().trim(),
                productDTO.getCategoryId(),
                seller.getId()

        )) {
            log.warn("Duplicate product found for sellerId: {}", seller.getId());
            throw new BadRequestException("Product already exists for this seller with same name, brand, and category");
        }

        Category category = categoryRepo.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResouceNotFound("Category id not found"));

        if (categoryRepo.existsByParentCategoryId(category.getId())) {
            log.warn("Provided category is not a leaf category");
            throw new BadRequestException("Provided category id is not leaf, please provide a valid one");
        }

        Product product = new Product();

        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription().toLowerCase());
        }
        product.setName(productDTO.getName().toLowerCase().trim());
        product.setBrand(productDTO.getBrand().toLowerCase().trim());
        product.setIsActive(false);
        product.setIsCancellable(productDTO.getIsCancellable());
        product.setIsReturnable(productDTO.getIsReturnable());
        product.setIsDeleted(false);
        product.setCategory(category);
        product.setSeller(seller);


        productRepo.save(product);
        log.info("Product saved successfully: {}", product.getId());

        emailService.sendNewProductEmailToAdmin(emailName, product);
        emailService.sendNewProductEmailToAdmin(adminEmail, product);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("product.added", null, locale), message, LocalDateTime.now()));
    }


    public ProductResponseDTO viewProductOfSeller(Long productId, HttpServletRequest request) {
        log.info("Request to view product with ID: {}", productId);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        Seller seller = sellerService.getLoggedinSeller(request);

        if (seller == null) {
            log.error("Seller not found");
            throw new UserNotFoundException("Seller not found");
        }

        if (!product.getSeller().getId().equals(seller.getId())) {
            log.warn("Unauthorized access attempt by seller ID: {}", seller.getId());
            throw new BadRequestException("This user is not allowed to view this product");
        }

        if (product.getIsDeleted() && product.getIsDeleted() != null) {
            log.warn("Attempt to access deleted product: {}", productId);
            throw new BadRequestException("Product is deleted");
        }

        ProductResponseDTO responseDTO = new ProductResponseDTO();

        responseDTO.setId(product.getId());
        responseDTO.setName(product.getName());
        responseDTO.setBrand(product.getBrand());
        responseDTO.setDescription(product.getDescription());
        responseDTO.setCategoryId(product.getCategory().getId());
        responseDTO.setCategoryName(product.getCategory().getName());
        responseDTO.setIsActive(product.getIsActive());
        responseDTO.setIsCancellable(product.getIsCancellable());
        responseDTO.setIsReturnable(product.getIsReturnable());

        return responseDTO;

    }

    public ResponseEntity<GenericResponse> deleteProductOfSeller(Long productId, HttpServletRequest request) {
        log.info("Deleting product ID: {}", productId);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        Seller seller = sellerService.getLoggedinSeller(request);

        if (seller == null) {
            log.error("Seller not found");
            throw new UserNotFoundException("Seller not found");
        }

        if (!product.getSeller().getId().equals(seller.getId())) {
            log.warn("Unauthorized delete attempt by seller ID: {}", seller.getId());
            throw new BadRequestException("This user is not allowed to delete this product");
        }

        productRepo.deleteById(productId);
        log.info("Product deleted: {}", productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Product deleted successfully", message, LocalDateTime.now()));
    }


    public ResponseEntity<GenericResponse> updateProductOfSeller(Long productId, ProductUpdateDTO productUpdateDTO, HttpServletRequest request) {
        log.info("Updating product ID: {}", productId);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        if (productUpdateDTO.getName() != null) {

            boolean exists = productRepo.existsByNameIgnoreCaseAndBrandAndCategoryIdAndSellerId(
                    productUpdateDTO.getName().toLowerCase().trim(),
                    product.getBrand(),
                    product.getCategory().getId(),
                    product.getSeller().getId()
            );

            if (exists) {
                log.warn("Product name conflict during update for sellerId: {}", product.getSeller().getId());
                throw new BadRequestException("Product name already exists for this brand, category and seller");
            }
        }

        Seller seller = sellerService.getLoggedinSeller(request);

        if (seller == null) {
            log.error("Seller not found");
            throw new UserNotFoundException("Seller not found");
        }

        if (!product.getSeller().getId().equals(seller.getId())) {
            log.warn("Unauthorized update attempt by seller ID: {}", seller.getId());
            throw new BadRequestException("This user is not allowed to update this product");
        }

        if (productUpdateDTO.getName() != null) {
            product.setName(productUpdateDTO.getName().toLowerCase().trim());
        }

        if (productUpdateDTO.getDescription() != null) {
            product.setDescription(productUpdateDTO.getDescription().toLowerCase().trim());
        }
        if (productUpdateDTO.getIsCancellable() != null) {
            product.setIsCancellable(productUpdateDTO.getIsCancellable());
        }

        if (productUpdateDTO.getIsReturnable() != null) {
            product.setIsReturnable(productUpdateDTO.getIsReturnable());
        }

        productRepo.save(product);
        log.info("Product updated: {}", productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Product updated successfully", message, LocalDateTime.now()));

    }

    public List<ProductResponseDTO> viewAllProductOfSeller(int page, int size, String sort, String order, String filter, HttpServletRequest request) {
        Seller seller = sellerService.getLoggedinSeller(request);

        if (seller == null) {
            throw new UserNotFoundException("Seller not found");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));

        Page<Product> productPage;

        if (filter != null && !filter.isBlank()) {
            log.info("Fetching seller products with filter: {}", filter);
            productPage = productRepo.findProductsBySeller(seller.getId(), filter, pageable);
        } else {
            log.info("Fetching all products for sellerId: {}", seller.getId());
            productPage = productRepo.findBySellerIdAndIsDeletedFalse(seller.getId(), pageable);
        }


        return productPage.stream().map(
                product ->
                        new ProductResponseDTO(
                                product.getId(),
                                product.getName(),
                                product.getBrand(),
                                product.getDescription(),
                                product.getIsCancellable(),
                                product.getIsReturnable(),
                                product.getIsActive(),
                                product.getCategory().getId(),
                                product.getCategory().getName()
                        )
        ).toList();

    }

    public ResponseEntity<GenericResponse> addProductVariation(ProductVariationRequestDTO productVariationRequestDTO, HttpServletRequest request) {
        log.info("Adding variation for product ID: {}", productVariationRequestDTO.getProductId());
        Product product = productRepo.findById(productVariationRequestDTO.getProductId()).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        Seller seller = sellerService.getLoggedinSeller(request);

        if (!seller.getId().equals(product.getSeller().getId())) {
            log.warn("Unauthorized variation add attempt by seller ID: {}", seller.getId());
            throw new AccessDeniedException("This user is not allowed to add product variation for this product id");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted()) || Boolean.TRUE.equals(!product.getIsActive())) {

            throw new BadRequestException("Product is either deleted or inactive");
        }

        List<ProductVariation> variations = productVariationRepo.findByProductId(product.getId());

        try {
            for (ProductVariation variation : variations) {
                Map<String, String> existingMetadata = objectMapper.readValue(variation.getMetaData(), new TypeReference<>() {
                });
                existingMetadata = toLowerCaseMap(existingMetadata);
                if (existingMetadata.equals(toLowerCaseMap(productVariationRequestDTO.getMetaData()))) {
                    throw new DuplicateEntryException("Duplicate variation");
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error while parsing metadata JSON: {}", e.getMessage());
        }


        validateMetadata(product.getCategory(), productVariationRequestDTO.getMetaData());

        // convert json meta data
        String jsonMetadata;
        try {
            jsonMetadata = objectMapper.writeValueAsString(productVariationRequestDTO.getMetaData());
        } catch (JsonProcessingException e) {
            log.error("Error serializing metadata: {}", e.getMessage());
            throw new BadRequestException("Error while processing metadata JSON");
        }

        ProductVariation productVariation = new ProductVariation();
        productVariation.setProduct(product);
        productVariation.setQuantityAvailable(productVariationRequestDTO.getQuantityAvailable());
        productVariation.setPrice(productVariationRequestDTO.getPrice());
        productVariation.setMetaData(jsonMetadata.toLowerCase());
        productVariation.setIsActive(true);

        productVariation = productVariationRepo.save(productVariation);

        ImageUtils.uploadImage(productVariationRequestDTO.getPrimaryImage(), productVariation.getId(), type);

        String originalFilename = productVariationRequestDTO.getPrimaryImage().getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String imageName = productVariation.getId() + "." + extension;
        productVariation.setPrimaryImageName(imageName);

        List<MultipartFile> secondaryImages = productVariationRequestDTO.getSecondaryImages();
        if (secondaryImages != null && !secondaryImages.isEmpty()) {
            Long count = 1L;
            for (MultipartFile image : secondaryImages) {
                ImageUtils.uploadImage(image, productVariation.getId() * value + count++, secondary);
            }
        }

        productVariationRepo.save(productVariation);
        log.info("Variation saved with ID: {}", productVariation.getId());


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Product variation added successfully", message, LocalDateTime.now()));

    }

    private Map<String, String> toLowerCaseMap(Map<String, String> metadata) {
        return metadata.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toLowerCase(),
                        e -> e.getValue().toLowerCase()
                ));
    }

    private void validateMetadata(Category category, Map<String, String> metaData) {
        List<CategoryMetadataFieldValues> fieldValues = categoryMetadataFieldValuesRepo.findByCategoryId(category.getId());

        if (fieldValues.isEmpty()) {
            throw new ResouceNotFound("No metadata fields defined for this category.");
        }
        for (CategoryMetadataFieldValues field : fieldValues) {
            String filedName = field.getCategoryMetadataField().getName().toLowerCase();

            List<String> allowedValues = Arrays.asList(field.getFieldValues().toLowerCase().split(","));

            if (!metaData.containsKey(filedName)) {
                throw new ResouceNotFound("Missing metadata filed name");
            }

            String value = metaData.get(filedName);
            if (!allowedValues.contains(value.toLowerCase())) {
                throw new BadRequestException("Invalid meta data value for allowed values ");
            }

        }

        if (metaData.isEmpty()) {
            throw new BadRequestException("Variation must have at least one metadata field value");
        }

    }


    public ProductVariationResponseDTO getProductVariation(Long variationId, HttpServletRequest request) {
        ProductVariation productVariation = productVariationRepo.findById(variationId).orElseThrow(
                () -> new ResouceNotFound("Product variation id not found")
        );
        Seller seller = sellerService.getLoggedinSeller(request);

        if (!seller.getId().equals(productVariation.getProduct().getSeller().getId())) {
            throw new AccessDeniedException("This user is not allowed to view this product variation");
        }

        if (productVariation.getProduct().getIsDeleted()) {
            throw new BadRequestException("Product is deleted");
        }

        return responseDTO(productVariation);

    }


    private ProductVariationResponseDTO responseDTO(ProductVariation productVariation) {
        try {
            log.debug("Mapping ProductVariation to ResponseDTO. ID: {}", productVariation.getId());
            return new ProductVariationResponseDTO(
                    productVariation.getId(),
                    productVariation.getQuantityAvailable(),
                    productVariation.getPrice(),
                    objectMapper.readValue(productVariation.getMetaData(), new TypeReference<>() {
                    }),
                    productVariation.getIsActive(),
                    new ProductResponseDTO(
                            productVariation.getProduct().getId(),
                            productVariation.getProduct().getName(),
                            productVariation.getProduct().getBrand(),
                            productVariation.getProduct().getDescription(),
                            productVariation.getProduct().getIsCancellable(),
                            productVariation.getProduct().getIsReturnable(),
                            productVariation.getProduct().getIsActive(),
                            productVariation.getProduct().getCategory().getId(),
                            productVariation.getProduct().getCategory().getName()
                    ),
                    ImageUtils.getImage(productVariation.getId(), type)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse metadata JSON for variation ID: {}", productVariation.getId(), e);
            throw new BadRequestException(e.getMessage());
        }


    }


    public List<ProductVariationResponseDTO> getVariationsForProduct(int page, int size, String sort, String order, String filter, Long productId, HttpServletRequest request) {
        log.info("Fetching variations for productId: {}", productId);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        Seller seller = sellerService.getLoggedinSeller(request);

        if (!seller.getId().equals(product.getSeller().getId())) {
            throw new AccessDeniedException("This user is not allowed to view this product variation");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is deleted");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));
        log.debug("Pagination: page={}, size={}, sort={}, order={}, filter={}", page, size, sort, order, filter);
        Page<ProductVariation> productVariations;
        if (filter != null && !filter.isBlank()) {
            productVariations = productVariationRepo.findByProductIdAndMetaDataContaining(productId, filter, pageable);
        } else {
            productVariations = productVariationRepo.findByProductId(productId, pageable);
        }
        log.info("Returning {} variations for productId: {}", productVariations.getTotalElements(), productId);
        return productVariations.stream()
                .map(this::responseDTO).toList();
    }


    public ResponseEntity<GenericResponse> updateProductVariation(Long variationId, ProductVariationUpdateDTO variationUpdateDTO, HttpServletRequest request) {
        ProductVariation productVariation = productVariationRepo.findById(variationId).orElseThrow(
                () -> new ResouceNotFound("Product variation not found")
        );

        Seller seller = sellerService.getLoggedinSeller(request);

        if (!seller.getId().equals(productVariation.getProduct().getSeller().getId())) {
            throw new AccessDeniedException("This user is not allowed to update this product variation");
        }

        if (Boolean.TRUE.equals(productVariation.getProduct().getIsDeleted()) || !productVariation.getProduct().getIsActive()) {
            throw new BadRequestException("Product is either deleted or inactive");
        }
        log.info("Updating ProductVariation ID: {}", variationId);

        if (variationUpdateDTO.getMetaData() != null) {
            validateMetadata(productVariation.getProduct().getCategory(), variationUpdateDTO.getMetaData());

            String jsonMetadata;
            try {
                jsonMetadata = objectMapper.writeValueAsString(variationUpdateDTO.getMetaData()).toLowerCase();
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Error while processing metadata JSON");
            }
            productVariation.setMetaData(jsonMetadata.toLowerCase());
            log.info("Updated metadata for variation {}", variationId);
        }


        if (variationUpdateDTO.getPrice() != null) {
            productVariation.setPrice(variationUpdateDTO.getPrice());
        }

        if (variationUpdateDTO.getQuantityAvailable() != null) {
            productVariation.setQuantityAvailable(variationUpdateDTO.getQuantityAvailable());
        }

        if (variationUpdateDTO.getIsActive() != null) {
            log.info("Set isActive = {} for variation {}", variationUpdateDTO.getIsActive(), variationId);
            productVariation.setIsActive(variationUpdateDTO.getIsActive());
        }


        productVariation = productVariationRepo.save(productVariation);

        ImageUtils.uploadImage(variationUpdateDTO.getPrimaryImage(), productVariation.getId(), type);

        List<MultipartFile> secondaryImages = variationUpdateDTO.getSecondaryImages();
        if (secondaryImages != null && !secondaryImages.isEmpty()) {
            Long count = 1L;
            for (MultipartFile image : secondaryImages) {
                ImageUtils.uploadImage(image, productVariation.getId() * value + count++, secondary);
            }
        }
        log.info("Variation updated successfully");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Product variation updated successfully", message, LocalDateTime.now()));

    }


    public ProductViewResponseDTO viewProductAndVariationDetails(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        if (product.getIsDeleted() || !product.getIsActive()) {
            throw new BadRequestException("Product is deleted or not active");
        }

        List<ProductVariation> productVariations = productVariationRepo.findByProductIdAndIsActiveTrue(productId);

        if (productVariations.isEmpty()) {
            throw new BadRequestException("No active variations for this product");
        }

        ProductViewResponseDTO viewResponseDTO = new ProductViewResponseDTO();

        viewResponseDTO.setId(product.getId());
        viewResponseDTO.setName(product.getName());
        viewResponseDTO.setBrand(product.getBrand());
        viewResponseDTO.setDescription(product.getDescription());
        viewResponseDTO.setIsCancellable(product.getIsCancellable());
        viewResponseDTO.setIsReturnable(product.getIsReturnable());

        CategoryViewResponseDTO categoryViewResponseDTO = new CategoryViewResponseDTO(
                product.getCategory().getId(),
                product.getCategory().getName()
        );

        viewResponseDTO.setCategory(categoryViewResponseDTO);

        List<ProductVariationDTO> productVariationDTOS = productVariations.stream()
                .map(
                        variation ->
                        {
                            try {
                                return new ProductVariationDTO(
                                        variation.getId(),
                                        variation.getQuantityAvailable(),
                                        variation.getPrice(),
                                        objectMapper.readValue(variation.getMetaData(), new TypeReference<>() {
                                        }),
                                        ImageUtils.getImage(variation.getId(), type),
                                        variation.getIsActive()
                                );
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ).toList();

        viewResponseDTO.setVariations(productVariationDTOS);

        return viewResponseDTO;
    }


    public ProductViewDTO viewProductByAdmin(Long productId) {
        log.info("Admin viewing product by productId: {}", productId);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        List<ProductVariation> productVariations = productVariationRepo.findByProductId(productId);

        List<String> imageUtils = productVariations.stream().map(
                variation -> ImageUtils.getImage(variation.getId(), type)
        ).filter(Objects::nonNull).toList();
        log.info("Admin fetched product: {}", product.getName());
        return new ProductViewDTO(
                productId,
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getIsCancellable(),
                product.getIsReturnable(),
                new CategoryViewResponseDTO(product.getCategory().getId(), product.getCategory().getName()),
                imageUtils
        );
    }


    public List<ProductViewDTO> viewAllProductsByCustomer(int page, int size, String sort, String order, String filter, Long categoryId) {
        log.info("Customer viewing all products under categoryId: {}", categoryId);
        Category category = categoryRepo.findById(categoryId).orElseThrow(
                () -> new ResouceNotFound("Category not found")
        );

        List<Long> categoryIds = new ArrayList<>();

        if (category.getProducts().isEmpty()) {
            // If this is not a leaf category, fetch all child categories
            categoryIds.addAll(getAllChildCategoryIds(category));
        } else {
            categoryIds.add(categoryId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));
        log.debug("Pagination: page={}, size={}, sort={}, order={}, filter={}", page, size, sort, order, filter);
        Page<Product> productPage;

        if (filter != null && !filter.isBlank()) {
            productPage = productRepo.findByAllCategoryIdsAndFilter(categoryIds, filter.toLowerCase(), pageable);
        } else {
            productPage = productRepo.findByCategoryIdInAndIsActiveTrueAndIsDeletedFalse(categoryIds, pageable);
        }
        log.info("Total products found: {}", productPage.getTotalElements());
        return productPage.stream().map(
                product -> {
                    List<String> imageUtils = product.getProductVariations().stream()
                            .filter(ProductVariation::getIsActive).map(
                                    variation -> ImageUtils.getImage(variation.getId(), type)
                            ).filter(Objects::nonNull).toList();

                    return new ProductViewDTO(
                            product.getId(),
                            product.getName(),
                            product.getBrand(),
                            product.getDescription(),
                            product.getIsCancellable(),
                            product.getIsReturnable(),
                            new CategoryViewResponseDTO(product.getCategory().getId(), product.getCategory().getName()),
                            imageUtils
                    );
                }
        ).toList();

    }

    private List<Category> getChildren(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(
                        () -> new ResouceNotFound("Category id not found")
                );

        return categoryRepo.findByParentCategory(category);
    }

    private List<Long> getAllChildCategoryIds(Category category) {
        List<Long> ids = new ArrayList<>();
        getChildCategoryIdRecursive(category, ids);
        return ids;
    }

    private void getChildCategoryIdRecursive(Category category, List<Long> ids) {

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            ids.add(category.getId());
        }

        List<Category> childCategory = getChildren(category.getId());
        for (Category child : childCategory) {
            getChildCategoryIdRecursive(child, ids);
        }
    }

    public ResponseEntity<GenericResponse> changeProductStatus(Long productId, boolean isActive) {
        log.info("Changing product status. productId: {}, newStatus: {}", productId, isActive);
        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ResouceNotFound("Product not found")
        );

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is deleted so can not update it's status");
        }

        if (product.getIsActive().equals(isActive)) {
            String status = isActive ? "activated" : "deactivated";
            throw new BadRequestException("Product is already " + status);
        }

        product.setIsActive(isActive);
        productRepo.save(product);

        emailService.sendProductStatusUpdateToSeller(emailName, product, product.getIsActive());
        emailService.sendProductStatusUpdateToSeller(product.getSeller().getEmail(), product, product.getIsActive());

        String newState = isActive ? "activated" : "deactivated";
        log.info("Product status updated. productId: {}, currentStatus: {}", productId, product.getIsActive());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Product has been successfully " + newState, message, LocalDateTime.now()));
    }

    public List<ProductViewDTO> viewAllProductsByAdmin(int page, int size, String sort, String order, String filter) {
        log.info("Admin viewing all active products");
        Page<Product> products;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));
        log.debug("Pagination: page={}, size={}, sort={}, order={}, filter={}", page, size, sort, order, filter);
        if (filter != null && !filter.isBlank()) {
            products = productRepo.findActiveProductWithQuery(filter, pageable);
        } else {
            products = productRepo.findByIsDeletedFalseAndIsActiveTrue(pageable);
        }

        log.info("Total active products fetched: {}", products.getTotalElements());
        return products.stream().map(
                product -> {
                    List<String> imageUtils = product.getProductVariations().stream()
                            .filter(ProductVariation::getIsActive).map(
                                    variation -> ImageUtils.getImage(variation.getId(), type)
                            ).filter(Objects::nonNull).toList();

                    return new ProductViewDTO(
                            product.getId(),
                            product.getName(),
                            product.getBrand(),
                            product.getDescription(),
                            product.getIsCancellable(),
                            product.getIsReturnable(),
                            new CategoryViewResponseDTO(product.getCategory().getId(), product.getCategory().getName()),
                            imageUtils
                    );
                }
        ).toList();

    }

    public List<ProductViewDTO> viewSimilarProducts(int page, int size, String sort, String order, String filter, Long productId) {
        log.info("Fetching similar products for productId: {}", productId);
        Product product = productRepo.findByIdAndIsActiveTrueAndIsDeletedFalse(productId).orElseThrow(
                () -> new ResouceNotFound("Product id not found")
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sort));
        log.debug("Pagination: page={}, size={}, sort={}, order={}, filter={}", page, size, sort, order, filter);
        Long categoryId = product.getCategory().getId();

        Page<Product> products;

        if (filter != null && !filter.isBlank()) {
            products = productRepo.findActiveProductsByCategoryIdAndQuery(categoryId, filter, pageable);
        } else {
            products = productRepo.findByCategoryIdAndIsActiveTrueAndIsDeletedFalse(categoryId, pageable);
        }

        log.info("Found {} similar products for categoryId: {}", products.getTotalElements(), categoryId);
        return products.stream().map(
                p -> {
                    List<String> imageUtils = p.getProductVariations().stream()
                            .filter(ProductVariation::getIsActive).map(
                                    variation -> ImageUtils.getImage(variation.getId(), type)
                            ).filter(Objects::nonNull).toList();

                    return new ProductViewDTO(
                            p.getId(),
                            p.getName(),
                            p.getBrand(),
                            p.getDescription(),
                            p.getIsCancellable(),
                            p.getIsReturnable(),
                            new CategoryViewResponseDTO(p.getCategory().getId(), p.getCategory().getName()),
                            imageUtils
                    );
                }
        ).toList();


    }

}
