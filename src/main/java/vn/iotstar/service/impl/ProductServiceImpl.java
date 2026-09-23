package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.ProductService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final String CLOUDINARY_FOLDER = "shop/products";

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(String keyword, Pageable pageable) {
        Page<Product> productPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productRepository.searchProducts(keyword.trim(), pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }
        return productPage.map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByUserId(Long userId, Pageable pageable) {
        return productRepository.findByUserId(userId, pageable).map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return productMapper.toDto(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản người dùng: " + username));

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên ảnh đại diện cho sản phẩm");
        }

        CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(imageFile, CLOUDINARY_FOLDER);

        Product product = productMapper.toEntity(productDTO);
        product.setUser(user);
        product.setImageUrl(uploadResult.toDbFormat());

        Product savedProduct = productRepository.save(product);
        log.info("Created new product ID: {} by user: {}", savedProduct.getId(), username);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile newImageFile, String username) {
        Product existingProduct = findEntityById(id);
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));
        boolean isOwner = existingProduct.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa sản phẩm này");
        }

        if (newImageFile != null && !newImageFile.isEmpty()) {
            String oldPublicId = existingProduct.getCloudinaryPublicId();

            CloudinaryUploadResult newUploadResult = cloudinaryService.uploadImage(newImageFile, CLOUDINARY_FOLDER);

            if (oldPublicId != null && !oldPublicId.isBlank()) {
                cloudinaryService.deleteImage(oldPublicId);
            }

            existingProduct.setImageUrl(newUploadResult.toDbFormat());
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Updated product ID: {} by user: {}", updatedProduct.getId(), username);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String username) {
        Product product = findEntityById(id);
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));
        boolean isOwner = product.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Bạn không có quyền xóa sản phẩm này");
        }

        String publicId = product.getCloudinaryPublicId();
        if (publicId != null && !publicId.isBlank()) {
            cloudinaryService.deleteImage(publicId);
        }

        productRepository.delete(product);
        log.info("Deleted product ID: {} by user: {}", id, username);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalProducts() {
        return productRepository.count();
    }
}
