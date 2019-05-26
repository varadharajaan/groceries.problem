package com.shoppingCart.app.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shoppingCart.app.dao.CartDao;
import com.shoppingCart.app.dao.OrderDao;
import com.shoppingCart.app.dao.ProductDao;
import com.shoppingCart.app.model.Cart;
import com.shoppingCart.app.model.Customer;
import com.shoppingCart.app.model.LineItem;
import com.shoppingCart.app.model.Product;
import com.shoppingCart.app.util.OrderStatus;
import com.shoppingCart.app.model.Order;
import com.shoppingCart.app.model.Order.BuilderOrder;

@Service
@Transactional
public class CartServiceImp implements CartService {

	@Autowired
	CartDao cartDao;

	@Autowired
	ProductDao productDao;
	
	@Autowired
	OrderDao orderDao;

	@Override
	public Long save(Cart cart) {
		return cartDao.save(cart);
	}

	@Override
	public void add(Long idCart, Long idProduct, Integer quantity) {
		Cart cart = cartDao.findBy(idCart);
		Product product = productDao.findBy(idProduct);
		cart.getLinesItems().add(new LineItem(cart, product, quantity, product.getPrice()));
		cartDao.update(cart);
	}

	@Override
	public Long ordered(Long idCart) {
		Cart cart = cartDao.findBy(idCart);
		Order order = new BuilderOrder()
				.setCustomer(cart.getCustomer())
				.setOrdered(new Date())
				.setStatus(OrderStatus.NEW.toString())
				.setTotal(cart.calculateTotal())
				.setLinesItems(cart.getLinesItems())
				.build();
		
		//find discount here
		order = findDiscountsIfAny(order,cart);
		return orderDao.save(order);
	}

	public Order findDiscountsIfAny(Order order, Cart cart) {
		Customer customer = cart.getCustomer();
		double discount =0 ;
		double amount = Double.parseDouble(String.valueOf(cart.getSubtotal()));
		if(null!=customer) {
			if(customer.getIsEmployee().equalsIgnoreCase("true")) {
				discount = 30;
			}
			if(customer.getIsAffialted().equalsIgnoreCase("true")) {
				discount = 5;
				
			}
			List <LineItem> lineItems = order.getLinesItems();
			
			for(LineItem items : lineItems) {
				if(items.getProduct().getCategory().getDescription().equals("groceries")) {
				discount =0;
				}
				else {
					amount = amount+ ((Double.parseDouble(String.valueOf(items.getProduct().getPrice()))/100-discount)*100);
				}
			}
			
			int value = (int) amount/100;
			amount = amount - (value * 5);
			BigDecimal amountTotal = new BigDecimal(amount);
			order.setTotal(amountTotal);
			return order;
		}
		
		
		return order;
	}

}
