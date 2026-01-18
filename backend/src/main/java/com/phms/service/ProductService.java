package com.phms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.phms.dto.ProductSaveDTO;
import com.phms.entity.Product;
import com.phms.vo.ProductVO;

import java.util.List;

/**
 * 商品 Service
 */
public interface ProductService extends IService<Product> {

    Page<ProductVO> pageList(Page<Product> page, String name, Integer status, Long hotelId);

    ProductVO getDetail(Long id);

    boolean saveProduct(ProductSaveDTO dto, Long operatorHotelId, boolean isAdmin);

    boolean updateProduct(ProductSaveDTO dto, Long operatorHotelId, boolean isAdmin);

    boolean updateStatus(Long id, Integer status, Long operatorHotelId, boolean isAdmin);

    boolean adjustStock(Long id, Integer delta, Long operatorHotelId, boolean isAdmin);

    boolean removeProduct(Long id, Long operatorHotelId, boolean isAdmin);

    List<ProductVO> listUserProducts(Long hotelId);

    boolean canManageProduct(Long productId, Long hotelId);
}
