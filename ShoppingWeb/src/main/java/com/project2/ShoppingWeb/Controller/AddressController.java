package com.project2.ShoppingWeb.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project2.ShoppingWeb.Entity.Address;
import com.project2.ShoppingWeb.Service.AddressService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/save")
    public ResponseEntity<Address> saveAndUpdateAddress(@RequestBody Address address) {
        return ResponseEntity.ok(addressService.saveAndUpdateAddress(address));
    }
}
