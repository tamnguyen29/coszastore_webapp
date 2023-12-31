package alticshaw.com.coszastore.service;

import alticshaw.com.coszastore.common.ConvertArray;
import alticshaw.com.coszastore.entity.*;
import alticshaw.com.coszastore.entity.ids.ProductColorIds;
import alticshaw.com.coszastore.entity.ids.ProductSizeIds;
import alticshaw.com.coszastore.entity.ids.ProductTagIds;
import alticshaw.com.coszastore.exception.CustomException;
import alticshaw.com.coszastore.exception.NotFoundCustomException;
import alticshaw.com.coszastore.exception.NotImageException;
import alticshaw.com.coszastore.exception.ValidationCustomException;
import alticshaw.com.coszastore.mapper.ModelUtilMapper;
import alticshaw.com.coszastore.payload.request.ProductRequest;
import alticshaw.com.coszastore.payload.request.ProductUploadRequest;
import alticshaw.com.coszastore.payload.response.MessageResponse;
import alticshaw.com.coszastore.payload.response.ProductResponse;
import alticshaw.com.coszastore.payload.response.ProductUploadResponse;
import alticshaw.com.coszastore.payload.response.TagResponse;
import alticshaw.com.coszastore.repository.*;
import alticshaw.com.coszastore.service.imp.ProductServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService implements ProductServiceImp {
    @Value("${path.root.directory}")
    private String pathImage;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductSizeRepository productSizeRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            SizeRepository sizeRepository,
            ColorRepository colorRepository,
            TagRepository tagRepository,
            ProductTagRepository productTagRepository,
            ProductColorRepository productColorRepository,
            ProductSizeRepository productSizeRepository,
            FileStorageService fileStorageService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
        this.tagRepository = tagRepository;
        this.productTagRepository = productTagRepository;
        this.productColorRepository = productColorRepository;
        this.productSizeRepository = productSizeRepository;
        this.fileStorageService = fileStorageService;
    }

