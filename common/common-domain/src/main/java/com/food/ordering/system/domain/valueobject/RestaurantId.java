package com.food.ordering.system.domain.valueobject;

import java.security.PublicKey;
import java.util.UUID;

public class RestaurantId extends BaseId<UUID>{
  public RestaurantId(UUID value) {
    super(value);
  }
}
