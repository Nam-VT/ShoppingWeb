package com.project2.ShoppingWeb.Repository;

import com.project2.ShoppingWeb.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {
}
