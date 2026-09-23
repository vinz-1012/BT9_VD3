package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;

public interface ProductService {

    Page<ProductDTO> getAllProducts(String keyword, Pageable pageable);

    Page<ProductDTO> getProductsByUserId(Long userId, Pageable pageable);

    ProductDTO getProductById(Long id);

    Product findEntityById(Long id);

    ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile, String username);

    ProductDTO updateProduct(Long id, ProductDTO productDTO, MultipartFile newImageFile, String username);

    void deleteProduct(Long id, String username);

    long countTotalProducts();
}
