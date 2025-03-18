package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Entity.Address;
import com.project2.ShoppingWeb.Service.AddressService;
import com.project2.ShoppingWeb.Repository.AddressRepo;
import com.project2.ShoppingWeb.Service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepo addressRepo;
    private final UserService userService;

    @Override
    public Address saveAndUpdateAddress(Address address) {
        User user = userService.getLoginUser();
        address = user.getAddress();

        if(address == null) {
            address = new Address();
            address.setUser(user);
        }
        
        if(address.getCity() != null) {
            address.setCity(address.getCity());
        }
        if(address.getCountry() != null) {
            address.setCountry(address.getCountry());
        }   
        if(address.getStreet() != null) {
            address.setStreet(address.getStreet());
        }
        if (address.getState() != null) {
            address.setState(address.getState());
            
        }
        addressRepo.save(address);

        return address;
    }
}
