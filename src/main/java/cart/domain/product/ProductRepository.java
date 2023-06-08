package cart.domain.product;

import java.util.List;

public interface ProductRepository {

    List<Product> getAllProducts();

    Product getProductById(final Long id);

    Long save(final Product product);

    void updateProduct(final Long id, final Product product);

    void deleteProduct(final Long id);

    boolean existById(final Long id);

    List<Product> getProductsByPage(final int page, final int size);

    long getAllProductCount();
}
