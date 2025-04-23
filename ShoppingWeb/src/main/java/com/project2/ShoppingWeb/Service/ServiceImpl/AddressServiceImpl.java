package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Entity.Address;
import com.project2.ShoppingWeb.Service.AddressService;
import com.project2.ShoppingWeb.Repository.AddressRepo;
import com.project2.ShoppingWeb.Service.UserService;
import com.project2.ShoppingWeb.DTO.AddressDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepo addressRepo;
    private final UserService userService;

    @Override
    public AddressDto saveAndUpdateAddress(AddressDto addressDto) {
        User user = userService.getLoginUser();
        Address existingAddress = user.getAddress();

        if(existingAddress == null) {
            // Tạo mới địa chỉ
            Address newAddress = new Address();
            newAddress.setUser(user);
            newAddress.setStreet(addressDto.getStreet());
            newAddress.setCity(addressDto.getCity());
            newAddress.setState(addressDto.getState());
            newAddress.setCountry(addressDto.getCountry());
            
            Address savedAddress = addressRepo.save(newAddress);
            return convertToDto(savedAddress);
        } else {
            // Cập nhật địa chỉ hiện có
            if(addressDto.getStreet() != null) existingAddress.setStreet(addressDto.getStreet());
            if(addressDto.getCity() != null) existingAddress.setCity(addressDto.getCity());
            if(addressDto.getState() != null) existingAddress.setState(addressDto.getState());
            if(addressDto.getCountry() != null) existingAddress.setCountry(addressDto.getCountry());
            
            Address updatedAddress = addressRepo.save(existingAddress);
            return convertToDto(updatedAddress);
        }
    }
    
    // Phương thức chuyển đổi Entity sang DTO
    private AddressDto convertToDto(Address address) {
        if (address == null) return null;
        
        return AddressDto.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .build();
    }
}
