package com.shopperspoint.service;


import com.shopperspoint.dto.*;
import com.shopperspoint.entity.Category;
import com.shopperspoint.entity.CategoryMetadataFieldValues;
import com.shopperspoint.entity.Product;
import com.shopperspoint.entity.ProductVariation;
import com.shopperspoint.exceptionhandler.DuplicateEntryException;
import com.shopperspoint.exceptionhandler.ResouceNotFound;
import com.shopperspoint.repository.*;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo;
    private final CategoryMetadataFieldRepo categoryMetadataFieldRepo;
    private final ProductRepo productRepo;
    private final MessageSource messageSource;
    private final ProductVariationRepo productVariationRepo;

    @Autowired
    public CategoryService(CategoryRepo categoryRepo, CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo,
                           CategoryMetadataFieldRepo categoryMetadataFieldRepo, ProductRepo productRepo,
                           ProductVariationRepo productVariationRepo, MessageSource messageSource) {
        this.categoryRepo = categoryRepo;
        this.categoryMetadataFieldValuesRepo = categoryMetadataFieldValuesRepo;
        this.categoryMetadataFieldRepo = categoryMetadataFieldRepo;
        this.productRepo = productRepo;
        this.productVariationRepo = productVariationRepo;
        this.messageSource = messageSource;
    }


    @Value("${success.message}")
    private String message;


    public Long createCategory(CategoryDTO categoryDTO) {
        String categoryName = categoryDTO.getName().trim();
        log.info("Creating category with name: '{}'", categoryName);
        Long parentCategoryId = categoryDTO.getParentCategoryId();

        Optional<Category> rootCategory;

        if (parentCategoryId == null) {
            rootCategory = categoryRepo.findByNameIgnoreCaseAndParentCategoryIsNull(categoryName);

            if (rootCategory.isPresent()) {
                log.warn("Duplicate category '{}' found at root level", categoryName);
                throw new DuplicateEntryException("Category name present at root level");
            }
        }

        Category parentCategory = null;


        if (parentCategoryId != null) {
            parentCategory = categoryRepo.findById(parentCategoryId).orElseThrow(
                    () -> new ResouceNotFound("Parent category id not found")
            );

            if (parentCategory.getProducts() != null && !parentCategory.getProducts().isEmpty()) {
                log.warn("Parent category ID {} has products", parentCategoryId);
                throw new ResouceNotFound("Parent category have products, so can not create sub category");
            }
        }


        Category currentCategory = parentCategory;

        //check in upper
        while (currentCategory != null) {
            if (currentCategory.getName().equalsIgnoreCase(categoryName)) {
                log.warn("Duplicate category '{}' found in parent hierarchy", categoryName);
                throw new DuplicateEntryException("Category name already present in parent");
            }

            currentCategory = currentCategory.getParentCategory();
        }

        // check in down
        if (duplicateChildPresent(parentCategory, categoryName)) {
            log.warn("Duplicate category '{}' found in children", categoryName);
            throw new DuplicateEntryException("Category name present in subcategories");
        }


        // check same level(optional)
        Optional<Category> siblingCategory = categoryRepo.findByNameIgnoreCaseAndParentCategoryId(categoryName, parentCategoryId);

        if (siblingCategory.isPresent()) {
            log.warn("Duplicate category '{}' found among siblings", categoryName);
            throw new DuplicateEntryException("Category name present is siblings");
        }

        Category category = new Category();

        category.setName(categoryName.toLowerCase());
        category.setParentCategory(parentCategory);

        categoryRepo.save(category);
        log.info("Category '{}' created successfully with ID: {}", categoryName, category.getId());
        return category.getId();

    }

    private boolean duplicateChildPresent(Category category, String name) {
        List<Category> children = categoryRepo.findByParentCategory(category);

        for (Category child : children) {
            if (child.getName().equalsIgnoreCase(name)) {
                log.error("Duplicate category name found in child category '{}'", name);
                return true;
            }

            if (duplicateChildPresent(child, name)) {
                return true;
            }
        }

        return false;
    }


    public CategoryResponseDTO viewCategory(Long id) {
        log.info("Fetching details for category ID: {}", id);
        Category category = categoryRepo.findById(id).orElseThrow(
                () -> {
                    log.error("Category id '{}' not found", id);
                    return new ResouceNotFound("Category id not found");
                }
        );

        ParentCategoryDTO parentCategoryDTO = nestedParent(category.getParentCategory());

        List<Category> childCategories = categoryRepo.findByParentCategoryId(id);
        // get child category
        List<SimpleCategoryDTO> simpleCategoryDTOList = childCategories.stream()
                .map(child ->
                        new SimpleCategoryDTO(
                                child.getId(),
                                child.getName()
                        )).toList();


        List<MetadataFieldDTO> metadataFieldDTOS = category.getMetadataFieldValues().stream()
                .map(metadataFieldValues ->
                        new MetadataFieldDTO(
                                metadataFieldValues.getCategoryMetadataField().getName(),
                                Arrays.stream(metadataFieldValues.getFieldValues().split(","))
                                        .map(values -> values.trim())
                                        .toList()
                        )).toList();


        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                parentCategoryDTO,
                simpleCategoryDTOList,
                metadataFieldDTOS

        );

    }


    private ParentCategoryDTO nestedParent(Category category) {
        if (category == null) return null;

        return new ParentCategoryDTO(
                category.getId(),
                category.getName(),
                nestedParent(category.getParentCategory())
        );
    }


    public List<CategoryResponseDTO> getAllCategories(int page, int size, String sortBy, String order, String filter) {
        log.info("Fetching all categories with page: {}, size: {}, sortBy: {}, order: {}, filter: {}", page, size, sortBy, order, filter);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy));

        Page<Category> categoryPage;

        if (filter != null && !filter.isBlank()) {
            categoryPage = categoryRepo.findByNameContainingIgnoreCase(filter, pageable);
        } else {
            categoryPage = categoryRepo.findAll(pageable);
        }

        return categoryPage.stream()
                .map(
                        category -> {
                            ParentCategoryDTO parentCategoryDTO = nestedParent(category.getParentCategory());

                            List<SimpleCategoryDTO> simpleCategoryDTOList = categoryRepo.findByParentCategory(category)
                                    .stream().map(
                                            child ->
                                                    new SimpleCategoryDTO(
                                                            child.getId(),
                                                            child.getName()
                                                    )
                                    ).toList();


                            List<MetadataFieldDTO> metadataFieldDTOS = category.getMetadataFieldValues()
                                    .stream()
                                    .map(values ->
                                            new MetadataFieldDTO(
                                                    values.getCategoryMetadataField().getName(),
                                                    Arrays.stream(values.getFieldValues().split(","))
                                                            .map(value -> value.trim()).toList()
                                            )
                                    ).toList();


                            return new CategoryResponseDTO(
                                    category.getId(),
                                    category.getName(),
                                    parentCategoryDTO,
                                    simpleCategoryDTOList,
                                    metadataFieldDTOS
                            );

                        }
                ).toList();
    }


    public ResponseEntity<GenericResponse> updateCategory(CategoryUpdateDTO categoryUpdateDTO, Locale locale) {
        log.info("Updating category with ID: {}", categoryUpdateDTO.getId());
        Category category = categoryRepo.findById(categoryUpdateDTO.getId()).orElseThrow(
                () -> new ResouceNotFound("Category id not found")
        );

        String newName = categoryUpdateDTO.getName().trim();

        // check in same parent chain no globally
        Category parent = category.getParentCategory();
        while (parent != null) {
            if (parent.getName().equalsIgnoreCase(newName)) {
                log.warn("Duplicate name '{}' in parent hierarchy during update", newName);
                throw new DuplicateEntryException("Category name already present in parent");
            }

            parent = parent.getParentCategory();
        }

        //check in children

        if (duplicateChildPresent(category, newName)) {
            log.warn("Duplicate name '{}' in child hierarchy during update", newName);
            throw new DuplicateEntryException("Category name already present in child");
        }


        category.setName(categoryUpdateDTO.getName());
        categoryRepo.save(category);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("category.update", null, locale), message, LocalDateTime.now()));

    }


    public List<SellerViewAllCategoryDTO> viewAllLeafCategory() {
        log.info("Fetching all leaf categories for seller view");
        List<Category> allCategory = categoryRepo.findAll();

        if (allCategory.isEmpty()) {
            throw new ResouceNotFound("No categories present");
        }

        List<SellerViewAllCategoryDTO> leafResponseDTO = new ArrayList<>();

        for (Category category : allCategory) {
            List<Category> childrenList = getChildren(category.getId());

            if (!childrenList.isEmpty()) {
                continue; // slip if not leaf
            }

            // build parent chain

            List<SellerViewAllCategoryDTO.ParentCategoryDTO> parentCategoryDTOList = new ArrayList<>();
            Category current = category.getParentCategory();

            while (current != null) {
                parentCategoryDTOList.add(0,  // to add parent in first place
                        new SellerViewAllCategoryDTO.ParentCategoryDTO(
                                current.getId(),
                                current.getName()));

                current = current.getParentCategory();
            }


            // get metadata values
            List<CategoryMetadataFieldValues> metadataFieldValuesList = categoryMetadataFieldValuesRepo.findByCategoryId(
                    category.getId()
            );

            List<MetadataFieldWithIdDTO> metadataFieldDTOS = metadataFieldValuesList.stream()
                    .map(value ->

                            new MetadataFieldWithIdDTO(
                                    value.getCategoryMetadataField().getId(),
                                    value.getCategoryMetadataField().getName(),
                                    Arrays.stream(
                                            value.getFieldValues().split(",")
                                    ).map(
                                            metaValue -> metaValue.toString()
                                    ).toList()
                            )
                    ).toList();


            // build response dto


            SellerViewAllCategoryDTO responseDTO = new SellerViewAllCategoryDTO();
            responseDTO.setCategoryId(category.getId());
            responseDTO.setCategoryName(category.getName());
            responseDTO.setParentChain(parentCategoryDTOList);
            responseDTO.setMetadataFields(metadataFieldDTOS);

            leafResponseDTO.add(responseDTO);


        }

        return leafResponseDTO;


    }

    private List<Category> getChildren(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(
                        () -> new ResouceNotFound("Category id not found")
                );

        return categoryRepo.findByParentCategory(category);
    }


    public List<CustomerViewCategoryDTO> viewCategoryCustomer(Long id) {
        log.info("Fetching categories for customer view with parent ID: {}", id);
        List<Category> categories;

        if (id == null) {
            categories = categoryRepo.findByParentCategoryIsNull();
        } else {
            Category parentCategory = categoryRepo.findById(id).orElseThrow(
                    () -> new ResouceNotFound("Category id not found")
            );

            categories = categoryRepo.findByParentCategoryId(parentCategory.getId());
        }

        return categories.stream()
                .map(category -> new CustomerViewCategoryDTO(
                        category.getId(),
                        category.getName()
                )).toList();

    }


    public CategoryFilterResponseDTO getAllFilterCategoryDetails(Long categoryId) {
        log.info("Getting filter details for category ID: {}", categoryId);
        Category category = categoryRepo.findById(categoryId).orElseThrow(
                () -> new ResouceNotFound("category id not found")
        );


        // check id if it has children or not if not then add the given id

        List<Long> categoryIds = new ArrayList<>();
        List<Category> childCategories = categoryRepo.findByParentCategoryId(categoryId);

        if (childCategories == null && childCategories.isEmpty()) {
            categoryIds.add(category.getId());
        } else {
            for (Category child : childCategories) {
                categoryIds.add(child.getId());
            }
        }

        // get the metadata field for given category
        List<CategoryMetadataFieldValues> metadataList = categoryMetadataFieldValuesRepo.findByCategoryId(categoryId);

        List<MetadataFieldWithIdDTO> metadataFieldWithIdDTOS = metadataList.stream()
                .map(value -> new MetadataFieldWithIdDTO(
                        value.getId().getCategoryMetadataFieldId(),
                        value.getCategory().getName(),
                        Arrays.stream(value.getFieldValues().split(","))
                                .map(meta -> meta.trim()).toList()

                )).toList();


        // fetch products for given category

        List<Product> products = productRepo.findByCategoryIdIn(categoryIds);

        Set<String> brands = products.stream()
                .map(product -> product.getBrand())
                .filter(brand -> brand != null)
                .collect(Collectors.toSet());


        // fetch price from product variation
        List<Long> productIds = products.stream().map(product -> product.getId()).toList();

        List<ProductVariation> productVariations = productVariationRepo.findByProductIdIn(productIds);

        Double minPrice = productVariations.stream()
                .mapToDouble(productVariation -> productVariation.getPrice())
                .min()
                .orElse(0);

        Double maxPrice = productVariations.stream()
                .mapToDouble(productVariation -> productVariation.getPrice())
                .max()
                .orElse(0);


        return new CategoryFilterResponseDTO(
                category.getId(),
                category.getName(),
                brands,
                metadataFieldWithIdDTOS,
                minPrice,
                maxPrice
        );

    }


}
