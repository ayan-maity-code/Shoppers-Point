package com.shopperspoint.service;


import com.shopperspoint.dto.CategoryMetadataFieldDTO;
import com.shopperspoint.dto.CategoryMetadataFieldValuesDTO;
import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.dto.MetadataFieldValueRequestDTO;
import com.shopperspoint.entity.Category;
import com.shopperspoint.entity.CategoryMetadataField;
import com.shopperspoint.entity.CategoryMetadataFieldValues;
import com.shopperspoint.exceptionhandler.BadRequestException;
import com.shopperspoint.exceptionhandler.DuplicateEntryException;
import com.shopperspoint.exceptionhandler.ResouceNotFound;
import com.shopperspoint.key.CategoryMetadataKey;
import com.shopperspoint.repository.CategoryMetadataFieldRepo;
import com.shopperspoint.repository.CategoryMetadataFieldValuesRepo;
import com.shopperspoint.repository.CategoryRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class MetadataFieldService {

    private final CategoryMetadataFieldRepo categoryMetadataFieldRepo;
    private final CategoryRepo categoryRepo;
    private final CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo;

    @Autowired
    public MetadataFieldService(CategoryMetadataFieldRepo categoryMetadataFieldRepo,
                                CategoryRepo categoryRepo,
                                CategoryMetadataFieldValuesRepo categoryMetadataFieldValuesRepo) {
        this.categoryMetadataFieldRepo = categoryMetadataFieldRepo;
        this.categoryRepo = categoryRepo;
        this.categoryMetadataFieldValuesRepo = categoryMetadataFieldValuesRepo;
    }

    @Value("${success.message}")
    private String message;

    public ResponseEntity<GenericResponse> addMetadata(CategoryMetadataFieldDTO categoryMetadataFieldDTO) {
        log.info("Adding new metadata field: {}", categoryMetadataFieldDTO.getName());
        Optional<CategoryMetadataField> metadataFieldOptional = categoryMetadataFieldRepo.findByNameIgnoreCase(categoryMetadataFieldDTO.getName());

        if (metadataFieldOptional.isPresent()) {
            log.error("Metadata field name '{}' already exists", categoryMetadataFieldDTO.getName());
            throw new DuplicateEntryException("Metadata field name " + categoryMetadataFieldDTO.getName() + " is already exists");

        }

        CategoryMetadataField categoryMetadataField = new CategoryMetadataField();
        categoryMetadataField.setName(categoryMetadataFieldDTO.getName().toLowerCase());
        CategoryMetadataField field = categoryMetadataFieldRepo.save(categoryMetadataField);
        log.info("Metadata field added successfully with id: {}", field.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Field added successfully with id: " + field.getId(), message, LocalDateTime.now()));
    }


    public List<CategoryMetadataFieldDTO> getAllMetadata(int page, int size, String sortBy, String order, String filter) {
        log.info("Fetching metadata fields with filter: {}, page: {}, size: {}, sortBy: {}, order: {}", filter, page, size, sortBy, order);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(order), sortBy));
        Page<CategoryMetadataField> fieldList;
        if (filter != null && !filter.isBlank()) {
            fieldList = categoryMetadataFieldRepo.findByNameContainingIgnoreCase(filter, pageable);
        } else {
            fieldList = categoryMetadataFieldRepo.findAll(pageable);
        }

        log.warn("No metadata fields found for filter: {}", filter);


        log.info("Fetched {} metadata fields", fieldList);

        return fieldList.stream()
                .map(
                        field ->
                        {
                            CategoryMetadataFieldDTO fieldDTO = new CategoryMetadataFieldDTO();
                            fieldDTO.setName(field.getName());
                            fieldDTO.setId(field.getId());
                            return fieldDTO;
                        }
                ).toList();

    }

    @Transactional
    public ResponseEntity<GenericResponse> addMetadataValues(CategoryMetadataFieldValuesDTO categoryMetadataFieldValuesDTO) {
        log.info("Adding metadata values for category id: {}", categoryMetadataFieldValuesDTO.getCategoryId());
        for (MetadataFieldValueRequestDTO fieldValues : categoryMetadataFieldValuesDTO.getMetadataFieldValues()) {
            Long metadataFieldId = fieldValues.getCategoryMetadataFieldId();
            String metadataValues = fieldValues.getValues().toLowerCase();

            Category category = categoryRepo.findById(categoryMetadataFieldValuesDTO.getCategoryId()).orElseThrow(
                    () -> new ResouceNotFound("Category id not found")
            );

            if (categoryRepo.existsByParentCategoryId(category.getId())) {
                log.error("Attempt to add metadata values to a non-leaf category with id: {}", category.getId());
                throw new BadRequestException("Metadata field values can be only added to leaf categories");
            }


            CategoryMetadataField categoryMetadataField = categoryMetadataFieldRepo.findById(
                    metadataFieldId
            ).orElseThrow(
                    () -> new ResouceNotFound("Category metadata field id not found")
            );

            if (!isValidValues(metadataValues)) {
                log.error("Invalid values for metadata field id: {}", metadataFieldId);
                throw new BadRequestException("Invalid values in category metadata");
            }


            Optional<CategoryMetadataFieldValues> existingValues = categoryMetadataFieldValuesRepo.findByCategoryIdAndCategoryMetadataFieldId(
                    categoryMetadataFieldValuesDTO.getCategoryId(), metadataFieldId
            );

            if (existingValues.isPresent()) {
                log.error("Metadata field values already exist for category id: {} and metadata field id: {}", categoryMetadataFieldValuesDTO.getCategoryId(), metadataFieldId);
                throw new BadRequestException("The values of Category and Metadata Field combination already exists");
            }

            CategoryMetadataFieldValues metadataFieldValues = new CategoryMetadataFieldValues();
            metadataFieldValues.setId(new CategoryMetadataKey(categoryMetadataFieldValuesDTO.getCategoryId(),
                    metadataFieldId));

            String commaSeparatedValues = Arrays.stream(
                    metadataValues.split(",")
            ).map(values -> values.trim()).collect(Collectors.joining(","));

            metadataFieldValues.setFieldValues(commaSeparatedValues.toLowerCase());
            metadataFieldValues.setCategoryMetadataField(categoryMetadataField);
            metadataFieldValues.setCategory(category);

            categoryMetadataFieldValuesRepo.save(metadataFieldValues);
            log.info("Metadata values added successfully for category id: {} and metadata field id: {}", categoryMetadataFieldValuesDTO.getCategoryId(), metadataFieldId);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Metadata field values added successfully", message, LocalDateTime.now()));
    }


    private boolean isValidValues(String inputValues) {
        if (inputValues == null || inputValues.trim().isEmpty()) return false;

        String[] values = inputValues.split(",");

        HashSet<String> uniqueValues = new HashSet<>();

        for (String value : values) {
            String trimValue = value.trim();
            if (trimValue.isEmpty()) {
                return false;
            }

            if (!uniqueValues.add(trimValue)) {
                return false;
            }
        }

        return true;
    }


    @Transactional
    public ResponseEntity<GenericResponse> updateMetadataFieldValues(CategoryMetadataFieldValuesDTO metadataFieldValuesDTO) {
        log.info("Updating metadata values for category id: {}", metadataFieldValuesDTO.getCategoryId());
        for (MetadataFieldValueRequestDTO fieldValues : metadataFieldValuesDTO.getMetadataFieldValues()) {
            Long metadataFieldId = fieldValues.getCategoryMetadataFieldId();
            String metadataValues = fieldValues.getValues().toLowerCase();

            List<CategoryMetadataFieldValues> categoryList = categoryMetadataFieldValuesRepo.findByCategoryId(metadataFieldValuesDTO.getCategoryId());

            if (categoryList.isEmpty()) {
                log.error("Category id not found: {}", metadataFieldValuesDTO.getCategoryId());
                throw new ResouceNotFound("Category id not found");
            }

            List<CategoryMetadataFieldValues> fieldList = categoryMetadataFieldValuesRepo.findByCategoryMetadataFieldId(
                    metadataFieldId
            );

            if (fieldList.isEmpty()) {
                log.error("Metadata field id not found: {}", metadataFieldId);
                throw new ResouceNotFound("Metadata field id not found");
            }


            if (!categoryMetadataFieldValuesRepo.existsByCategoryIdAndCategoryMetadataFieldId(metadataFieldValuesDTO.getCategoryId(),
                    metadataFieldId)) {
                log.error("Combination of category id and metadata field id not present: {} - {}", metadataFieldValuesDTO.getCategoryId(), metadataFieldId);
                throw new BadRequestException("The combination of category and metadata field is not present");

            }

            List<CategoryMetadataFieldValues> valuesList = categoryMetadataFieldValuesRepo.findFieldValuesByCategoryIdAndCategoryMetadataFieldId(
                    metadataFieldValuesDTO.getCategoryId(), metadataFieldId
            );

            if (valuesList.isEmpty()) {
                log.error("Values not found for category id: {} and metadata field id: {}", metadataFieldValuesDTO.getCategoryId(), metadataFieldId);
                throw new ResouceNotFound("Values not found");
            }

            CategoryMetadataFieldValues category = valuesList.get(0);

            String existingValues = category.getFieldValues();

            if (!isValidValues(metadataValues) || !isUniqueValues(existingValues, metadataValues)) {
                throw new BadRequestException("Invalid values or values are already present");
            }

            String allValues = mergeValues(existingValues, metadataValues);

            category.setFieldValues(allValues.toLowerCase());
            categoryMetadataFieldValuesRepo.save(category);
            log.info("Metadata field values updated successfully for category id: {} and metadata field id: {}", metadataFieldValuesDTO.getCategoryId(), metadataFieldId);

        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("New metadata field values added successfully", message, LocalDateTime.now()));
    }


    private boolean isUniqueValues(String existingValues, String inputValues) {

        String[] existingValue = existingValues.toLowerCase().split(",");
        String[] inputValue = inputValues.toLowerCase().split(",");

        HashSet<String> existingSet = new HashSet<>(
                Arrays.stream(existingValue)
                        .map(value -> value.trim().toLowerCase())
                        .collect(Collectors.toSet())
        );

        for (String value : inputValue) {
            if (existingSet.contains(value.trim().toLowerCase())) {
                return false;
            }
        }

        return true;

    }

    private String mergeValues(String existingValues, String inputValues) {
        String[] existingValue = existingValues.toLowerCase().split(",");
        String[] inputValue = inputValues.toLowerCase().split(",");

        HashSet<String> mergedValues = new HashSet<>(
                Arrays.stream(existingValue)
                        .map(value -> value.trim().toLowerCase())
                        .collect(Collectors.toSet())
        );

        mergedValues.addAll(
                Arrays.stream(inputValue)
                        .map(value -> value.trim().toLowerCase())
                        .collect(Collectors.toSet())
        );

        return String.join(",", mergedValues);
    }
}
