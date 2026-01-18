package com.phms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phms.common.exception.BusinessException;
import com.phms.common.result.ResultCode;
import com.phms.dto.ProductSaveDTO;
import com.phms.entity.Hotel;
import com.phms.entity.Product;
import com.phms.entity.ProductHotel;
import com.phms.mapper.ProductHotelMapper;
import com.phms.mapper.ProductMapper;
import com.phms.service.HotelService;
import com.phms.service.ProductService;
import com.phms.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final ProductHotelMapper productHotelMapper;
    private final HotelService hotelService;

    @Override
    public Page<ProductVO> pageList(Page<Product> page, String name, Integer status, Long hotelId) {
        List<Long> productIds = null;
        if (hotelId != null) {
            productIds = listProductIdsByHotel(hotelId);
            if (productIds.isEmpty()) {
                Page<ProductVO> empty = new Page<>(page.getCurrent(), page.getSize());
                empty.setTotal(0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), Product::getName, name)
                .eq(status != null, Product::getStatus, status)
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getCreatedAt);
        if (productIds != null) {
            wrapper.in(Product::getId, productIds);
        }
        Page<Product> result = productMapper.selectPage(page, wrapper);
        Page<ProductVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        if (result.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }
        voPage.setRecords(fillHotels(result.getRecords()));
        return voPage;
    }

    @Override
    public ProductVO getDetail(Long id) {
        Product product = getById(id);
        if (product == null || Objects.equals(product.getIsDeleted(), 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        List<ProductVO> list = fillHotels(List.of(product));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProduct(ProductSaveDTO dto, Long operatorHotelId, boolean isAdmin) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setDescription(dto.getDescription());
        product.setImages(dto.getImages());
        product.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        boolean saved = save(product);
        if (!saved) {
            return false;
        }
        bindHotels(product.getId(), resolveHotelIds(dto.getHotelIds(), operatorHotelId, isAdmin));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(ProductSaveDTO dto, Long operatorHotelId, boolean isAdmin) {
        Product product = getById(dto.getId());
        if (product == null || Objects.equals(product.getIsDeleted(), 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (!isAdmin && !canManageProduct(dto.getId(), operatorHotelId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该商品");
        }
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setDescription(dto.getDescription());
        product.setImages(dto.getImages());
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }
        boolean updated = updateById(product);
        if (!updated) {
            return false;
        }
        bindHotels(product.getId(), resolveHotelIds(dto.getHotelIds(), operatorHotelId, isAdmin));
        return true;
    }

    @Override
    public boolean updateStatus(Long id, Integer status, Long operatorHotelId, boolean isAdmin) {
        Product product = getById(id);
        if (product == null || Objects.equals(product.getIsDeleted(), 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (!isAdmin && !canManageProduct(id, operatorHotelId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该商品");
        }
        product.setStatus(status);
        return updateById(product);
    }

    @Override
    public boolean adjustStock(Long id, Integer delta, Long operatorHotelId, boolean isAdmin) {
        Product product = getById(id);
        if (product == null || Objects.equals(product.getIsDeleted(), 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (!isAdmin && !canManageProduct(id, operatorHotelId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该商品");
        }
        int newStock = product.getStock() + delta;
        if (newStock < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "库存不足");
        }
        product.setStock(newStock);
        return updateById(product);
    }

    @Override
    public boolean removeProduct(Long id, Long operatorHotelId, boolean isAdmin) {
        Product product = getById(id);
        if (product == null || Objects.equals(product.getIsDeleted(), 1)) {
            return false;
        }
        if (!isAdmin && !canManageProduct(id, operatorHotelId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作该商品");
        }
        return removeById(id);
    }

    @Override
    public List<ProductVO> listUserProducts(Long hotelId) {
        if (hotelId == null) {
            return Collections.emptyList();
        }
        List<Long> productIds = listProductIdsByHotel(hotelId);
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Product::getId, productIds)
                .eq(Product::getStatus, 1)
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getCreatedAt);
        List<Product> products = list(wrapper);
        return fillHotels(products);
    }

    @Override
    public boolean canManageProduct(Long productId, Long hotelId) {
        if (productId == null || hotelId == null) {
            return false;
        }
        LambdaQueryWrapper<ProductHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductHotel::getProductId, productId)
                .eq(ProductHotel::getHotelId, hotelId)
                .eq(ProductHotel::getIsDeleted, 0);
        return productHotelMapper.selectCount(wrapper) > 0;
    }

    private List<Long> listProductIdsByHotel(Long hotelId) {
        LambdaQueryWrapper<ProductHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductHotel::getHotelId, hotelId)
                .eq(ProductHotel::getIsDeleted, 0);
        List<ProductHotel> list = productHotelMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(ProductHotel::getProductId).toList();
    }

    private void bindHotels(Long productId, List<Long> hotelIds) {
        LambdaQueryWrapper<ProductHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductHotel::getProductId, productId);
        productHotelMapper.delete(wrapper);

        if (hotelIds == null || hotelIds.isEmpty()) {
            return;
        }
        for (Long hotelId : hotelIds) {
            ProductHotel ph = new ProductHotel();
            ph.setProductId(productId);
            ph.setHotelId(hotelId);
            productHotelMapper.insert(ph);
        }
    }

    private List<Long> resolveHotelIds(List<Long> hotelIds, Long operatorHotelId, boolean isAdmin) {
        if (!isAdmin) {
            if (operatorHotelId == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "门店信息缺失");
            }
            return List.of(operatorHotelId);
        }
        if (hotelIds == null || hotelIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择关联门店");
        }
        return hotelIds;
    }

    private List<ProductVO> fillHotels(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = products.stream().map(Product::getId).toList();
        LambdaQueryWrapper<ProductHotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ProductHotel::getProductId, productIds)
                .eq(ProductHotel::getIsDeleted, 0);
        List<ProductHotel> relations = productHotelMapper.selectList(wrapper);

        Map<Long, List<Long>> productHotelIds = new HashMap<>();
        List<Long> hotelIds = new ArrayList<>();
        for (ProductHotel relation : relations) {
            productHotelIds.computeIfAbsent(relation.getProductId(), k -> new ArrayList<>()).add(relation.getHotelId());
            hotelIds.add(relation.getHotelId());
        }
        Map<Long, String> hotelNameMap = new HashMap<>();
        if (!hotelIds.isEmpty()) {
            List<Hotel> hotels = hotelService.listByIds(hotelIds);
            if (hotels != null) {
                hotelNameMap = hotels.stream().collect(Collectors.toMap(Hotel::getId, Hotel::getName, (a, b) -> a));
            }
        }

        List<ProductVO> result = new ArrayList<>();
        for (Product product : products) {
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setName(product.getName());
            vo.setCategory(product.getCategory());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setDescription(product.getDescription());
            vo.setImages(product.getImages());
            vo.setStatus(product.getStatus());
            vo.setCreatedAt(product.getCreatedAt());
            List<Long> ids = productHotelIds.getOrDefault(product.getId(), Collections.emptyList());
            vo.setHotelIds(ids);
            if (!ids.isEmpty()) {
                List<String> names = ids.stream().map(hotelNameMap::get).filter(Objects::nonNull).toList();
                vo.setHotelNames(names);
            } else {
                vo.setHotelNames(Collections.emptyList());
            }
            result.add(vo);
        }
        return result;
    }
}