//    @Override
//    public List<ProductResponse> getAll() {
//        List<ProductEntity> productEntityList = productRepository.findAllProductsCustom();
//        List<ProductResponse> productResponses = new ArrayList<>();
//        String path = pathImage + File.separator + "images" + File.separator;
//        try {
//            for (ProductEntity product : productEntityList) {
//                ProductResponse productResponse = ModelUtilMapper.map(product, ProductResponse.class);
//
//                productResponse.setCategory_id(product.getCategory().getId());
//
//                if (productResponse.getImage() != null && !productResponse.getImage().isEmpty()) {
//                    productResponse.setImage(path + product.getImage());
//                } else {
//                    productResponse.setImage(null);
//                }
//
//                if (productResponse.getListImage() != null && !productResponse.getListImage().isEmpty()) {
//                    List<String> listImage = ConvertArray.parseStringToList(productResponse.getListImage());
//                    List<String> listImage2 = new ArrayList<>();
//                    for (String image : listImage) {
//                        listImage2.add(path + image);
//                    }
//
//                    productResponse.setListImage(ConvertArray.convertListToUrlString(listImage2));
//                } else {
//                    productResponse.setListImage(null);
//                }
//
//                List<String> sizeList = product.getProductSizes().stream()
//                        .map(sizeEntity -> sizeEntity.getSize().getName())
//                        .collect(Collectors.toList());
//                productResponse.setSize(sizeList);
//
//                List<String> colorList = product.getProductColors().stream()
//                        .map(colorEntity -> colorEntity.getColor().getName())
//                        .collect(Collectors.toList());
//                productResponse.setColor(colorList);
//
//                List<TagResponse> tagResponses = product.getProductTags().stream()
//                        .map(tagEntity -> {
//                            TagResponse tagResponse = ModelUtilMapper.map(tagEntity, TagResponse.class);
//                            tagResponse.setId(tagEntity.getTag().getId());
//                            tagResponse.setName(tagEntity.getTag().getName());
//                            return tagResponse;
//                        })
//                        .collect(Collectors.toList());
//                productResponse.setTag(tagResponses);
//
//                productResponses.add(productResponse);
//            }
//        } catch (CustomException e) {
//            throw new CustomException("Error get list product");
//        }
//        return productResponses;
//    }

    @Override
    public List<ProductResponse> getAll() {
        List<ProductEntity> productEntityList = productRepository.findAllProductsCustom();
        return productEntityList.stream()
                .map(this::mapProductEntityToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse addProduct(ProductRequest productRequest, BindingResult signUpBindingResult) {
        validateRequest(signUpBindingResult);
        return saveProduct(productRequest);
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundCustomException("Product not found with id : " + id, HttpStatus.NOT_FOUND.value()));
        try {
            ProductResponse productResponse = ModelUtilMapper.map(product, ProductResponse.class);
            List<String> sizeList = product.getProductSizes().stream()
                    .map(sizeEntity -> sizeEntity.getSize().getName())
                    .collect(Collectors.toList());
            productResponse.setSize(sizeList);

            List<String> colorList = product.getProductColors().stream()
                    .map(colorEntity -> colorEntity.getColor().getName())
                    .collect(Collectors.toList());
            productResponse.setColor(colorList);

            List<TagResponse> tagResponses = product.getProductTags().stream()
                    .map(tagEntity -> {
                        TagResponse tagResponse = ModelUtilMapper.map(tagEntity, TagResponse.class);
                        tagResponse.setId(tagEntity.getTag().getId());
                        tagResponse.setName(tagEntity.getTag().getName());
                        return tagResponse;
                    })
                    .collect(Collectors.toList());
            productResponse.setTag(tagResponses);

            return productResponse;
        } catch (Exception e) {
            throw new CustomException("Get product by id error " + e.getMessage());
        }
    }

    @Override
    public ProductResponse updateProduct(ProductRequest productRequest, Integer id, BindingResult updateBindingResult) {
        validateRequest(updateBindingResult);
        return updateProduct(productRequest, id);
    }

    @Override
    public ProductUploadResponse uploadImages(ProductUploadRequest productUploadRequest, Integer id) {
        ProductUploadResponse response = new ProductUploadResponse();
        String path = pathImage + File.separator + "images" + File.separator;
        try {
            ProductEntity productEntity = productRepository.findById(id).orElseThrow(() -> new NotFoundCustomException("Product id node found with id: " + id, HttpStatus.NOT_FOUND.value()));
            String image = saveNullOrValidImage(productUploadRequest.getImage());
            List<String> listImage = saveNullOrValidListImage(productUploadRequest.getListImage());
            if (productEntity.getImage() != null && !productEntity.getImage().isEmpty()) {
                fileStorageService.deleteByName(productEntity.getImage());
            }

            if (productEntity.getListImage() != null && !productEntity.getListImage().isEmpty()) {
                List<String> listImage1 = ConvertArray.parseStringToList(productEntity.getListImage());
                for (String image2 : listImage1) {
                    fileStorageService.deleteByName(image2);
                }
            }

            if (image != null && !image.isEmpty()) {
                productEntity.setImage(image);
                response.setImage(path + image);
            }
            if (listImage.size() > 0) {
                List<String> listWithFullPath = new ArrayList<>();
                for (String image1 : listImage) {
                    listWithFullPath.add(path + image1);
                }
                productEntity.setListImage(ConvertArray.convertListToUrlString(listImage));
                response.setList_images(listWithFullPath);
            }
            productRepository.save(productEntity);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return response;
    }

    @Override
    public void deleteProduct(Integer id) {
        ProductEntity product = productRepository.findByProductCustom(id);

        if (product == null) {
            throw new NotFoundCustomException("Product not found with id : " + id, HttpStatus.NOT_FOUND.value());
        }
        try {
            if (product.getImage() != null) {
                fileStorageService.deleteByName(product.getImage());
            }
            productRepository.delete(product);
            new MessageResponse().success();
        } catch (Exception e) {
            throw new CustomException("Delete failed: " + e.getMessage());
        }
    }

    private ProductResponse saveProduct(ProductRequest request) {
        try {
            CategoryEntity category = categoryRepository.findById(request.getCategory_id())
                    .orElseThrow(() -> new NotFoundCustomException("Category id not found", HttpStatus.NOT_FOUND.value()));
            ProductEntity product = new ProductEntity();
            if (category == null) {
                throw new NotFoundCustomException("Category id not found", HttpStatus.NOT_FOUND.value());
            }

            try {
                product.setName(request.getName());
                product.setShort_description(request.getShort_description());
                product.setDescription(request.getDescription());
                product.setImport_price(request.getImport_price());
                product.setPrice(request.getPrice());
                product.setCategory(category);
                product.setQuantity(request.getQuantity());
                product.setDimensions(request.getDimensions());
                product.setWeight(request.getWeight());
                product.setMaterials(request.getMaterials());
                product.setIsNewProduct(request.getIs_new_product());
                product.setIsBestSelling(request.getIs_best_selling());
                productRepository.save(product);

            } catch (Exception e) {
                throw new CustomException("Error add product");
            }

            if (request.getSize().getSize_id() != null && !request.getSize().getSize_id().isEmpty()) {
                List<SizeEntity> sizes = sizeRepository.findAllById(request.getSize().getSize_id());
                if (!sizes.isEmpty()) {
                    Set<ProductSizeEntity> productSizes = sizes.stream().map(size -> {
                        ProductSizeEntity productSizeEntity = new ProductSizeEntity();
                        ProductSizeIds productSizeIds = new ProductSizeIds();

                        productSizeIds.setProductId(product.getId());
                        productSizeIds.setSizeId(size.getId());

                        productSizeEntity.setIds(productSizeIds);
                        productSizeEntity.setSize(size);
                        productSizeEntity.setQuantity(request.getSize().getQuantity());
                        productSizeEntity.setProduct(product);
                        return productSizeEntity;
                    }).collect(Collectors.toSet());

                    product.setProductSizes(productSizes);
                } else {
                    throw new NotFoundCustomException("Size id not found ", HttpStatus.NOT_FOUND.value());
                }
            }

            if (request.getColor().getColor_id() != null && !request.getColor().getColor_id().isEmpty()) {
                List<ColorEntity> colors = colorRepository.findAllById((request.getColor().getColor_id()));

                if (!colors.isEmpty()) {
                    Set<ProductColorEntity> productColors = colors.stream().map(color -> {
                        ProductColorEntity productColorEntity = new ProductColorEntity();
                        ProductColorIds productColorIds = new ProductColorIds();

                        productColorIds.setProductId(product.getId());
                        productColorIds.setColorId(color.getId());
                        productColorEntity.setColor(color);

                        productColorEntity.setIds(productColorIds);
                        productColorEntity.setProduct(product);
                        productColorEntity.setQuantity(request.getColor().getQuantity());
                        return productColorEntity;
                    }).collect(Collectors.toSet());

                    product.setProductColors(productColors);
                } else {
                    throw new NotFoundCustomException("Color id not found ", HttpStatus.NOT_FOUND.value());
                }
            }

            if (request.getTag_id() != null && !request.getTag_id().isEmpty()) {
                List<TagEntity> tags = tagRepository.findAllById(request.getTag_id());

                if (!tags.isEmpty()) {
                    Set<ProductTagEntity> productTags = tags.stream().map(tag -> {
                        ProductTagEntity productTagEntity = new ProductTagEntity();
                        ProductTagIds productTagIds = new ProductTagIds();

                        productTagIds.setProductId(product.getId());
                        productTagIds.setTagId(tag.getId());

                        productTagEntity.setIds(productTagIds);
                        productTagEntity.setTag(tag);
                        productTagEntity.setProduct(product);
                        return productTagEntity;
                    }).collect(Collectors.toSet());

                    product.setProductTags(productTags);
                } else {
                    throw new NotFoundCustomException("Tag id not found ", HttpStatus.NOT_FOUND.value());
                }
            }

            productSizeRepository.saveAll(product.getProductSizes());
            productColorRepository.saveAll(product.getProductColors());
            productTagRepository.saveAll(product.getProductTags());

            ProductResponse productResponse = mapProductEntityToProductResponse(product);

            productResponse.setCategory_id(product.getCategory().getId());

            List<String> sizeList = product.getProductSizes().stream()
                    .map(sizeEntity -> sizeEntity.getSize().getName())
                    .collect(Collectors.toList());

            productResponse.setSize(sizeList);

            List<String> colorList = product.getProductColors().stream()
                    .map(colorEntity -> colorEntity.getColor().getName())
                    .collect(Collectors.toList());
            productResponse.setColor(colorList);

            List<TagResponse> tagResponses = product.getProductTags().stream()
                    .map(tagEntity -> {
                        TagResponse tagResponse = ModelUtilMapper.map(tagEntity, TagResponse.class);
                        tagResponse.setId(tagEntity.getTag().getId());
                        tagResponse.setName(tagEntity.getTag().getName());
                        return tagResponse;
                    })
                    .collect(Collectors.toList());

            productResponse.setTag(tagResponses);
            return productResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private ProductResponse updateProduct(ProductRequest request, Integer id) {
        ProductEntity product = productRepository.findByProductCustom(id);
        if (product == null) {
            throw new NotFoundCustomException("Product id not found", HttpStatus.NOT_FOUND.value());
        }
        try {
            CategoryEntity category = categoryRepository.findById(request.getCategory_id())
                    .orElseThrow(() -> new NotFoundCustomException("Category id not found with ID: " + id, HttpStatus.NOT_FOUND.value()));

            try {
                categoryRepository.findById(request.getCategory_id()).ifPresent(product::setCategory);
                product.setName(request.getName());
                product.setShort_description(request.getShort_description());
                product.setDescription(request.getDescription());
                product.setImport_price(request.getImport_price());
                product.setPrice(request.getPrice());
                product.setCategory(category);
                product.setQuantity(request.getQuantity());
                product.setDimensions(request.getDimensions());
                product.setWeight(request.getWeight());
                product.setMaterials(request.getMaterials());
                product.setIsNewProduct(request.getIs_new_product());
                product.setIsBestSelling(request.getIs_best_selling());

                productRepository.save(product);
            } catch (Exception e) {
                throw new CustomException("Error add product");
            }

            if (request.getSize().getSize_id() != null && !request.getSize().getSize_id().isEmpty()) {
                List<SizeEntity> sizes = sizeRepository.findAllById(request.getSize().getSize_id());
                if (!sizes.isEmpty()) {
                    Set<ProductSizeEntity> productSizes = sizes.stream().map(size -> {
                        ProductSizeEntity productSizeEntity = new ProductSizeEntity();
                        ProductSizeIds productSizeIds = new ProductSizeIds();

                        productSizeIds.setProductId(product.getId());
                        productSizeIds.setSizeId(size.getId());

                        productSizeEntity.setIds(productSizeIds);
                        productSizeEntity.setSize(size);
                        productSizeEntity.setProduct(product);
                        return productSizeEntity;
                    }).collect(Collectors.toSet());

                    product.setProductSizes(productSizes);
                } else {
                    throw new NotFoundCustomException("Size id not found with ID: " + request.getSize().getSize_id(), HttpStatus.NOT_FOUND.value());

                }
            }

            if (request.getColor().getColor_id() != null && !request.getColor().getColor_id().isEmpty()) {
                List<ColorEntity> colors = colorRepository.findAllById(request.getColor().getColor_id());

                if (!colors.isEmpty()) {
                    Set<ProductColorEntity> productColors = colors.stream().map(color -> {
                        ProductColorEntity productColorEntity = new ProductColorEntity();
                        ProductColorIds productColorIds = new ProductColorIds();

                        productColorIds.setProductId(product.getId());
                        productColorIds.setColorId(color.getId());
                        productColorEntity.setColor(color);

                        productColorEntity.setIds(productColorIds);
                        productColorEntity.setProduct(product);
                        return productColorEntity;
                    }).collect(Collectors.toSet());

                    product.setProductColors(productColors);
                } else {
                    throw new NotFoundCustomException("Color id not found with Id: " + request.getColor().getColor_id(), HttpStatus.NOT_FOUND.value());
                }
            }

            if (request.getTag_id() != null && !request.getTag_id().isEmpty()) {
                List<TagEntity> tags = tagRepository.findAllById(request.getTag_id());

                if (!tags.isEmpty()) {
                    Set<ProductTagEntity> productTags = tags.stream().map(tag -> {
                        ProductTagEntity productTagEntity = new ProductTagEntity();
                        ProductTagIds productTagIds = new ProductTagIds();

                        productTagIds.setProductId(product.getId());
                        productTagIds.setTagId(tag.getId());

                        productTagEntity.setIds(productTagIds);
                        productTagEntity.setTag(tag);
                        productTagEntity.setProduct(product);
                        return productTagEntity;
                    }).collect(Collectors.toSet());

                    product.setProductTags(productTags);
                } else {
                    throw new NotFoundCustomException("Tag id not found with ID: " + request.getTag_id(), HttpStatus.NOT_FOUND.value());
                }
            }

            productSizeRepository.saveAll(product.getProductSizes());
            productColorRepository.saveAll(product.getProductColors());
            productTagRepository.saveAll(product.getProductTags());

            ProductResponse productResponse = mapProductEntityToProductResponse(product);

            List<String> sizeList = product.getProductSizes().stream()
                    .map(sizeEntity -> sizeEntity.getSize().getName())
                    .collect(Collectors.toList());
            productResponse.setSize(sizeList);

            List<String> colorList = product.getProductColors().stream()
                    .map(colorEntity -> colorEntity.getColor().getName())
                    .collect(Collectors.toList());
            productResponse.setColor(colorList);

            List<TagResponse> tagResponses = product.getProductTags().stream()
                    .map(tagEntity -> {
                        TagResponse tagResponse = ModelUtilMapper.map(tagEntity, TagResponse.class);
                        tagResponse.setId(tagEntity.getTag().getId());
                        tagResponse.setName(tagEntity.getTag().getName());
                        return tagResponse;
                    })
                    .collect(Collectors.toList());
            productResponse.setTag(tagResponses);

            return productResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        }
    }

    private String saveNullOrValidImage(MultipartFile image) {
        if (!(image == null || image.isEmpty())) {
            if (fileStorageService.isImage(image)) {
                return fileStorageService.save(image);
            } else {
                throw new NotImageException(image.getOriginalFilename() + " is not an image!");
            }
        }
        return null;
    }

    private List<String> saveNullOrValidListImage(List<MultipartFile> images) {
        List<String> listImage = new ArrayList<>();
        for (MultipartFile image : images)
            if (image != null && !image.isEmpty()) {
                if (fileStorageService.isImage(image)) {
                    listImage.add(fileStorageService.save(image));
                } else {
                    throw new NotImageException(image.getOriginalFilename() + " is not an image!");
                }
            }
        return listImage;
    }

    private ProductResponse mapProductEntityToProductResponse(ProductEntity product) {
        String path = pathImage + File.separator + "images" + File.separator;
        ProductResponse productResponse = ModelUtilMapper.map(product, ProductResponse.class);

        productResponse.setIs_new_product(product.getIsNewProduct());
        productResponse.setIs_best_selling(product.getIsBestSelling());
        productResponse.setCategory_id(product.getCategory().getId());

        if (productResponse.getImage() != null && !productResponse.getImage().isEmpty()) {
            productResponse.setImage(path + product.getImage());
        } else {
            productResponse.setImage(null);
        }

        if (productResponse.getListImage() != null && !productResponse.getListImage().isEmpty()) {
            List<String> listImage = ConvertArray.parseStringToList(productResponse.getListImage());
            List<String> listImage2 = listImage.stream()
                    .map(image -> path + image)
                    .collect(Collectors.toList());

            productResponse.setListImage(ConvertArray.convertListToUrlString(listImage2));
        } else {
            productResponse.setListImage(null);
        }

        List<String> sizeList = product.getProductSizes().stream()
                .map(sizeEntity -> sizeEntity.getSize().getName())
                .collect(Collectors.toList());
        productResponse.setSize(sizeList);

        List<String> colorList = product.getProductColors().stream()
                .map(colorEntity -> colorEntity.getColor().getName())
                .collect(Collectors.toList());
        productResponse.setColor(colorList);

        List<TagResponse> tagResponses = product.getProductTags().stream()
                .map(tagEntity -> {
                    TagResponse tagResponse = ModelUtilMapper.map(tagEntity, TagResponse.class);
                    tagResponse.setId(tagEntity.getTag().getId());
                    tagResponse.setName(tagEntity.getTag().getName());
                    return tagResponse;
                })
                .collect(Collectors.toList());
        productResponse.setTag(tagResponses);

        return productResponse;
    }

    private void validateRequest(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationCustomException(bindingResult);
        }
    }

}
