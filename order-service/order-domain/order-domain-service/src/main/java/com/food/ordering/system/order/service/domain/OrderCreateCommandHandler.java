package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import java.util.Optional;
import java.util.UUID;
import javax.sound.midi.MidiFileFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateCommandHandler {

  private final OrderDomainService orderDomainService;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper mapper;

  @Transactional
  public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.getCustomerId());
    Restaurant restaurant = checkRestaurant(createOrderCommand);
    Order order = mapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent =
        orderDomainService.validateAndInitiateOrder(order, restaurant);
    Order orderResult = saveOrder(order);
    return mapper.orderToCreateOrderResponse(orderResult, "SUCCESS");
  }

  private Order saveOrder(Order order) {
    Order orderResult = orderRepository.save(order);
    if (orderResult == null) {
      log.warn("Could not save order!");
      throw new OrderDomainException("");
    }
    log.info("Order is saved with id: {}", order.getId().getValue());
    return orderResult;
  }

  private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
    Restaurant restaurant = mapper.createOrderCommandToRestaurant(createOrderCommand);
    Optional<Restaurant> optionalRestaurant =
        restaurantRepository.findRestaurantInformation(restaurant);
    if (optionalRestaurant.isEmpty()) {
      log.warn("Could not find restaurant with restaurant id: {}",
          createOrderCommand.getRestaurantId());
      throw new OrderDomainException("Could not find restaurant with id: " +
          createOrderCommand.getRestaurantId());
    }
    return optionalRestaurant.get();
  }

  private void checkCustomer(UUID customerId) {
    Optional<Customer> customer = customerRepository.findCustomer(customerId);
    if (customer.isEmpty()) {
      log.warn("Could not find customer with id: {}", customerId);
      throw new OrderDomainException("Could not find customer with id: " + customerId);
    }
  }


}
